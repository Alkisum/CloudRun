package com.alkisum.android.cloudrun.events;

/**
 * Class defining distance event for EventBus.
 *
 * @author Alkisum
 * @version 3.0
 * @since 1.0
 */
public class DistanceEvent {

    /**
     * Distance value in meter.
     */
    private final float value;

    /**
     * DistanceEvent constructor.
     *
     * @param value Distance value in meter
     */
    public DistanceEvent(final float value) {
        this.value = value;
    }

    /**
     * @return Distance value in meter
     */
    public final float getValue() {
        return value;
    }
}
