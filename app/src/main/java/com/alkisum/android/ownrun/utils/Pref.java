package com.alkisum.android.ownrun.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.alkisum.android.jsoncloud.utils.CloudPref;
import com.alkisum.android.ownrun.location.LocationHandler;

/**
 * Class defining constants for SharedPreferences.
 *
 * @author Alkisum
 * @version 2.4
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
        if (!sharedPref.contains(CloudPref.SAVE_OWNCLOUD_INFO)) {
            editor.putBoolean(CloudPref.SAVE_OWNCLOUD_INFO, true);
        }
        if (!sharedPref.contains(KEEP_SCREEN_ON)) {
            editor.putBoolean(KEEP_SCREEN_ON, true);
        }
        if (!sharedPref.contains(DISTANCE_CNT)) {
            editor.putInt(DISTANCE_CNT, LocationHandler.DISTANCE_CNT_DEFAULT);
        }
        editor.apply();
    }
}
