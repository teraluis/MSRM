package core;

import java.time.Duration;
import java.util.Calendar;
import java.util.TimeZone;

public class SchedulerUtils {

    public static Duration durationToTriggerTime(Integer hours, Integer minutes, Integer secondsPeriod) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris"));
        long nowInMilli = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);
        long expectedInMilli = calendar.getTimeInMillis();
        long interval = expectedInMilli - nowInMilli;
        if (interval >= 0) {
            return Duration.ofMillis(interval);
        } else {
            return Duration.ofMillis(secondsPeriod * 1000 + interval);
        }
    }
}
