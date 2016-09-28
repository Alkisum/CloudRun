package com.alkisum.android.ownrun.location;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.util.Log;

import com.alkisum.android.ownrun.event.CoordinateEvent;
import com.alkisum.android.ownrun.event.DistanceEvent;
import com.alkisum.android.ownrun.event.PaceEvent;
import com.alkisum.android.ownrun.event.SpeedEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Class implementing the location handler listener and posting location event
 * through EventBus.
 *
 * @author Alkisum
 * @version 1.3
 * @since 1.0
 */
public class LocationHelper implements LocationHandlerListener {

    /**
     * Log tag.
     */
    private static final String TAG = "LocationHelper";

    /**
     * LocationHandler instance.
     */
    private final LocationHandler mLocationHandler;

    /**
     * EventBus instance.
     */
    private final EventBus mEventBus;

    /**
     * LocationHelper constructor.
     *
     * @param activity Activity
     */
    public LocationHelper(final Activity activity) {
        mEventBus = EventBus.getDefault();
        mLocationHandler = new LocationHandler(activity, this);
    }

    /**
     * Called when the activity attached to the helper is destroyed.
     */
    public final void onDestroy() {
        if (mLocationHandler != null) {
            mLocationHandler.onDestroy();
        }
    }

    /**
     * Start location updates.
     */
    public final void start() {
        mLocationHandler.startLocationUpdates();
    }

    /**
     * Stop location updates.
     */
    public final void stop() {
        if (mLocationHandler != null) {
            mLocationHandler.stopLocationUpdates();
        }
    }

    @Override
    public final void onLocationRequestCreated() {
        mLocationHandler.buildLocationSettingsRequest();
    }

    @Override
    public final void onNewSpeedValue(final float value) {
        mEventBus.post(new SpeedEvent(value));
    }

    @Override
    public final void onNewPaceValue(final long value) {
        mEventBus.post(new PaceEvent(value));
    }

    @Override
    public final void onNewDistanceValue(final float value) {
        mEventBus.post(new DistanceEvent(value));
    }

    @Override
    public final void onNewCoordinate(final Coordinate coordinate) {
        mEventBus.post(new CoordinateEvent(coordinate));
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
