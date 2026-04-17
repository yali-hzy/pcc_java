package parse;

import defs.Lifespan;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class TimeParser {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

    private TimeParser() {
    }

    public static Instant parseDateTimeFromTimeStamp(long timestamp) {
        return Instant.ofEpochSecond(timestamp);
    }

    public static Instant parseDateTimeFromTimeStamp(String timestampStr) {
        return parseDateTimeFromTimeStamp(Long.parseLong(timestampStr));
    }

    public static Instant parseDateTimeFromStr(String dateTimeStr) {
        LocalDateTime ldt = LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        return ldt.toInstant(ZoneOffset.UTC);
    }

    public static Lifespan parseLifespan(String lifespanStr) {
        String[] parts = lifespanStr.split("\\s+");
        List<Instant> times = new ArrayList<>();
        for (String part : parts) {
            if (!part.isEmpty() && Character.isDigit(part.charAt(0))) {
                times.add(parseDateTimeFromTimeStamp(part));
            }
        }
        if (times.size() == 1) {
            return new Lifespan.Moment(times.get(0));
        }
        if (times.size() == 2) {
            return new Lifespan.Period(times.get(0), times.get(1));
        }
        throw new IllegalArgumentException("Invalid lifespan format: " + lifespanStr);
    }
}
