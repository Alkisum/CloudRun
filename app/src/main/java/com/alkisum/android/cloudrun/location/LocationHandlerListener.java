package com.alkisum.android.cloudrun.location;

/**
 * Listener for LocationHandler.
 *
 * @author Alkisum
 * @version 3.0
 * @since 1.0
 */
interface LocationHandlerListener {

    /**
     * Called when the LocationRequest has been created. After this, it is
     * allowed to build the LocationSettingsRequest.
     */
    void onLocationRequestCreated();

    /**
     * Called when the location is off and there is no way to change the
     * settings automatically.
     */
    void onLocationSettingsChangeUnavailable();

    /**
     * A new speed value has been received.
     *
     * @param value Speed value
     */
    void onNewSpeedValue(float value);

    /**
     * A new pace value has been received.
     *
     * @param value Pace value
     */
    void onNewPaceValue(long value);

    /**
     * A new distance value has been received.
     *
     * @param value Distance value
     */
    void onNewDistanceValue(float value);

    /**
     * New Coordinate values have been received.
     *
     * @param coordinate Coordinate values
     */
    void onNewCoordinate(Coordinate coordinate);
}
