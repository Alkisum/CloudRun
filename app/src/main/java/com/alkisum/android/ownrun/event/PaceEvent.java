package com.alkisum.android.ownrun.event;

/**
 * Class defining pace event for EventBus.
 *
 * @author Alkisum
 * @version 1.3
 * @since 1.3
 */
public class PaceEvent {

    /**
     * Pace value in milliseconds.
     */
    private final long mValue;

    /**
     * PaceEvent constructor.
     *
     * @param value Pace value in milliseconds
     */
    public PaceEvent(final long value) {
        mValue = value;
    }

    /**
     * @return Pace value in milliseconds
     */
    public final long getValue() {
        return mValue;
    }
}
