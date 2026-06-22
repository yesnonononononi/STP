package com.summit.stp.payment.domain;

import com.summit.stp.payment.application.vo.PayVO;
import com.summit.stp.payment.domain.model.PayType;
import com.summit.stp.shared.util.EncryptUtil;
import com.summit.stp.shared.util.PaymentSignHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

@RequiredArgsConstructor
@Service
public class PayDomainService {

    private final PaymentSignHelper paymentSignHelper;

    @Value("${payment.pid}")
    private Integer pid;
    @Value("${payment.to}")
    private String to;
    @Value("${payment.notify_url}")
    private String notifyUrl;

    @Value("${payment.return_url}")
    private String returnUrl;

    @Value("${payment.platform.publicKey}")
    private String platformPublicKey;












    /**
     * 生成签名
     */
    public String generateSign(PayType payType, Long orderId, String memberName, BigDecimal amount, Timestamp timestamp) {
        String payload = buildSignPayload(payType, orderId, memberName, amount, timestamp);
        return paymentSignHelper.sign(payload);
    }


    /**
     * 回调验签
     *
     * @param params 回调参数Map
     * @param sign 签名字符串
     * @return 是否验签通过
     */
    public boolean checkSign(Map<String, String> params, String sign) {
        // 使用回调返回的所有参数构建待签名字符串（剔除 sign 和 sign_type）
        String payload = buildCallbackSignPayload(params);
        // 使用平台公钥验证签名
        return EncryptUtil.rsaVerify(platformPublicKey, payload, sign);
    }

    /**
     * 构建回调待签名字符串（使用回调返回的所有参数）
     */
    private String buildCallbackSignPayload(Map<String, String> params) {
        Map<String, String> sortedParams = new TreeMap<>();

        // 添加所有回调参数（除 sign 和 sign_type）
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!"sign".equals(key) && !"sign_type".equals(key) && value != null) {
                sortedParams.put(key, value);
            }
        }

        // 拼接参数
        StringJoiner joiner = new StringJoiner("&");
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            joiner.add(entry.getKey() + "=" + entry.getValue());
        }

        return joiner.toString();
    }




    /**
     * 按照标准规则构建待签名字符串
     */
    public String buildSignPayload(PayType payType, Long orderId, String memberName, BigDecimal amount, Timestamp timestamp) {
        // 使用 TreeMap 自动按 ASCII 码排序
        Map<String, Object> sortedMap = buildParamMap(payType, orderId, memberName, amount, timestamp);


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
     * 构建签名参数Map(按照API文档要求的字段)
     */
    public Map<String, Object> buildParamMap(PayType payType, Long orderId, String memberName, BigDecimal amount, Timestamp timestamp) {
        Map<String, Object> signParams = new TreeMap<>();
        signParams.put("pid", pid);
        signParams.put("type", payType.getType());
        signParams.put("sign_type", paymentSignHelper.getCurrentSignType());
        signParams.put("out_trade_no", String.valueOf(orderId));
        signParams.put("notify_url", notifyUrl);
        signParams.put("return_url", returnUrl + "?orderId=" + orderId);
        signParams.put("name", memberName);
        signParams.put("money", amount == null ? null : amount.setScale(2, RoundingMode.HALF_UP).toPlainString());  // 2位小数字符串
        signParams.put("timestamp", timestamp == null ? null : String.valueOf(timestamp.getTime() / 1000));  // 10位秒级时间戳
        return signParams;
    }

    /**
     * 构建响应参数
     * @param payType 支付渠道类型
     * @param orderId 订单ID
     * @param amount 金额
     * @param memberName 会员名称
     * @param sign 签名
     * @param timestamp 时间戳
     * @return 响应参数
     */

    public PayVO buildResponse(PayType payType, Long orderId, BigDecimal amount, String memberName,String sign,Timestamp timestamp) {
        return PayVO.builder()
                .pid(pid)
                .type(payType.getType())
                .orderId(orderId)
                .money(amount.setScale(2, RoundingMode.HALF_UP).toPlainString())  // 格式化为2位小数字符串
                .memberName(memberName)
                .to(to)
                .sign(sign)
                .timestamp(String.valueOf(timestamp.getTime() / 1000))  // API要求：10位秒级时间戳
                .notifyUrl(notifyUrl)
                .returnUrl(returnUrl + "?orderId=" + orderId)
                .signType(paymentSignHelper.getCurrentSignType())
                .build();
    }


}
