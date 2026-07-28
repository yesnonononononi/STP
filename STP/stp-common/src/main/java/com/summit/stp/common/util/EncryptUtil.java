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
        Sign sign = new Sign(
                SignAlgorithm.SHA256withRSA, null, publicKey);
        
        byte[] signed = java.util.Base64.getDecoder().decode(signBase64);
        return sign.verify(content.getBytes(java.nio.charset.StandardCharsets.UTF_8), signed);
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



    public static String encodeStrForStar(String str,String type){
        if(StringUtil.isNullOrEmpty(str))return "";
        switch ( type){
            case "email" ->{
                return str.replaceAll(str.substring(4,str.indexOf("@")-2), "****");
            }
            case "phone" ->{
                return str.replaceAll(str.substring(3,str.length()-4), "****");
            }
            default -> {
                return null;
            }
        }
    }
}
