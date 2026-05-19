package com.summit.stp.shared.util;

import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.crypto.digest.MD5;

public class EncryptUtil {



    public static String md5(String str) {
        return MD5.create().digestHex(str);
    }

    public static String rsaSign(String privateKey, String content) {
        Sign sign = new Sign(
                SignAlgorithm.SHA256withRSA, privateKey, null);
        return sign.signHex(content);
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
}
