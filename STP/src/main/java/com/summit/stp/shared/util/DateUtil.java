package com.summit.stp.shared.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间处理工具类，提供多格式日期时间解析、格式化及自动时区换算等功能
 */
public class DateUtil {

    private static final DateTimeFormatter STANDARD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 将各种格式的日期时间字符串（支持 ISO-8601 含时区、标准 yyyy-MM-dd HH:mm:ss、时间戳等）
     * 统一转换为本地系统/数据库默认时区的标准时间字符串 ("yyyy-MM-dd HH:mm:ss")
     *
     * @param dateStr 待解析的日期时间字符串
     * @return 标准格式时间字符串；若解析失败则返回原字符串
     */
    public static String parseToStandardString(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return dateStr;
        }
        try {
            LocalDateTime localDateTime = parseToLocalDateTime(dateStr);
            if (localDateTime != null) {
                return localDateTime.format(STANDARD_FORMATTER);
            }
        } catch (Exception e) {
            // 忽略异常，降级返回原字符串
        }
        return dateStr;
    }

    /**
     * 将各种格式的日期时间字符串解析为 LocalDateTime，支持时区自动换算
     *
     * @param dateStr 待解析字符串
     * @return LocalDateTime 对象；若入参为空则返回 null
     */
    public static LocalDateTime parseToLocalDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        String trimmed = dateStr.trim();

        // 1. 处理包含 "T" 的 ISO-8601 格式，例如 "2026-05-26T09:49:17.000+08:00"
        if (trimmed.contains("T")) {
            try {
                // 如果包含时区偏移量 (如 +08:00 或 -05:00) 或者是 UTC 标记 Z
                if (trimmed.contains("+") || trimmed.contains("-") || trimmed.endsWith("Z")) {
                    OffsetDateTime odt = OffsetDateTime.parse(trimmed);
                    return odt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
                } else {
                    // 无时区偏置的本地 ISO 格式，如 2026-05-26T09:49:17
                    return LocalDateTime.parse(trimmed);
                }
            } catch (Exception e) {
                // 忽略当前失败，进入通用格式化逻辑
            }
        }

        // 2. 处理纯数字类型的时间戳（秒或毫秒）
        if (trimmed.matches("^\\d+$")) {
            long timestamp = Long.parseLong(trimmed);
            if (trimmed.length() == 10) {
                timestamp *= 1000;
            }
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        }

        // 3. 处理标准日期格式 "yyyy-MM-dd HH:mm:ss"
        try {
            return LocalDateTime.parse(trimmed, STANDARD_FORMATTER);
        } catch (Exception e) {
            // 忽略，进入降级逻辑
        }

        // 4. 降级尝试使用 OffsetDateTime 转换
        try {
            return OffsetDateTime.parse(trimmed).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception e) {
            throw new IllegalArgumentException("Unsupported date format: " + dateStr, e);
        }
    }
}
