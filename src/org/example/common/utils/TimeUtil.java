package org.example.common.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author duoyian
 * @date 2026/3/3
 */
public class TimeUtil {

    /**
     * 解析时间字符串到当天的时间戳
     * @param timeStr 时间字符串，格式如："9:00" 或 "18:00"
     * @return 当天的时间戳（毫秒）
     */
    public static long parseTimeToTodayTimestamp(String timeStr) {
        // 将 "9:00" 转换为 "09:00" 格式
        String formattedTime = formatTimeString(timeStr);

        // 解析时间
        LocalTime localTime = LocalTime.parse(formattedTime);

        // 结合今天日期
        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), localTime);

        // 转换为时间戳
        return dateTime.atZone(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    /**
     * 格式化时间字符串为 HH:mm 格式
     * "9:00" -> "09:00"
     * "18:00" -> "18:00"
     * @param timeStr 原始时间字符串
     * @return 格式化后的时间字符串
     */
    private static String formatTimeString(String timeStr) {
        String[] parts = timeStr.split(":");
        if (parts[0].length() == 1) {
            return "0" + timeStr;
        }
        return timeStr;
    }

}
