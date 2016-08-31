package com.alkisum.android.ownrun.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class to format values.
 *
 * @author Alkisum
 * @version 1.0
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
     * Format the pace value.
     *
     * @param pace Pace in millis
     * @return Format pace
     */
    private static String formatPace(final long pace) {
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
        float avgSpeed = (distance / 1000f) / (time / 3600000f);
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
        long avgPace = Math.round(time / (distance / 1000f));
        if (distance == 0 || avgPace > 3600000) {
            avgPace = 0;
        }
        return formatPace(avgPace);
    }

    /**
     * Format constructor.
     */
    private Format() {

    }
}
