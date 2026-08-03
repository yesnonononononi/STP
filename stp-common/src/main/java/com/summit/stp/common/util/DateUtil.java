package com.summit.stp.common.util;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间处理工具类，提供多格式日期时间解析、格式化及自动时区换算等功能
 */
public class DateUtil {

    private static final DateTimeFormatter STANDARD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 解析时间戳或ISO时间字符串，异常时返回当前时间
     * @param timestamp 时间字符串或绝对毫秒数时间戳
     * @return Instant
     */
    public static Instant parse(String timestamp) {
        if (timestamp == null || timestamp.trim().isEmpty()) {
            return Instant.now();
        }
        try {
            if (timestamp.matches("^\\d+$")) {
                return Instant.ofEpochMilli(Long.parseLong(timestamp));
            }
            return Instant.parse(timestamp);
        } catch (Exception e) {
            return Instant.now();
        }
    }



}
