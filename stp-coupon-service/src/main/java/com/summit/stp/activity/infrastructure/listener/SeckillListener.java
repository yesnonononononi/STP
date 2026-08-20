package com.summit.stp.activity.infrastructure.listener;

import com.rabbitmq.client.Channel;
import com.summit.stp.activity.domain.event.SeckillEvent;
import com.summit.stp.activity.domain.repository.CouponActivityRepository;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.user_coupon.domain.model.UserCoupon;
import com.summit.stp.user_coupon.domain.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import java.io.IOException;
import java.sql.Timestamp;


@Component
@RequiredArgsConstructor
@Slf4j
public class SeckillListener {
    private final CouponActivityRepository couponActivityRepository;
    private final UserCouponRepository userCouponRepository;
    private final TransactionTemplate transactionTemplate;

    @RabbitListener(bindings = @QueueBinding(exchange = @Exchange(name = MqConstants.Coupon.EXCHANGE,type = "topic"), value = @Queue(name = MqConstants.Coupon.COUPON_SECKILL_QUEUE), key = MqConstants.Coupon.COUPON_SECKILL_ROUTING_KEY))
    public void listenSeckill(SeckillEvent event, Channel channel, Message message) {
        log.info("【优惠券异步落库】Received seckill message: {}", message);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        UserCoupon userCoupon = event.getUserCoupon();
        if (userCoupon == null) throw new RuntimeException("UserCoupon is null");
        Long userId = userCoupon.getUserId();
        Long couponId = userCoupon.getTemplate().getId();
        Timestamp endTime = userCoupon.getEndTime();
        if (userId == null || couponId == null || endTime == null) throw new RuntimeException("Invalid seckill event");
        try {
            transactionTemplate.executeWithoutResult(status -> {
                try {
                    // 乐观锁扣减库存,防止超领
                    couponActivityRepository.deductStockWithOptimisticLock(couponId);
                    // 保存用户-优惠券映射关系
                    userCouponRepository.save(userCoupon);
                } catch (DuplicateKeyException e) {
                    log.info("【优惠券异步落库】User has already received the coupon, ignore this message", e);
                    status.setRollbackOnly();
                } catch (BusinessException e) {
                    status.setRollbackOnly();
                }
            });
            channel.basicAck(deliveryTag, false);
            log.info("【优惠券异步落库】UserCoupon saved successfully uid:{}, couponId: {}", userId, couponId);
        } catch (Exception e) {
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
