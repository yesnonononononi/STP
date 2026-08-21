package com.summit.stp.member.infrastructure.listener;

import com.rabbitmq.client.Channel;
import com.summit.stp.admin.domain.exception.NoMemberPackageException;
import com.summit.stp.common.application.domain.event.OrderPaidEvent;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.common.util.DistributedLockUtil;
import com.summit.stp.member.domain.exception.NoSuchMemberPackageException;
import com.summit.stp.member.domain.model.Member;
import com.summit.stp.member.domain.model.MemberLevelConfig;
import com.summit.stp.member.domain.model.UserMember;
import com.summit.stp.member.domain.repository.MemberLevelConfigRepository;
import com.summit.stp.member.domain.repository.MemberRepository;
import com.summit.stp.member.domain.repository.UserMemberRepository;
import com.summit.stp.user.infrastructure.constants.UserConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class MemberPayEventQueueListener {
    private final UserMemberRepository userMemberRepository;
    private final MemberRepository<Member> memberRepository;
    private final MemberLevelConfigRepository<MemberLevelConfig> memberLevelConfigRepository;
    private final TransactionTemplate transactionTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final DistributedLockUtil distributedLockUtil;
    private final JsonMapper objectMapper;

    private static final Long TIMEOUT = 3L; // 3 seconds
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.Member.QUEUE, durable = "true"),
            exchange = @Exchange(name = MqConstants.Member.EXCHANGE, type = "topic", durable = "true"),
            key = MqConstants.Member.ROUTING_KEY
    ))
 
    public void onEvent(OrderPaidEvent event, Message message, Channel channel) throws InterruptedException, IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        long orderId = event.getOrderId();
        log.info("【MQ-member】收到会员权益分发事件，订单ID: {}, DeliveryTag: {}", orderId, deliveryTag);

        String lockKey = UserConstants.Cache.LOCK_PAY + orderId;
        RLock lock = distributedLockUtil.getLock(lockKey);
        try {
            // 尝试获取锁，等待5秒，超时则重新入队列
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                // 尝试原子抢占消费标记（防重复消费）
                if (!tryConsume(orderId)) {
                    log.info("【MQ-member】会员权益分发事件已处理或处理中，订单ID: {}", orderId);
                    channel.basicAck(deliveryTag, false);
                    return;
                }

                final UserMember[] userMemberHolder = new UserMember[1];
                transactionTemplate.executeWithoutResult(status -> {
                    //查询充值会员套餐信息
                    long packageId = event.getPackageId();
                    Member member = queryMemberInfo(packageId);

                    //查询用户会员状态
                    UserMember userMember = userMemberRepository.queryUserMemberByUserIdForUpdate(event.getCreatorId());
                    //如果用户没有会员信息则初始化
                    if (userMember == null) {
                        userMember = initUserMember(event.getCreatorId(), member);
                    }
                    //查询目标等级
                    List<MemberLevelConfig> levelList = memberLevelConfigRepository.findAll();

                    //更新用户状态
                    userMember.recharge(levelList, member, event.getQuantity());

                    //保存用户
                    userMemberRepository.save(userMember);
                    userMemberHolder[0] = userMember;
                });

                UserMember updatedUserMember = userMemberHolder[0];
                if (updatedUserMember != null) {
                    updateVipCache(updatedUserMember);
                }

                channel.basicAck(deliveryTag, false);
                log.info("【MQ-member】会员权益分发事件处理成功，订单ID: {}", orderId);
            } else {
                log.warn("【MQ-member】获取分布式锁超时，消息将重回队列，订单ID: {}", orderId);
                channel.basicNack(deliveryTag, false, true);
            }
        } catch (TransactionException | IOException exception) {
            throw exception;
        } catch (BusinessException e) {
            //业务异常,直接入死信,避免无限重试
            log.error("【MQ-member】会员权益分发事件处理失败，订单ID: {}, 错误码: {}, 错误信息: {}", orderId, e.getErrorCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("【MQ-member】会员权益分发事件处理失败，订单ID: {}, 错误信息: {}", orderId, e.getMessage());
            throw e;
        } finally {
            distributedLockUtil.releaseLock(lock);
        }
    }

    private boolean tryConsume(Long orderId) {
        String key = UserConstants.Cache.PAY_CONSUME_MARK + orderId;
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", TIMEOUT, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }
    
    


    private UserMember initUserMember(Long uid, Member member) {
        return UserMember.builder()
                .userId(uid)
                .totalRecharge(0)
                .level(MemberLevelConfig.builder().level(1L).build())
                .memberType(member.getType())
                .packageTypeId(member.getType().getTypeId())
                .expireTime(new Timestamp(System.currentTimeMillis()))
                .dailyRate(BigDecimal.ZERO)
                .levelUpgradeTime(null)
                .updateTime(null)
                .build();
    }


    private Member queryMemberInfo(Long memberId) {
        return memberRepository.findMemberById(memberId).orElseThrow(NoMemberPackageException::new);
    }

    private void updateVipCache(UserMember userMember) {
        try {
            if (userMember.isMemberActive()) {
                String key = UserConstants.Cache.STATE_CACHE + userMember.getUserId();
                String value = objectMapper.writeValueAsString(userMember.getMemberType());
                long remainingMillis = userMember.getExpireTime().getTime() - System.currentTimeMillis();
                if (remainingMillis > 0) {
                    stringRedisTemplate.opsForValue().set(key, value, remainingMillis, TimeUnit.MILLISECONDS);
                    log.info("【会员模块】用户:{}，更新缓存成功，TTL: {} ms", userMember.getUserId(), remainingMillis);
                } else {
                    stringRedisTemplate.delete(key);
                }
            } else {
                String key = UserConstants.Cache.STATE_CACHE + userMember.getUserId();
                stringRedisTemplate.delete(key);
            }
        } catch (Exception e) {
            log.error("【会员模块】用户:{}，更新缓存失败", userMember.getUserId(), e);
        }
    }
}
