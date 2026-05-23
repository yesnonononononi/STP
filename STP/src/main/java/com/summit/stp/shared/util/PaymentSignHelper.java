package com.summit.stp.shared.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

@Component
public class PaymentSignHelper {

    @Value("${payment.signType.Md5.sign}")
    private String md5Sign;

    @Value("${payment.signType.current}")
    private String currentSignType;

    @Value("${payment.signType.RSA.privateKey}")
    private String privateKey;

    /**
     * 对请求参数自动填充 timestamp、sign_type、sign 并构建 HttpEntity
     * @param params 原始请求参数
     * @return 包含签名和 form-urlencoded 头部的 HttpEntity
     */
    public HttpEntity<MultiValueMap<String, String>> buildFormUrlEncodedEntity(MultiValueMap<String, String> params) {
        if (!params.containsKey("timestamp")) {
            params.add("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        }
        if (!params.containsKey("sign_type")) {
            params.add("sign_type", currentSignType);
        }

        // 生成并添加签名
        String sign = generateSign(params);
        params.add("sign", sign);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return new HttpEntity<>(params, headers);
    }

    /**
     * 根据 MultiValueMap 自动构建待签名载荷并生成签名
     */
    public String generateSign(MultiValueMap<String, String> params) {
        String payload = EncryptUtil.buildSignPayload(params);
        return sign(payload);
    }

    /**
     * 对载荷内容进行数字签名
     */
    public String sign(String payload) {
        if ("RSA".equalsIgnoreCase(currentSignType)) {
            return EncryptUtil.rsaSign(privateKey, payload);
        } else {
            return EncryptUtil.md5(payload + md5Sign);
        }
    }

    /**
     * 获取当前的签名算法类型 (如 RSA, MD5)
     */
    public String getCurrentSignType() {
        return currentSignType;
    }
}
