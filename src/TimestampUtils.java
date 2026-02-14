/**
 * @author duoyian
 * @date 2025/8/19
 */
import java.time.*;
import java.time.temporal.ChronoUnit;

public class TimestampUtils {
    public static void main(String[] args) {
        // 1. 当前时间戳（毫秒）
        long timeMillisOfNow = System.currentTimeMillis();

        // 2. 今天12:00:00的时间戳（推荐使用常量LocalTime.NOON）
        long timestampOf12h = LocalDate.now()
                .atTime(LocalTime.NOON)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        // 3. 今天17:00:00的时间戳
        long timestampOf17h = LocalDate.now()
                .atTime(17, 0, 0)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        // 4. 下一个整点时间戳（更简洁的写法）
        long nextHourTime = ZonedDateTime.now(ZoneId.systemDefault())
                .truncatedTo(ChronoUnit.HOURS)
                .plusHours(1)
                .toInstant()
                .toEpochMilli();

        // 5. 今天24:00:00（次日00:00:00）时间戳（更直观的写法）
        long midnightTimestamp = LocalDate.now()
                .plusDays(1)
                .atStartOfDay()  // 获取当天的00:00:00
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        // 打印结果
        System.out.println("当前时间戳: " + timeMillisOfNow);
        System.out.println("今天12点时间戳: " + timestampOf12h);
        System.out.println("今天17点时间戳: " + timestampOf17h);
        System.out.println("下一个整点时间戳: " + nextHourTime);
        System.out.println("今天24点时间戳: " + midnightTimestamp);
    }
}
