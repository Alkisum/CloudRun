package com.alkisum.android.ownrun.location;

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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

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

/**
 * Class handling location updates.
 *
 * @author Alkisum
 * @version 1.0
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
     * Request check settings.
     */
    public static final int REQUEST_CHECK_SETTINGS = 481;

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
     * Pending Intent to get data from {@link LocationUpdateService}.
     */
    private PendingIntent mPendingIntent;

    /**
     * Flag set to true when the location updates have started, false otherwise.
     */
    private boolean mLocationUpdatesStarted;

    /**
     * Last location received.
     */
    private Coordinate mLastCoordinate;

    /**
     * Time of the last location received.
     */
    private long mLastLocationMillis;

    /**
     * Kalman filter instance.
     */
    private KalmanFilter mKalmanFilter;

    /**
     * Activity instance.
     */
    private final Activity mActivity;

    /**
     * LocationHandlerListener instance.
     */
    private final LocationHandlerListener mCallback;

    /**
     * Google API Client.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Location Request instance.
     */
    private LocationRequest mLocationRequest;

    /**
     * LocationHandler constructor.
     *
     * @param activity Activity instance
     * @param callback LocationHandlerListener instance
     */
    public LocationHandler(final Activity activity,
                           final LocationHandlerListener callback) {
        mActivity = activity;
        mCallback = callback;

        // Build and connect GoogleApiClient
        buildGoogleApiClient();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Called when the activity attached to the helper is destroyed.
     */
    public final void onDestroy() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Build Google Api Client.
     */
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
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
    public final void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.
                Builder().addLocationRequest(mLocationRequest)
                .setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull final LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(mActivity,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.e(TAG, e.getMessage());
                        }
                        break;
                    case LocationSettingsStatusCodes.
                            SETTINGS_CHANGE_UNAVAILABLE:
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
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mCallback.onLocationRequestCreated();
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
                googleApiAvailability.getErrorDialog(mActivity, code,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                googleApiAvailability.getErrorDialog(mActivity, code,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
                break;
            case ConnectionResult.SERVICE_DISABLED:
                googleApiAvailability.getErrorDialog(mActivity, code,
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
    public final void startLocationUpdates() {
        Intent intent = new Intent(mActivity, LocationUpdateService.class);
        mPendingIntent = PendingIntent.getService(mActivity,
                REQUEST_LOCATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission
                .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mActivity, Manifest
                .permission.ACCESS_COARSE_LOCATION) != PackageManager
                .PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, mPendingIntent);
        mActivity.registerReceiver(mLocationReceiver, new IntentFilter(
                LocationUpdateService.LOCATION_NOTIFICATION));
        mLocationUpdatesStarted = true;

        mKalmanFilter = new KalmanFilter(3f);
    }

    /**
     * Stop location updates.
     */
    public final void stopLocationUpdates() {
        mLastCoordinate = null;
        if (mLocationUpdatesStarted) {
            mActivity.unregisterReceiver(mLocationReceiver);
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(
                        mGoogleApiClient, mPendingIntent);
            }
            mLocationUpdatesStarted = false;
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

            // Apply Kalman filter
            mKalmanFilter.process(location.getLatitude(),
                    location.getLongitude(), location.getAccuracy(),
                    locationMillis);
            // Store calculated location
            Coordinate coordinate = new Coordinate(mKalmanFilter.getLatitude(),
                    mKalmanFilter.getLongitude(), location.getAltitude());
            mCallback.onNewCoordinate(coordinate);

            if (mLastCoordinate != null) {

                // Distance
                float distance = coordinate.distanceTo(mLastCoordinate);
                mCallback.onNewDistanceValue(distance);

                // Speed / Pace
                if (!location.hasSpeed()) {
                    long interval = locationMillis - mLastLocationMillis;
                    // Calculate speed
                    float speed = (distance / 1000f) / (interval / 3600000f);
                    if (speed > 1) {
                        mCallback.onNewSpeedValue(speed);
                        mCallback.onNewPaceValue(
                                Math.round(interval / (distance / 1000f)));
                    } else {
                        mCallback.onNewSpeedValue(0);
                        mCallback.onNewPaceValue(0);
                    }
                } else {
                    // Get speed from GPS
                    float speed = location.getSpeed() * 3.6f;
                    if (speed > 1) {
                        mCallback.onNewSpeedValue(speed);
                        mCallback.onNewPaceValue(
                                Math.round((60 / speed) * 60000));
                    } else {
                        mCallback.onNewSpeedValue(0);
                        mCallback.onNewPaceValue(0);
                    }
                }
            }
            mLastCoordinate = coordinate;
            mLastLocationMillis = locationMillis;
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
}
