package com.alkisum.android.ownrun.utils;

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
    public static final String DISTANCE = "%.2f";

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
            new SimpleDateFormat("MMM. dd, yyyy HH:mm:ss",
                    Locale.getDefault());

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
                TimeUnit.MILLISECONDS.toHours(duration),
                TimeUnit.MILLISECONDS.toMinutes(duration)
                        % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(duration)
                        % TimeUnit.MINUTES.toSeconds(1));
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
