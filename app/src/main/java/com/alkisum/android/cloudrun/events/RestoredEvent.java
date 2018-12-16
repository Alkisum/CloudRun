package com.alkisum.android.cloudrun.events;

import com.alkisum.android.cloudrun.interfaces.Restorable;

/**
 * Class defining restored event for EventBus.
 *
 * @author Alkisum
 * @version 4.0
 * @since 3.0
 */
public class RestoredEvent {

    /**
     * Restorable entity restored.
     */
    private final Restorable restorable;

    /**
     * RestoredEvent constructor.
     *
     * @param restorable Restorable entity restored
     */
    public RestoredEvent(final Restorable restorable) {
        this.restorable = restorable;
    }

    /**
     * @return Restorable entity restored
     */
    public final Restorable getRestorable() {
        return this.restorable;
    }
}
