package com.summit.stp.common.util;

import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.crypto.digest.MD5;
import io.netty.util.internal.StringUtil;
import org.springframework.util.MultiValueMap;

import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

public class EncryptUtil {

    /**
     * 构建待签名 payload 字符串 (自动排除 sign 和 sign_type 字段，对键按 ASCII 字典序排序)
     * @param params 请求参数 MultiValueMap
     * @return 待签名的 payload 拼接字符串
     */
    public static String buildSignPayload(MultiValueMap<String, String> params) {
        Map<String, String> sortedMap = new TreeMap<>();
        for (String key : params.keySet()) {
            if ("sign".equals(key) || "sign_type".equals(key)) {
                continue;
            }
            String value = params.getFirst(key);
            if (value != null && !value.trim().isEmpty()) {
                sortedMap.put(key, value);
            }
        }

        StringJoiner joiner = new StringJoiner("&");
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            joiner.add(entry.getKey() + "=" + entry.getValue());
        }
        return joiner.toString();
    }

    public static String md5(String str) {
        return MD5.create().digestHex(str);
    }

    /**
     * 使用私钥对内容进行签名
     */
    public static String rsaSign(String privateKey, String content) {
        Sign sign = new Sign(
                SignAlgorithm.SHA256withRSA, privateKey, null);

        byte[] signed = sign.sign(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return java.util.Base64.getEncoder().encodeToString(signed);
    }

    /**
     * 使用公钥验证签名
     * @param publicKey 公钥
     * @param content 原始内容
     * @param signBase64 签名内容(Base64编码)
     * @return 是否验证通过
     */
        public static boolean rsaVerify(String publicKey, String content, String signBase64) {
        // 参数校验：任一关键参数为空视为验签不通过
        if (publicKey == null || content == null || signBase64 == null) {
            return false;
        }
        try {
            Sign sign = new Sign(
                    SignAlgorithm.SHA256withRSA, null, publicKey);

            byte[] signed = java.util.Base64.getDecoder().decode(signBase64);
            return sign.verify(content.getBytes(java.nio.charset.StandardCharsets.UTF_8), signed);
        } catch (IllegalArgumentException e) {
            // signBase64 非法（含 '-' 等非 Base64 字符、长度异常）时返回验签不通过
            return false;
        } catch (Exception e) {
            // 公钥格式非法、验签过程异常等一律视为验签不通过
            return false;
        }
    }

    /**
     * 使用 BCrypt 对原始密码进行哈希（自动加盐）
     */
    public static String bcryptHash(String rawPassword) {
        return BCrypt.hashpw(rawPassword);
    }

    /**
     * 校验原始密码与 BCrypt 哈希是否匹配
     */
    public static boolean bcryptVerify(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }



    /**
     * 对敏感字符串进行脱敏。
     * <ul>
     *     <li>phone：保留前 3 位与后 4 位，中间以 **** 代替（如 138****8888）；长度不足 8 位时整体掩码。</li>
     *     <li>email：本地部分保留首字符，其余以 **** 代替（如 a****@example.com）；无 @ / 本地部分为空时整体掩码。</li>
     * </ul>
     * <p>实现使用切片 + 字符串拼接，<b>不使用正则</b>：历史实现把脱敏子串当正则传给
     * {@code replaceAll}，遇到 {@code * . +} 等元字符会误替换甚至抛异常；且 email 分支
     * {@code substring(4, indexOf("@")-2)} 在本地部分较短时会抛
     * {@link StringIndexOutOfBoundsException}。两者均已修复。</p>
     *
     * @param str  原始字符串
     * @param type 脱敏类型：phone / email
     * @return 脱敏后的字符串；未知类型返回 null
     */
    public static String encodeStrForStar(String str,String type){
        if(StringUtil.isNullOrEmpty(str))return "";
        switch ( type){
            case "email" ->{
                return maskEmail(str);
            }
            case "phone" ->{
                return maskPhone(str);
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * 邮箱脱敏：本地部分保留首字符，其余以 **** 代替。
     */
    private static String maskEmail(String str) {
        int at = str.indexOf('@');
        if (at <= 0) {
            // 无 @，或 @ 前为空：无法安全保留内容，整体掩码
            return "****";
        }
        String local = str.substring(0, at);
        String domain = str.substring(at);
        if (local.length() <= 1) {
            return "*" + domain;
        }
        return local.charAt(0) + "****" + domain;
    }

    /**
     * 手机号脱敏：保留前 3 位与后 4 位，中间以 **** 代替。
     */
    private static String maskPhone(String str) {
        // 长度不足以安全保留前 3 后 4（至少 8 位），整体掩码
        if (str.length() <= 7) {
            return "****";
        }
        return str.substring(0, 3) + "****" + str.substring(str.length() - 4);
    }
}
