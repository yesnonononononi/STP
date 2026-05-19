package com.summit.stp.payment.application.service.impl;

import com.summit.stp.payment.application.command.PayCommand;
import com.summit.stp.payment.application.vo.PayVO;
import com.summit.stp.payment.application.service.PayAppService;
import com.summit.stp.payment.domain.model.Coupon;
import com.summit.stp.payment.domain.model.UserCoupon;
import com.summit.stp.payment.domain.model.Order;
import com.summit.stp.payment.domain.model.ShoppingCart;
import com.summit.stp.payment.domain.repository.CommodityRepository;
import com.summit.stp.payment.domain.repository.OrderRepository;
import com.summit.stp.payment.domain.repository.ShoppingCartRepository;
import com.summit.stp.payment.domain.repository.UserCouponRepository;
import com.summit.stp.payment.infrastructure.Enum.PayType;
import com.summit.stp.shared.ThreadContext.UserHolder;
import com.summit.stp.shared.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

@Slf4j
@RequiredArgsConstructor
@Service
public class PayAppServiceImpl implements PayAppService {
    private final CommodityRepository commodityRepository;
    private final OrderRepository orderRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final UserCouponRepository userCouponRepository;

    @Value("${payment.pid}")
    private String pid;

    @Value("${payment.notify_url}")
    private String notifyUrl;

    @Value("${payment.return_url}")
    private String returnUrl;

    /**
     * 应用程序签名(md5)
     */
    @Value("${payment.signType.Md5.sign}")
    private String md5Sign;

    @Value("${payment.signType.RSA.privateKey}")
    private String privateKey;

    @Value("${payment.signType.current}")
    private String currentSignType;

    @Value("${payment.to}")
    private String to;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PayVO pay(PayCommand payCommand) {
        BigDecimal amount, couponAmount = null, discount = null;
        PayType payType = payCommand.getPayType();


        // 1. 根据购物车ID查询购物车
        ShoppingCart shoppingCart = shoppingCartRepository.findShoppingCartById(payCommand.getShoppingCartId());


        // 验证商品数量是否合理
        checkQuantity(shoppingCart.getItems());

        // 2. 是否有优惠券
        handleCoupon(payCommand.getCouponId());

        // 3. 计算订单金额
        amount = calculateAmount(shoppingCart, discount, couponAmount);

        // 4. 生成订单id and 时间戳
        Long orderId = orderRepository.generateOrderId();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        // 5. 执行签名
        String sign = sign(payType, orderId, shoppingCart, amount, timestamp);


        // 6. 构造订单领域聚合根 (符合 DDD)
        Order order = new Order(
                orderId,
                amount,
                payType,
                to,
                sign
        );

        // 7. 保存订单至仓储
        orderRepository.save(order);

        return PayVO.builder()
                .payType(payType)
                .orderId(orderId)
                .amount(amount)
                .to(to)
                .sign(sign)
                .timestamp(timestamp)
                .build();
    }


    public static void checkQuantity(List<ShoppingCart.CartItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("商品数量不能小于0");
        }
    }


    /**
     * 如果有优惠券
     * @param couponId 优惠券ID
     */
    private void handleCoupon(Long couponId) {
        if (couponId == null) {
            return;
        }
        UserCoupon userCoupon = userCouponRepository.findUserCouponById(couponId);

        if (userCoupon != null) {

            // 安全判定：必须属于当前登录用户
            Long currentUserId = UserHolder.getUser().getId();
            if (!userCoupon.getUserId().equals(currentUserId)) {
                throw new IllegalArgumentException("该优惠券不属于当前用户！");
            }

            // 校验并核销
            if (userCoupon.isAvailable()) {
                Coupon template = userCoupon.getTemplate();
                if (template != null) {
                    BigDecimal couponAmount = template.getAmount();
                    BigDecimal discount = template.getDiscount();
                }

                // 执行核销
                userCoupon.use();

                userCouponRepository.save(userCoupon);

            } else {
                throw new IllegalStateException("该优惠券已被使用或已过期！");
            }
        } else {
            throw new IllegalArgumentException("未找到对应的优惠券记录！");
        }
    }


    /**
     * 签名
     * @param payType 支付方式
     * @param orderId 订单ID
     * @param shoppingCart 购物车
     * @param amount 金额
     * @param timestamp 时间戳
     * @return 签名
     */
    private String sign(PayType payType, Long orderId, ShoppingCart shoppingCart, BigDecimal amount, Timestamp timestamp){
        String payload = buildSignPayload(payType, orderId, String.valueOf(shoppingCart.getId()), amount, timestamp);
        if ("RSA".equalsIgnoreCase(currentSignType)) {
            return EncryptUtil.rsaSign(privateKey, payload);
        } else {
            return EncryptUtil.md5(payload + md5Sign);
        }
    }


    public BigDecimal calculateAmount(ShoppingCart cart, BigDecimal discount, BigDecimal couponAmount) {
        return cart.calculateTotal()
                .multiply(discount == null ? BigDecimal.ONE : discount)
                .subtract(couponAmount == null ? BigDecimal.ZERO : couponAmount);
    }

    /**
     * 按照标准规则构建待签名字符串
     */
    private String buildSignPayload(PayType payType, Long orderId, String commodityName, BigDecimal amount, Timestamp timestamp) {
        Map<String, Object> params = buildParamMap(payType, orderId, commodityName, amount, timestamp);
        // 使用 TreeMap 自动按 ASCII 码排序
        TreeMap<String, Object> sortedMap = new TreeMap<>(params);

        StringJoiner joiner = new StringJoiner("&");

        for (Map.Entry<String, Object> entry : sortedMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // 过滤 sign, sign_type 和空值
            if ("sign".equals(key) || "sign_type".equals(key) || value == null || value.toString().isEmpty()) {
                continue;
            }

            // 拼接参数（不进行 URL 编码）
            joiner.add(key + "=" + value);
        }

        return joiner.toString();
    }

    /**
     * 构建签名参数Map(排序)
     */
    private Map<String, Object> buildParamMap(PayType payType, Long orderId, String commodityName, BigDecimal amount, Timestamp timestamp) {
        Map<String, Object> signParams = new java.util.HashMap<>();
        signParams.put("pid", pid);
        signParams.put("type", payType.getType());
        signParams.put("out_trade_no", String.valueOf(orderId));
        signParams.put("notify_url", notifyUrl);
        signParams.put("return_url", returnUrl);
        signParams.put("name", commodityName);
        signParams.put("money", amount.toString());
        signParams.put("timestamp", timestamp);
        return signParams;
    }
}
