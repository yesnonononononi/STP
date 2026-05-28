package com.summit.stp.member.infrastructure.listener;

import com.rabbitmq.client.Channel;
import com.summit.stp.member.domain.exception.NoSuchMemberPackageException;
import com.summit.stp.member.domain.model.Member;
import com.summit.stp.member.domain.model.MemberLevelConfig;
import com.summit.stp.member.domain.model.UserMember;
import com.summit.stp.member.domain.repository.MemberLevelConfigRepository;
import com.summit.stp.member.domain.repository.MemberRepository;
import com.summit.stp.member.domain.repository.UserMemberRepository;
import com.summit.stp.order.domain.event.OrderPaidEvent;
import com.summit.stp.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.math.BigDecimal;

import java.sql.Timestamp;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class MemberPayEventQueueListener {
    private final UserMemberRepository userMemberRepository;
    private final MemberRepository memberRepository;
    private final MemberLevelConfigRepository memberLevelConfigRepository;
    private final TransactionTemplate transactionTemplate;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "member.queue.pay", durable = "true"),
            exchange = @Exchange(name = "member.exchange.pay", durable = "true"),
            key = "member.queue.pay"
    ))
 
    public void onEvent(OrderPaidEvent event, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            transactionTemplate.executeWithoutResult(status -> {
                log.info("【MQ-member】收到会员权益分发事件，OrderPaidEvent: {}", event);
                
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
            });

            channel.basicAck(deliveryTag, false);
            log.info("【MQ-member】会员权益分发事件处理成功，OrderPaidEvent: {}", event);
        }catch (TransactionException | IOException exception){
            nack(deliveryTag, channel, event, true);
        }catch (BusinessException e){
            //业务异常,直接入死信,避免无限重试
            log.error("【MQ-member】会员权益分发事件处理失败，OrderPaidEvent: {}, 错误码: {}, 错误信息: {}", event, e.getErrorCode(), e.getMessage());
            nack(deliveryTag, channel, event, false);
        }catch (Exception e){
            log.error("【MQ-member】会员权益分发事件处理失败，OrderPaidEvent: {}, 错误信息: {}", event, e.getMessage());
            nack(deliveryTag, channel, event, true);
        }
    }
    
    
    /**
     * 拒绝消息
 
     */
    private void nack(long deliveryTag, Channel channel,OrderPaidEvent orderPaidEvent,boolean requeue){
        try {
            channel.basicNack(deliveryTag, false, requeue);
            log.error("【MQ-member】会员权益分发事件拒绝成功，OrderPaidEvent: {}", orderPaidEvent);
        }catch (IOException ioException){
            log.error("【MQ-member】会员权益分发事件拒绝失败，OrderPaidEvent: {}, 错误信息: {}", orderPaidEvent, ioException.getMessage());
        }
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
        Member memberById = memberRepository.findMemberById(memberId);
        if(memberById == null){
            throw new NoSuchMemberPackageException("没有该会员套餐");
        }
        return memberById;
    }
}
