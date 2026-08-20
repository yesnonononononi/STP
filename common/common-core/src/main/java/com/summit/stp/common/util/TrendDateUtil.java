package com.summit.stp.common.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 趋势走势日期补齐工具类
 * 统一处理 7d/30d 缺失日期的填充与默认值生成
 */
public class TrendDateUtil {

    private static final DateTimeFormatter DB_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    public record TrendDataResult<T>(List<String> dates, List<T> values) {}

    public static <D, R> TrendDataResult<R> buildTrendData(
            int days,
            Map<String, D> dbDataMap,
            Function<D, R> valueExtractor,
            R defaultValue
    ) {
        int limitDays = (days > 0 && days <= 60) ? days : 7;
        LocalDate endDate = LocalDate.now();

        List<String> dateList = new ArrayList<>(limitDays);
        List<R> valueList = new ArrayList<>(limitDays);

        for (int i = limitDays - 1; i >= 0; i--) {
            LocalDate date = endDate.minusDays(i);
            String dbKey = date.format(DB_DATE_FORMATTER);
            String displayKey = date.format(DISPLAY_DATE_FORMATTER);

            dateList.add(displayKey);
            D item = dbDataMap != null ? dbDataMap.get(dbKey) : null;
            if (item == null && dbDataMap != null) {
                item = dbDataMap.get(displayKey);
            }
            R val = (item != null && valueExtractor != null) ? valueExtractor.apply(item) : null;
            valueList.add(val != null ? val : defaultValue);
        }

        return new TrendDataResult<>(dateList, valueList);
    }
}
