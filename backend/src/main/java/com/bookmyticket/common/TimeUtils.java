package com.bookmyticket.common;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

public final class TimeUtils {

    public static final ZoneId INDIA = ZoneId.of("Asia/Kolkata");

    private TimeUtils() {
    }

    /** Formats minutes-from-midnight as HH:mm. */
    public static String clock(int minutesSinceMidnight) {
        int minutes = Math.floorMod(minutesSinceMidnight, 24 * 60);
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    /** Day offset for an arrival that may cross midnight. */
    public static int dayOffset(int departMinutes, int arriveMinutes) {
        return Math.floorDiv(arriveMinutes, 24 * 60) - Math.floorDiv(departMinutes, 24 * 60);
    }

    public static String localClock(Instant instant, ZoneId zone) {
        LocalTime time = instant.atZone(zone).toLocalTime();
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }

    public static String localDate(Instant instant, ZoneId zone) {
        return instant.atZone(zone).toLocalDate().toString();
    }

    public static String dayOffsetLabel(int offset) {
        return offset > 0 ? "+" + offset + " day" + (offset > 1 ? "s" : "") : "";
    }
}
