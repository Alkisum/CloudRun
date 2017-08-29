package com.alkisum.android.cloudrun.event;

/**
 * Class defining distance event for EventBus.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public class DistanceEvent {

    /**
     * Distance value in meter.
     */
    private final float mValue;

    /**
     * DistanceEvent constructor.
     *
     * @param value Distance value in meter
     */
    public DistanceEvent(final float value) {
        mValue = value;
    }

    /**
     * @return Distance value in meter
     */
    public final float getValue() {
        return mValue;
    }
}
