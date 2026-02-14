/**
 * @author duoyian
 * @date 2025/8/19
 */
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TimestampValidator {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void main(String[] args) {
        // 1. 当前时间
        ZonedDateTime now = ZonedDateTime.now(ZONE);
        long nowMillis = now.toInstant().toEpochMilli();

        // 2. 今天12:00
        ZonedDateTime noonToday = LocalDate.now(ZONE).atTime(LocalTime.NOON).atZone(ZONE);
        long noonMillis = noonToday.toInstant().toEpochMilli();

        // 3. 今天17:00
        ZonedDateTime fivePmToday = LocalDate.now(ZONE).atTime(17, 0).atZone(ZONE);
        long fivePmMillis = fivePmToday.toInstant().toEpochMilli();

        // 4. 下一个整点
        ZonedDateTime nextHour = now.truncatedTo(ChronoUnit.HOURS).plusHours(1);
        long nextHourMillis = nextHour.toInstant().toEpochMilli();

        // 5. 今天24:00（次日00:00）
        ZonedDateTime midnight = LocalDate.now(ZONE).plusDays(1).atStartOfDay(ZONE);
        long midnightMillis = midnight.toInstant().toEpochMilli();

        // 打印所有时间点
        System.out.println("当前时间: " + formatTime(now) + " (" + nowMillis + ")");
        System.out.println("今天12:00: " + formatTime(noonToday) + " (" + noonMillis + ")");
        System.out.println("今天17:00: " + formatTime(fivePmToday) + " (" + fivePmMillis + ")");
        System.out.println("下一个整点: " + formatTime(nextHour) + " (" + nextHourMillis + ")");
        System.out.println("今天24:00: " + formatTime(midnight) + " (" + midnightMillis + ")");

        // 计算并打印时间差
        System.out.println("\n时间差验证:");
        printDuration("当前时间 → 今天12:00", nowMillis, noonMillis);
        printDuration("当前时间 → 今天17:00", nowMillis, fivePmMillis);
        printDuration("当前时间 → 下一个整点", nowMillis, nextHourMillis);
        printDuration("当前时间 → 今天24:00", nowMillis, midnightMillis);
        printDuration("今天12:00 → 今天17:00", noonMillis, fivePmMillis);
        printDuration("今天17:00 → 今天24:00", fivePmMillis, midnightMillis);
    }

    private static String formatTime(ZonedDateTime zdt) {
        return zdt.format(TIME_FORMAT);
    }

    private static void printDuration(String label, long startMillis, long endMillis) {
        Duration duration = Duration.ofMillis(endMillis - startMillis);
        long seconds = duration.getSeconds();

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        System.out.printf("%-20s: %02d小时%02d分钟%02d秒 (%,d毫秒)%n",
                label, hours, minutes, secs, (endMillis - startMillis));
    }
}
