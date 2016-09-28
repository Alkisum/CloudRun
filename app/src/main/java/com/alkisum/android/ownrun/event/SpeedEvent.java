package com.alkisum.android.ownrun.event;

/**
 * Class defining speed event for EventBus.
 *
 * @author Alkisum
 * @version 1.3
 * @since 1.3
 */
public class SpeedEvent {

    /**
     * Speed value in km/h.
     */
    private final float mValue;

    /**
     * SpeedEvent constructor.
     *
     * @param value Speed value in km/h
     */
    public SpeedEvent(final float value) {
        mValue = value;
    }

    /**
     * @return Speed value in km/h
     */
    public final float getValue() {
        return mValue;
    }
}
