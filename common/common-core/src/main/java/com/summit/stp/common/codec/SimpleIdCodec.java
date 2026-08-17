package com.summit.stp.common.codec;

import cn.hutool.core.codec.Base62;
import org.springframework.stereotype.Component;

/**
 * 轻量级 Public ID 编解码实现
 * 基于可逆比特混淆与 Base62 算法，不引入复杂密码学配置
 */
@Component
public class SimpleIdCodec implements IdCodec {

    private static final long MASK = 0x5A8E3F1C9B2D4E67L;

    @Override
    public String encode(Long id) {
        if (id == null) {
            return null;
        }
        long obfuscated = id ^ MASK;
        byte[] bytes = longToBytes(obfuscated);
        return Base62.encode(bytes);
    }

    @Override
    public Long decode(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            byte[] bytes = Base62.decode(value);
            long obfuscated = bytesToLong(bytes);
            return obfuscated ^ MASK;
        } catch (Exception e) {
            throw new IllegalArgumentException("非法的 Public ID 格式: " + value, e);
        }
    }

    private static byte[] longToBytes(long val) {
        byte[] buffer = new byte[8];
        for (int i = 0; i < 8; i++) {
            int offset = (7 - i) * 8;
            buffer[i] = (byte) ((val >> offset) & 0xff);
        }
        return buffer;
    }

    private static long bytesToLong(byte[] bytes) {
        if (bytes == null || bytes.length != 8) {
            throw new IllegalArgumentException("无效的 ID 序列长度");
        }
        long val = 0;
        for (int i = 0; i < 8; i++) {
            val = (val << 8) | (bytes[i] & 0xff);
        }
        return val;
    }
}
