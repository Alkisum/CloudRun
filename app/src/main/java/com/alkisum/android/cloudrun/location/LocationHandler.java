package com.alkisum.android.cloudrun.location;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.alkisum.android.cloudrun.utils.Pref;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Class handling location updates.
 *
 * @author Alkisum
 * @version 3.0
 * @since 1.0
 */
public class LocationHandler implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /**
     * Log tag.
     */
    private static final String TAG = "LocationHandler";

    /**
     * Request location automatically.
     */
    public static final int REQUEST_LOCATION_AUTO = 481;

    /**
     * Location update interval.
     */
    public static final long LOCATION_REQUEST_INTERVAL = 2000;

    /**
     * Location update fastest interval in case another application request
     * a location update with a faster interval.
     */
    private static final long LOCATION_REQUEST_FASTEST_INTERVAL = 1000;

    /**
     * Request code for location.
     */
    private static final int REQUEST_LOCATION = 987;

    /**
     * The accuracy of the location provided by the GPS must under n meters.
     */
    private static final int LOCATION_ACCURACY = 30;

    /**
     * Request code for Play Service resolution.
     */
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 535;

    /**
     * Number of distance values stored in the queue. The higher the value,
     * the smoother the speed or pace calculated.
     */
    public static final int DISTANCE_CNT_DEFAULT = 10;

    /**
     * Queue storing the last distance values with the time passed to travel
     * each distance.
     */
    private final Queue<DistanceWrapper> distanceQueue = new LinkedList<>();

    /**
     * Pending Intent to get data from {@link LocationUpdateService}.
     */
    private PendingIntent pendingIntent;

    /**
     * Flag set to true when the location updates have started, false otherwise.
     */
    private boolean locationUpdatesStarted;

    /**
     * Last location received.
     */
    private Coordinate lastCoordinate;

    /**
     * Time of the last location received.
     */
    private long lastLocationMillis;

    /**
     * Activity instance.
     */
    private final Activity activity;

    /**
     * LocationHandlerListener instance.
     */
    private final LocationHandlerListener callback;

    /**
     * Google API Client.
     */
    private GoogleApiClient googleApiClient;

    /**
     * Location Request instance.
     */
    private LocationRequest locationRequest;

    /**
     * LocationHandler constructor.
     *
     * @param activity Activity instance
     * @param callback LocationHandlerListener instance
     */
    LocationHandler(final Activity activity,
                    final LocationHandlerListener callback) {
        this.activity = activity;
        this.callback = callback;

        // Build and connect GoogleApiClient
        buildGoogleApiClient();
        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
    }

    /**
     * Called when the activity attached to the helper is destroyed.
     */
    final void onDestroy() {
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    /**
     * Build Google Api Client.
     */
    private synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Build the location settings request to check if location is enabled.
     * If it is enabled, start location updates, if it is disabled,
     * show a dialog to enable the location.
     */
    final void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.
                Builder().addLocationRequest(locationRequest)
                .setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull final LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // Location on: start location updates
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Location off: show dialog
                            status.startResolutionForResult(activity,
                                    REQUEST_LOCATION_AUTO);
                        } catch (IntentSender.SendIntentException e) {
                            Log.e(TAG, e.getMessage());
                        }
                        break;
                    case LocationSettingsStatusCodes.
                            SETTINGS_CHANGE_UNAVAILABLE:
                        // There is no way to fix the settings
                        if (LocationHelper.isLocationEnabled(activity)) {
                            // Location on: start location updates
                            startLocationUpdates();
                        } else {
                            // Location off: user must change settings manually
                            callback.onLocationSettingsChangeUnavailable();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * Create location request.
     */
    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        callback.onLocationRequestCreated();
    }

    @Override
    public final void onConnected(final Bundle bundle) {
        createLocationRequest();
    }

    @Override
    public final void onConnectionSuspended(final int i) {
        Log.i(TAG, "onConnectionSuspended");
    }

    @Override
    public final void onConnectionFailed(
            @NonNull final ConnectionResult connectionResult) {
        GoogleApiAvailability googleApiAvailability =
                GoogleApiAvailability.getInstance();
        int code = connectionResult.getErrorCode();
        switch (code) {
            case ConnectionResult.SERVICE_MISSING:
                googleApiAvailability.getErrorDialog(activity, code,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                googleApiAvailability.getErrorDialog(activity, code,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
                break;
            case ConnectionResult.SERVICE_DISABLED:
                googleApiAvailability.getErrorDialog(activity, code,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
                break;
            default:
                Log.e(TAG, "Connection to GoogleApi failed "
                        + "(error " + code + ")");
        }
    }

    /**
     * Start location updates.
     */
    final void startLocationUpdates() {
        Intent intent = new Intent(activity, LocationUpdateService.class);
        pendingIntent = PendingIntent.getService(activity,
                REQUEST_LOCATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission
                .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest
                .permission.ACCESS_COARSE_LOCATION) != PackageManager
                .PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, pendingIntent);
        activity.registerReceiver(mLocationReceiver, new IntentFilter(
                LocationUpdateService.LOCATION_NOTIFICATION));
        locationUpdatesStarted = true;
    }

    /**
     * Stop location updates.
     */
    final void stopLocationUpdates() {
        lastCoordinate = null;
        if (locationUpdatesStarted) {
            activity.unregisterReceiver(mLocationReceiver);
            if (googleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(
                        googleApiClient, pendingIntent);
            }
            locationUpdatesStarted = false;
        }
    }

    /**
     * Save the location in list if the accuracy is high enough.
     *
     * @param location Location to save
     */
    private void saveLocation(final Location location) {
        if (location.getAccuracy() < LOCATION_ACCURACY) {

            long locationMillis = location.getTime();

            // Coordinate
            Coordinate coordinate = new Coordinate(location.getLatitude(),
                    location.getLongitude(), location.getAltitude());
            callback.onNewCoordinate(coordinate);

            if (lastCoordinate != null) {

                // Distance
                float distance = coordinate.distanceTo(lastCoordinate);
                callback.onNewDistanceValue(distance);

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
                    callback.onNewSpeedValue(speed);
                    callback.onNewPaceValue(pace);
                } else {
                    callback.onNewSpeedValue(0);
                    callback.onNewPaceValue(0);
                }
            }
            lastCoordinate = coordinate;
            lastLocationMillis = locationMillis;
        }
    }

    /**
     * Receive notifications from LocationUpdateService.
     */
    private final BroadcastReceiver mLocationReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(final Context context,
                                      final Intent intent) {
                    Bundle bundle = intent.getExtras();
                    if (bundle != null) {
                        android.location.Location location =
                                bundle.getParcelable(
                                        LocationUpdateService.LOCATION);
                        if (location != null) {
                            saveLocation(location);
                        }
                    }
                }
            };

    /**
     * @return Distance count from the SharedPreferences
     */
    private int getDistanceCnt() {
        return PreferenceManager.getDefaultSharedPreferences(activity).getInt(
                Pref.DISTANCE_CNT, DISTANCE_CNT_DEFAULT);
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
