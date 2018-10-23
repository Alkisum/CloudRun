package com.alkisum.android.cloudrun.location;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.database.Markers;
import com.alkisum.android.cloudrun.dialogs.ErrorDialog;
import com.alkisum.android.cloudrun.events.CoordinateEvent;
import com.alkisum.android.cloudrun.events.DistanceEvent;
import com.alkisum.android.cloudrun.events.MarkerAlertEvent;
import com.alkisum.android.cloudrun.events.PaceEvent;
import com.alkisum.android.cloudrun.events.SpeedEvent;
import com.alkisum.android.cloudrun.model.Marker;
import com.alkisum.android.cloudrun.utils.Pref;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Helper class for location operations.
 *
 * @author Alkisum
 * @version 4.0
 * @since 3.1
 */
public class LocationHelper {

    /**
     * Log tag.
     */
    private static final String TAG = "LocationHelper";

    /**
     * Request location automatically.
     */
    public static final int REQUEST_LOCATION_AUTO = 481;

    /**
     * Request location manually.
     */
    public static final int REQUEST_LOCATION_MANUAL = 359;

    /**
     * Location update interval.
     */
    public static final long LOCATION_REQUEST_INTERVAL = 2000;

    /**
     * Location update fastest interval in case another application request
     * a location update with a faster interval.
     */
    private static final long LOCATION_REQUEST_FASTEST_INTERVAL =
            LOCATION_REQUEST_INTERVAL / 2;

    /**
     * The accuracy of the location provided by the GPS must under n meters.
     */
    private static final int LOCATION_ACCURACY = 30;

    /**
     * Number of distance values stored in the queue. The higher the value,
     * the smoother the speed or pace calculated.
     */
    public static final int DISTANCE_CNT_DEFAULT = 10;

    /**
     * Activity instance wrapped into WeakReference object to avoid the activity
     * to be leaked with {@link LocationCallback}.
     */
    private final WeakReference<Activity> activity;

    /**
     * Location Request instance.
     */
    private LocationRequest locationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private final FusedLocationProviderClient fusedLocationClient;


    /**
     * A reference to the service used to get location updates.
     */
    private LocationUpdatesService service = null;

    /**
     * Tracks the bound state of the service.
     */
    private boolean bound = false;

    /**
     * Queue storing the last distance values with the time passed to travel
     * each distance.
     */
    private final Queue<DistanceWrapper> distanceQueue = new LinkedList<>();

    /**
     * Last location received.
     */
    private Coordinate lastCoordinate;

    /**
     * Time of the last location received.
     */
    private long lastLocationMillis;

    /**
     * Flag set to true if the location updates service is running in
     * foreground, false otherwise.
     */
    private boolean runningInForeground = false;

    /**
     * MarkerNotifier instance.
     */
    private final MarkerNotifier markerNotifier;

    /**
     * Set of markers for which the user has already been notified.
     */
    private final Set<Marker> notifiedMarkers = new HashSet<>();

    /**
     * LocationHelper constructor.
     *
     * @param activity Activity
     */
    public LocationHelper(final Activity activity) {
        this.activity = new WeakReference<>(activity);
        markerNotifier = new MarkerNotifier(activity);
        createLocationRequest();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(
                activity);
        activity.bindService(new Intent(activity, LocationUpdatesService.class),
                serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Make the service run in the foreground.
     */
    public final void startForeground() {
        if (bound && !activity.get().isFinishing()) {
            service.startForeground();
            runningInForeground = true;
        }
    }

    /**
     * Remove the service from foreground state.
     */
    public final void stopForeground() {
        if (bound) {
            service.stopForeground();
            runningInForeground = false;
        }
    }

    /**
     * Called when the activity is destroyed.
     */
    public final void onDestroy() {
        removeLocationUpdates();
        markerNotifier.onDestroy();
        if (bound) {
            service.stopSelf();
            activity.get().unbindService(serviceConnection);
            bound = false;
            runningInForeground = false;
        }
    }

    /**
     * Build the location settings request to check if location is enabled.
     * If it is enabled, start location updates, if it is disabled,
     * show a dialog to enable the location.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.
                Builder().addLocationRequest(locationRequest)
                .setAlwaysShow(true);
        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(activity.get())
                        .checkLocationSettings(builder.build());
        result.addOnCompleteListener(
                new OnCompleteListener<LocationSettingsResponse>() {
                    @Override
                    public void onComplete(@NonNull final Task
                            <LocationSettingsResponse> task) {
                        try {
                            task.getResult(ApiException.class);
                            // Location on: start location updates
                            requestLocationUpdates();
                        } catch (ApiException exception) {
                            handleLocationSettingsResponse(exception);
                        }
                    }
                });
    }

    /**
     * Handle location settings response when an exception is thrown.
     *
     * @param exception Thrown exception
     */
    private void handleLocationSettingsResponse(final ApiException exception) {
        switch (exception.getStatusCode()) {
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    // Location off: show dialog
                    ResolvableApiException resolvable = (ResolvableApiException)
                            exception;
                    resolvable.startResolutionForResult(activity.get(),
                            REQUEST_LOCATION_AUTO);
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, e.getMessage());
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // There is no way to fix the settings
                if (LocationUtils.isLocationEnabled(activity.get())) {
                    // Location on: start location updates
                    requestLocationUpdates();
                } else {
                    // Location off: user must change settings manually
                    openLocationSettings();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Create location request.
     */
    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        buildLocationSettingsRequest();
    }

    /**
     * Request location updates and start {@link LocationUpdatesService}.
     */
    public final void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(activity.get(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity.get(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        activity.get().startService(new Intent(
                activity.get().getApplicationContext(),
                LocationUpdatesService.class));
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.myLooper());
        } catch (SecurityException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Removes location updates.
     */
    private void removeLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        } catch (SecurityException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Monitor the state of the connection to the service.
     */
    private final ServiceConnection serviceConnection =
            new ServiceConnection() {

                @Override
                public void onServiceConnected(final ComponentName name,
                                               final IBinder iBinder) {
                    LocationUpdatesService.LocalBinder binder =
                            (LocationUpdatesService.LocalBinder) iBinder;
                    LocationHelper.this.service = binder.getService();
                    bound = true;
                }

                @Override
                public void onServiceDisconnected(final ComponentName name) {
                    service = null;
                    bound = false;
                }
            };

    /**
     * Open location settings to enable the user to manually turn the location
     * service on.
     */
    private void openLocationSettings() {
        ErrorDialog.show(activity.get(),
                activity.get().getString(R.string.location_required_title),
                activity.get().getString(R.string.location_required_message),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        activity.get().startActivityForResult(intent,
                                REQUEST_LOCATION_MANUAL);
                    }
                });
    }

    /**
     * Callback for changes in location.
     */
    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(final LocationResult locationResult) {
            super.onLocationResult(locationResult);
            for (Location location : locationResult.getLocations()) {
                saveLocation(location);
            }
        }
    };

    /**
     * Calculate and save location data if the accuracy is high enough.
     *
     * @param location Location to save
     */
    private void saveLocation(final Location location) {
        if (location.getAccuracy() < LOCATION_ACCURACY) {

            long locationMillis = location.getTime();

            // Coordinate
            Coordinate coordinate = new Coordinate(location.getTime(),
                    location.getLatitude(), location.getLongitude(),
                    location.getAltitude());
            EventBus.getDefault().post(new CoordinateEvent(coordinate));
            handleMarkers(coordinate);

            if (lastCoordinate != null) {

                // Distance
                float distance = coordinate.distanceTo(lastCoordinate);
                EventBus.getDefault().post(new DistanceEvent(distance));

                // Add distance and time to the queue
                long time = locationMillis - lastLocationMillis;
                distanceQueue.add(new DistanceWrapper(distance, time));
                while (distanceQueue.size() > getDistanceCnt()) {
                    distanceQueue.poll();
                }

                // Speed and pace
                float speed = calculateSpeed();
                long pace = calculatePace();
                if (speed > 1) {
                    EventBus.getDefault().post(new SpeedEvent(speed));
                    EventBus.getDefault().post(new PaceEvent(pace));
                } else {
                    EventBus.getDefault().post(new SpeedEvent(0));
                    EventBus.getDefault().post(new PaceEvent(0));
                }
            }
            lastCoordinate = coordinate;
            lastLocationMillis = locationMillis;
        }
    }

    /**
     * Get surrounding markers from the given coordinate and notify the user
     * only once.
     *
     * @param coordinate Current location
     */
    private void handleMarkers(final Coordinate coordinate) {
        // get active markers
        List<Marker> activeMarkers = Markers.getActiveMarkers(activity.get());

        // get surrounding markers
        List<Marker> surroundingMarkers = Markers.getSurroundingMarkers(
                activity.get().getApplicationContext(), coordinate);

        for (Marker activeMarker : activeMarkers) {
            if (surroundingMarkers.contains(activeMarker)
                    && !notifiedMarkers.contains(activeMarker)) {
                // first time being close to the marker
                notifiedMarkers.add(activeMarker);
                EventBus.getDefault().post(
                        new MarkerAlertEvent(surroundingMarkers));
            } else if (!surroundingMarkers.contains(activeMarker)) {
                // not close to the marker anymore
                notifiedMarkers.remove(activeMarker);
            }
        }
    }

    /**
     * @return Distance count from the SharedPreferences
     */
    private int getDistanceCnt() {
        return PreferenceManager.getDefaultSharedPreferences(activity.get())
                .getInt(Pref.DISTANCE_CNT, DISTANCE_CNT_DEFAULT);
    }

    /**
     * Calculate the speed from the distance stored in the queue.
     *
     * @return Speed in km/h
     */
    private float calculateSpeed() {
        float totalDistance = 0;
        long totalTime = 0;
        for (DistanceWrapper distanceWrapper : distanceQueue) {
            totalDistance += distanceWrapper.getDistance();
            totalTime += distanceWrapper.getTime();
        }
        return (totalDistance / 1000f) / (totalTime / 3600000f);
    }

    /**
     * Calculate the pace from the distance stored in the queue.
     *
     * @return Pace in milliseconds
     */
    private long calculatePace() {
        float totalDistance = 0;
        long totalTime = 0;
        for (DistanceWrapper distanceWrapper : distanceQueue) {
            totalDistance += distanceWrapper.getDistance();
            totalTime += distanceWrapper.getTime();
        }
        return Math.round(totalTime / (totalDistance / 1000f));
    }

    /**
     * @return true if the location updates service is running in foreground,
     * false otherwise
     */
    public final boolean isRunningInForeground() {
        return runningInForeground;
    }

    /**
     * Class to wrap a distance with the time passed to travel this distance.
     */
    private class DistanceWrapper {

        /**
         * Distance travelled.
         */
        private final float distance;

        /**
         * Time passed to travel the distance.
         */
        private final long time;

        /**
         * DistanceWrapper constructor.
         *
         * @param distance Distance travelled
         * @param time     Time passed to travel the distance
         */
        DistanceWrapper(final float distance, final long time) {
            this.distance = distance;
            this.time = time;
        }

        /**
         * @return Distance travelled
         */
        final float getDistance() {
            return distance;
        }

        /**
         * @return Time passed to travel the distance
         */
        final long getTime() {
            return time;
        }
    }

}
