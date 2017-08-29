package com.alkisum.android.cloudrun.location;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;

import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.dialogs.ErrorDialog;
import com.alkisum.android.cloudrun.events.CoordinateEvent;
import com.alkisum.android.cloudrun.events.DistanceEvent;
import com.alkisum.android.cloudrun.events.PaceEvent;
import com.alkisum.android.cloudrun.events.SpeedEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Class implementing the location handler listener and posting location event
 * through EventBus.
 *
 * @author Alkisum
 * @version 3.0
 * @since 1.0
 */
public class LocationHelper implements LocationHandlerListener {

    /**
     * Log tag.
     */
    private static final String TAG = "LocationHelper";

    /**
     * Request location manually.
     */
    public static final int REQUEST_LOCATION_MANUAL = 359;

    /**
     * LocationHandler instance.
     */
    private final LocationHandler locationHandler;

    /**
     * EventBus instance.
     */
    private final EventBus eventBus;

    /**
     * Activity instance.
     */
    private final Activity activity;

    /**
     * LocationHelper constructor.
     *
     * @param activity Activity
     */
    public LocationHelper(final Activity activity) {
        this.activity = activity;
        eventBus = EventBus.getDefault();
        locationHandler = new LocationHandler(activity, this);
    }

    /**
     * Called when the activity attached to the helper is destroyed.
     */
    public final void onDestroy() {
        if (locationHandler != null) {
            locationHandler.onDestroy();
        }
    }

    /**
     * Start location updates.
     */
    public final void start() {
        locationHandler.startLocationUpdates();
    }

    /**
     * Stop location updates.
     */
    public final void stop() {
        if (locationHandler != null) {
            locationHandler.stopLocationUpdates();
        }
    }

    @Override
    public final void onLocationRequestCreated() {
        locationHandler.buildLocationSettingsRequest();
    }

    @Override
    public final void onLocationSettingsChangeUnavailable() {
        ErrorDialog.build(activity,
                activity.getString(R.string.location_required_title),
                activity.getString(R.string.location_required_message),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        // Open the location settings to able the user
                        // to manually turn the location service on
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        activity.startActivityForResult(intent,
                                REQUEST_LOCATION_MANUAL);
                    }
                }).show();
    }

    @Override
    public final void onNewSpeedValue(final float value) {
        eventBus.post(new SpeedEvent(value));
    }

    @Override
    public final void onNewPaceValue(final long value) {
        eventBus.post(new PaceEvent(value));
    }

    @Override
    public final void onNewDistanceValue(final float value) {
        eventBus.post(new DistanceEvent(value));
    }

    @Override
    public final void onNewCoordinate(final Coordinate coordinate) {
        eventBus.post(new CoordinateEvent(coordinate));
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
