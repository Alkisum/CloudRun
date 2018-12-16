package com.alkisum.android.cloudrun.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.alkisum.android.cloudlib.utils.CloudPref;
import com.alkisum.android.cloudrun.location.LocationHelper;

/**
 * Class defining constants for SharedPreferences.
 *
 * @author Alkisum
 * @version 4.0
 * @since 1.0
 */
public final class Pref {

    /**
     * Preference key for keep screen on settings.
     */
    public static final String KEEP_SCREEN_ON = "keepScreenOn";

    /**
     * Preference key for about entry in Settings.
     */
    public static final String ABOUT = "about";

    /**
     * Preference key for build version entry in About.
     */
    public static final String BUILD_VERSION = "buildVersion";

    /**
     * Preference key for build date entry in About.
     */
    public static final String BUILD_DATE = "buildDate";

    /**
     * Preference key for github entry in About.
     */
    public static final String GITHUB = "github";

    /**
     * Preference key for left tile.
     */
    public static final String TILE_LEFT = "leftTile";

    /**
     * Preference key for top right tile.
     */
    public static final String TILE_RIGHT_TOP = "topRightTile";

    /**
     * Preference key for bottom right tile.
     */
    public static final String TILE_RIGHT_BOTTOM = "bottomRightTile";

    /**
     * Preference key for distance count.
     */
    public static final String DISTANCE_CNT = "distanceCnt";

    /**
     * Preference key for active routes.
     */
    public static final String ACTIVE_ROUTES = "activeRoutes";

    /**
     * Preference key for distance to marker.
     */
    public static final String DISTANCE_TO_MARKER = "distanceToMarker";

    /**
     * CloudPref constructor.
     */
    private Pref() {

    }

    /**
     * Initialize the preferences with their default values.
     *
     * @param context Context
     */
    public static void init(final Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (!sharedPref.contains(CloudPref.SAVE_ADDRESS)) {
            editor.putBoolean(CloudPref.SAVE_ADDRESS,
                    CloudPref.DEFAULT_SAVE_ADDRESS);
        }
        if (!sharedPref.contains(CloudPref.SAVE_PATH)) {
            editor.putBoolean(CloudPref.SAVE_PATH,
                    CloudPref.DEFAULT_SAVE_PATH);
        }
        if (!sharedPref.contains(CloudPref.SAVE_USERNAME)) {
            editor.putBoolean(CloudPref.SAVE_USERNAME,
                    CloudPref.DEFAULT_SAVE_USERNAME);
        }
        if (!sharedPref.contains(CloudPref.SAVE_PASSWORD)) {
            editor.putBoolean(CloudPref.SAVE_PASSWORD,
                    CloudPref.DEFAULT_SAVE_PASSWORD);
        }
        if (!sharedPref.contains(KEEP_SCREEN_ON)) {
            editor.putBoolean(KEEP_SCREEN_ON, true);
        }
        if (!sharedPref.contains(DISTANCE_CNT)) {
            editor.putInt(DISTANCE_CNT, LocationHelper.DISTANCE_CNT_DEFAULT);
        }
        if (!sharedPref.contains(DISTANCE_TO_MARKER)) {
            editor.putString(DISTANCE_TO_MARKER,
                    Markers.DISTANCE_TO_MARKER_DEFAULT);
        }
        editor.apply();
    }
}
