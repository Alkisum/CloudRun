package com.alkisum.android.cloudrun.events;

/**
 * Class defining speed event for EventBus.
 *
 * @author Alkisum
 * @version 3.0
 * @since 1.3
 */
public class SpeedEvent {

    /**
     * Speed value in km/h.
     */
    private final float value;

    /**
     * SpeedEvent constructor.
     *
     * @param value Speed value in km/h
     */
    public SpeedEvent(final float value) {
        this.value = value;
    }

    /**
     * @return Speed value in km/h
     */
    public final float getValue() {
        return value;
    }
}
