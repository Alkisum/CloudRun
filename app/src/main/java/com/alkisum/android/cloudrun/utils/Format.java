package com.alkisum.android.cloudrun.utils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class to format values.
 *
 * @author Alkisum
 * @version 1.3
 * @since 1.0
 */
public final class Format {

    /**
     * Format for Speed.
     */
    private static final String SPEED = "%.1f";

    /**
     * Format for Distance.
     */
    private static final String DISTANCE = "%.2f";

    /**
     * Format for Duration.
     */
    private static final String DURATION = "%02d:%02d:%02d";

    /**
     * Format for Pace.
     */
    private static final String PACE = "%02d:%02d";

    /**
     * Format for JSON file name.
     */
    public static final SimpleDateFormat DATE_TIME_JSON =
            new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault());

    /**
     * Format for date time in history list.
     */
    public static final SimpleDateFormat DATE_TIME_HISTORY =
            new SimpleDateFormat("EEE. MMM. dd, yyyy", Locale.getDefault());

    /**
     * Format for date when adding a session.
     */
    public static final SimpleDateFormat DATE_ADD_SESSION =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * Format for time when adding a session.
     */
    public static final SimpleDateFormat TIME_ADD_SESSION =
            new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    /**
     * Format for build date.
     */
    public static final SimpleDateFormat DATE_BUILD =
            new SimpleDateFormat("MMM. dd, yyyy", Locale.getDefault());

    /**
     * Format duration.
     *
     * @param duration Duration in millis
     * @return Format duration
     */
    public static String formatDuration(final long duration) {
        return String.format(Locale.getDefault(), DURATION,
                getHourFromMillis(duration),
                getMinuteFromMillis(duration),
                getSecondFromMillis(duration));
    }

    /**
     * Round and format the given distance value.
     *
     * @param distance Distance value to format
     * @return Format distance value
     */
    public static String formatDistance(final float distance) {
        return String.format(Locale.getDefault(),
                DISTANCE, round(distance / 1000f, 2));
    }

    /**
     * Round and format the given speed value.
     *
     * @param speed Speed value to format
     * @return Format speed value
     */
    public static String formatSpeed(final float speed) {
        return String.format(Locale.getDefault(), SPEED, round(speed, 1));
    }

    /**
     * Format the pace value.
     *
     * @param pace Pace in millis
     * @return Format pace
     */
    public static String formatPace(final long pace) {
        return String.format(Locale.getDefault(), PACE,
                TimeUnit.MILLISECONDS.toMinutes(pace),
                TimeUnit.MILLISECONDS.toSeconds(pace)
                        % TimeUnit.MINUTES.toSeconds(1));
    }

    /**
     * Calculate and format the speed average from duration and distance.
     *
     * @param time     Duration
     * @param distance Distance
     * @return Formatted speed average
     */
    public static String formatSpeedAvg(final long time,
                                        final float distance) {
        float avgSpeed = 0;
        if (time != 0 && distance != 0) {
            avgSpeed = (distance / 1000f) / (time / 3600000f);
        }
        return String.format(Locale.getDefault(), Format.SPEED, avgSpeed);
    }

    /**
     * Calculate and format the pace average from duration and distance.
     *
     * @param time     Duration
     * @param distance Distance
     * @return Formatted pace average. 0 if the pace is higher than an hour
     */
    public static String formatPaceAvg(final long time, final float distance) {
        long avgPace = 0;
        if (time != 0 && distance != 0) {
            avgPace = Math.round(time / (distance / 1000f));
            if (avgPace > 3600000) {
                avgPace = 0;
            }
        }
        return formatPace(avgPace);
    }

    /**
     * Format a distance value to show the GPS accuracy.
     *
     * @param distance Distance in meter
     * @return Formatted GPS accuracy
     */
    public static String formatGpsAccuracy(final float distance) {
        return String.valueOf(Math.round(distance));
    }

    /**
     * Get the number of hours from time in milliseconds.
     *
     * @param millis Time in milliseconds
     * @return Number of hours
     */
    public static int getHourFromMillis(final long millis) {
        return (int) TimeUnit.MILLISECONDS.toHours(millis);
    }

    /**
     * Get the number of minutes from time in milliseconds.
     *
     * @param millis Time in milliseconds
     * @return Number of minutes
     */
    public static int getMinuteFromMillis(final long millis) {
        return (int) (TimeUnit.MILLISECONDS.toMinutes(millis)
                % TimeUnit.HOURS.toMinutes(1));
    }

    /**
     * Get the number of seconds from time in milliseconds.
     *
     * @param millis Time in milliseconds
     * @return Number of seconds
     */
    public static int getSecondFromMillis(final long millis) {
        return (int) (TimeUnit.MILLISECONDS.toSeconds(millis)
                % TimeUnit.MINUTES.toSeconds(1));
    }

    /**
     * Get the number of milliseconds from the time given in hours, minutes and
     * seconds.
     *
     * @param hour   Number of hours
     * @param minute Number of minutes
     * @param second Number of seconds
     * @return Time in milliseconds
     */
    public static long getMillisFromTime(final int hour, final int minute,
                                         final int second) {
        return TimeUnit.HOURS.toMillis(hour)
                + TimeUnit.MINUTES.toMillis(minute)
                + TimeUnit.SECONDS.toMillis(second);
    }

    /**
     * Round the given float according to the given decimal place.
     *
     * @param d            Float to round
     * @param decimalPlace Decimal place
     * @return Rounded float
     */
    private static float round(final float d, final int decimalPlace) {
        return BigDecimal.valueOf(d).setScale(decimalPlace,
                BigDecimal.ROUND_HALF_UP).floatValue();
    }

    /**
     * Format constructor.
     */
    private Format() {

    }
}
