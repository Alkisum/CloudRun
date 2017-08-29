package com.alkisum.android.cloudrun.events;

/**
 * Class defining pace event for EventBus.
 *
 * @author Alkisum
 * @version 3.0
 * @since 1.3
 */
public class PaceEvent {

    /**
     * Pace value in milliseconds.
     */
    private final long value;

    /**
     * PaceEvent constructor.
     *
     * @param value Pace value in milliseconds
     */
    public PaceEvent(final long value) {
        this.value = value;
    }

    /**
     * @return Pace value in milliseconds
     */
    public final long getValue() {
        return value;
    }
}
