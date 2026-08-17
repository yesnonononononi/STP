package com.summit.stp.user.infrastructure.listener;

import com.rabbitmq.client.Channel;
import com.summit.stp.common.application.domain.event.UserChangedEvent;
import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.elasticsearch.document.UserDocument;
import com.summit.stp.elasticsearch.repo.AdminUserQueryRepository;
import com.summit.stp.user.domain.model.UserSetting;
import com.summit.stp.user.infrastructure.persistence.UserSettingRepositoryImpl;
import com.summit.stp.user.infrastructure.persistence.mapper.UserSettingMapper;
import com.summit.stp.user.infrastructure.persistence.mapper.UserStatMapper;
import com.summit.stp.user.infrastructure.persistence.po.UserSettingPO;
import com.summit.stp.user.infrastructure.persistence.po.UserStatPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserChangedEventListener {

    private final UserStatMapper userStatMapper;
    private final UserSettingMapper userSettingMapper;
    private final AdminUserQueryRepository adminUserQueryRepository;
    private final UserSettingRepositoryImpl userSettingRepositoryImpl;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.User.QUEUE_CHANGE, durable = "true"),
            exchange = @Exchange(name = MqConstants.User.EXCHANGE, type = "topic"),
            key = MqConstants.User.ROUTING_KEY_CHANGE
    ))
    public void onUserChangeEvent(UserChangedEvent event, Message message, Channel channel) throws IOException {
        log.info("【用户事件】标识：UserChange 动作：收到用户生命周期事件, userId={}", event.getUserId());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        Long userId = event.getUserId();
        if (userId == null) {
            channel.basicAck(deliveryTag, false);
            log.warn("【用户事件】 UserChange,userId为空,跳过");
            return;
        }
        try {
            UserChangedEvent.EventType type = event.getEventType();
            if (UserChangedEvent.EventType.CREATE.equals(type)) {
                log.info("【用户事件】标识：UserChange 动作：收到 CREATE 事件，开始执行初始化流程, userId={}", userId);

                // 2. 初始化 user_stat
                initUserStat(userId);
                // 3. 初始化 user_setting
                initUserSetting(userId);
                // 4. 初始化用户Es
                initEs(event);
            } else if (UserChangedEvent.EventType.DELETE.equals(type)) {
                deleteEs(userId);
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("【用户事件】标识：UserChange 动作：处理用户生命周期事件失败, userId={}", userId, e);
            // 不重投递回队首，防止单条消息挂掉引发消费者死循环阻塞
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void initEs(UserChangedEvent event) {
        Long userId = event.getUserId();
        try {
            UserDocument doc = UserDocument.builder()
                    .id(userId)
                    .userId(userId)
                    .gender(event.getGender())
                    .nick(Objects.requireNonNullElse(event.getNick(),""))
                    .phone(Objects.requireNonNullElse(event.getPhone    (),""))
                    .ip(Objects.requireNonNullElse(event.getIp  (),""))
                    .createTime(Objects.requireNonNullElse(event.getCreateTime(), new Timestamp(System.currentTimeMillis())))
                    .isVip(Objects.requireNonNullElse(event.getIsVip(), false))
                    .age(Objects.requireNonNullElse(event.getAge(), 0))
                    .vipLevel((int) (event.getVipLevel() != null ? event.getVipLevel().longValue() : 0L))
                    .statusCode(1)
                    .build();
            adminUserQueryRepository.save(doc);
            log.info("【用户事件】标识：Elasticsearch 动作：CREATE保存用户ES文档成功, userId={}", userId);
        } catch (Exception e) {
            log.error("【用户事件】标识：Elasticsearch 动作：CREATE保存用户ES文档失败, userId={}", userId, e);
        }
    }

    private void deleteEs(Long userId) {
        log.info("【用户事件】标识：UserChange 动作：收到 DELETE 事件，开始从 ES 删除文档, userId={}", userId);
        try {
            adminUserQueryRepository.deleteById(userId);
            log.info("【用户事件】标识：Elasticsearch 动作：删除用户ES文档成功, userId={}", userId);
        } catch (Exception e) {
            log.error("【用户事件】标识：Elasticsearch 动作：删除用户ES文档失败, userId={}", userId, e);
        }
    }



    private void initUserStat(Long userId) {
        try {
            userStatMapper.insert(UserStatPO.builder()
                    .userId(userId)
                    .fans(0L)
                    .topic(0L)
                    .liked(0L)
                    .build());
            log.info("【用户事件】标识：UserStat 动作：初始化统计档案成功, userId={}", userId);
        } catch (DuplicateKeyException e) {
            log.info("【用户事件】标识：UserStat 动作：统计档案已存在, userId={}", userId);
        } catch (Exception e) {
            log.error("【用户事件】标识：UserStat 动作：初始化统计档案失败（已捕获隔离）, userId={}", userId, e);
        }
    }

    private void initUserSetting(Long userId) {
        try {
            UserSetting userSetting = UserSetting.builder().userId(userId).showDelPost(1).customizationRecommend(1).build();
            userSettingRepositoryImpl.save(userSetting);

            log.info("【用户事件】标识：UserSetting 动作：初始化用户设置成功, userId={}", userId);
        } catch (DuplicateKeyException e) {
            log.info("【用户事件】标识：UserSetting 动作：用户设置已存在, userId={}", userId);
        } catch (Exception e) {
            log.error("【用户事件】标识：UserSetting 动作：初始化用户设置失败（已捕获隔离）, userId={}", userId, e);
        }
    }
}
