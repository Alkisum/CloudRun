package com.alkisum.android.cloudrun.location;

import android.content.Context;
import android.location.LocationManager;
import android.util.Log;

/**
 * Utility class for Location.
 *
 * @author Alkisum
 * @version 3.1
 * @since 3.1
 */
public final class LocationUtils {

    /**
     * Log tag.
     */
    private static final String TAG = "LocationUtils";

    /**
     * LocationUtils constructor.
     */
    private LocationUtils() {

    }

    /**
     * Check whether GPS or network are enabled for location.
     *
     * @param context Context
     * @return True if the location is enabled, false otherwise
     */
    public static boolean isLocationEnabled(final Context context) {

        LocationManager lm = (LocationManager) context.getSystemService(
                Context.LOCATION_SERVICE);
        boolean gpsEnabled;
        boolean networkEnabled;

        if (lm == null) {
            return false;
        }

        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }

        try {
            networkEnabled = lm.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }

        return gpsEnabled || networkEnabled;
    }
}
