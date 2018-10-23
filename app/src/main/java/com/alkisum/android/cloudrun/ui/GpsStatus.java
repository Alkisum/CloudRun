package com.alkisum.android.cloudrun.ui;

import android.content.Context;
import android.os.Handler;

import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.events.CoordinateEvent;
import com.alkisum.android.cloudrun.events.GpsStatusEvent;
import com.alkisum.android.cloudrun.location.LocationHelper;
import com.alkisum.android.cloudrun.location.LocationUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Class to handle the GPS status view show on toolbars.
 *
 * @author Alkisum
 * @version 4.0
 * @since 3.0
 */
public class GpsStatus {

    /**
     * Context.
     */
    private final Context context;

    /**
     * Handler for the task checking for the GPS status.
     */
    private final Handler gpsStatusHandler = new Handler();

    /**
     * Flag set to true when new GPS data has been received, false otherwise.
     */
    private boolean newGpsDataReceived;

    /**
     * EventBus instance.
     */
    private final EventBus eventBus;

    /**
     * Last GPS status icon.
     */
    private static int lastIcon = R.drawable.ic_gps_off_white_24dp;

    /**
     * GpsStatus constructor.
     *
     * @param context Context
     */
    public GpsStatus(final Context context) {
        this.context = context;
        eventBus = EventBus.getDefault();
    }

    /**
     * Start monitoring GPS to get status.
     */
    public final void start() {
        eventBus.register(this);
        gpsStatusHandler.postDelayed(gpsStatusTask,
                LocationHelper.LOCATION_REQUEST_INTERVAL * 2);
    }

    /**
     * Stop monitoring GPS to get status.
     */
    public final void stop() {
        gpsStatusHandler.removeCallbacks(gpsStatusTask);
        eventBus.unregister(this);
    }

    /**
     * Task sending callback with icon according to the GPS status.
     */
    private final Runnable gpsStatusTask = new Runnable() {
        @Override
        public void run() {
            gpsStatusHandler.postDelayed(this,
                    LocationHelper.LOCATION_REQUEST_INTERVAL * 2);
            lastIcon = getIcon();
            eventBus.post(new GpsStatusEvent(lastIcon));
            newGpsDataReceived = false;
        }
    };

    /**
     * Check GPS status and return GPS status icon accordingly.
     *
     * @return Drawable id
     */
    private int getIcon() {
        if (!LocationUtils.isLocationEnabled(context)) {
            return R.drawable.ic_gps_off_white_24dp;
        } else if (newGpsDataReceived) {
            return R.drawable.ic_gps_fixed_white_24dp;
        } else {
            return R.drawable.ic_gps_not_fixed_white_24dp;
        }
    }

    /**
     * @return Last GPS status icon
     */
    public static int getLastIcon() {
        return lastIcon;
    }

    /**
     * Triggered when new coordinates are received.
     *
     * @param event Coordinate event
     */
    @Subscribe
    public final void onCoordinateEvent(final CoordinateEvent event) {
        newGpsDataReceived = true;
    }
}
