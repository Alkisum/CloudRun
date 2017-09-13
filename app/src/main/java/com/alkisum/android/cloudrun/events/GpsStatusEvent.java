package com.alkisum.android.cloudrun.events;

/**
 * Class defining GPS status event for EventBus.
 *
 * @author Alkisum
 * @version 3.0
 * @since 3.0
 */
public class GpsStatusEvent {

    /**
     * GPS status icon.
     */
    private final int icon;

    /**
     * GpsStatusEvent constructor.
     *
     * @param icon GPS status icon
     */
    public GpsStatusEvent(final int icon) {
        this.icon = icon;
    }

    /**
     * @return GPS status icon
     */
    public final int getIcon() {
        return icon;
    }
}
