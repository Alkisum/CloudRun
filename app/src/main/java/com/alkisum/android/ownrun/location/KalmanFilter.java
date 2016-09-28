package com.alkisum.android.ownrun.location;

/**
 * Kalman filter for latitude and longitude from
 * http://stackoverflow.com/questions/1134579/smooth-gps-data/15657798#15657798.
 *
 * @author Alkisum
 * @version 1.3
 * @since 1.0
 */
class KalmanFilter {

    /**
     * Meters per second given when constructing the object.
     */
    private final float mMetersPerSecond;

    /**
     * Milliseconds.
     */
    private long mMillis;

    /**
     * Latitude.
     */
    private double mLatitude;

    /**
     * Longitude.
     */
    private double mLongitude;

    /**
     * Variance.
     * P matrix.  Negative means object uninitialised.
     * NB: units irrelevant, as long as same units used throughout.
     */
    private float variance;

    /**
     * KalmanFilter constructor.
     *
     * @param mps Meters per second
     */
    KalmanFilter(final float mps) {
        mMetersPerSecond = mps;
        variance = -1;
    }

    /**
     * @return Latitude
     */
    final double getLatitude() {
        return mLatitude;
    }

    /**
     * @return Longitude
     */
    final double getLongitude() {
        return mLongitude;
    }

    /**
     * Kalman filter processing for latitude and longitude.
     *
     * @param latitude    New measurement of latitude
     * @param longitude   New measurement of longitude
     * @param newAccuracy Measurement of 1 standard deviation error in meters
     * @param millis      Time of measurement
     */
    final void process(final double latitude, final double longitude,
                       final float newAccuracy, final long millis) {
        float accuracy = newAccuracy;
        if (accuracy < 1) {
            accuracy = 1;
        }
        if (variance < 0) {
            // if variance < 0, object is not initialised, so initialise with
            // current values
            mMillis = millis;
            mLatitude = latitude;
            mLongitude = longitude;
            variance = accuracy * accuracy;
        } else {
            // else apply Kalman filter methodology

            long timeIncMillis = millis - mMillis;
            if (timeIncMillis > 0) {
                // time has moved on, so the uncertainty in the current
                // position increases
                variance += timeIncMillis * mMetersPerSecond
                        * mMetersPerSecond / 1000;
                mMillis = timeIncMillis;
                // TO DO: USE VELOCITY INFORMATION HERE TO GET A BETTER
                // ESTIMATE OF CURRENT POSITION
            }

            // Kalman gain matrix K = Covariance * Inverse(Covariance +
            // MeasurementVariance)
            // NB: because K is dimensionless, it doesn't matter that
            // variance has different units to lat and lng
            float k = variance / (variance + accuracy * accuracy);
            // apply K
            mLatitude += k * (latitude - mLatitude);
            mLongitude += k * (longitude - mLongitude);
            // new Covariance  matrix is (IdentityMatrix - K) * Covariance
            variance = (1 - k) * variance;
        }
    }
}
