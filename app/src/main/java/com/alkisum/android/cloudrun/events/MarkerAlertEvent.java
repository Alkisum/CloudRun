package com.alkisum.android.cloudrun.events;

import com.alkisum.android.cloudrun.model.Marker;

import java.util.List;

/**
 * Class defining marker alert event for EventBus.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public class MarkerAlertEvent {

    /**
     * Detected markers.
     */
    private List<Marker> markers;

    /**
     * MarkerAlertEvent constructor.
     *
     * @param markers Detected markers
     */
    public MarkerAlertEvent(final List<Marker> markers) {
        this.markers = markers;
    }

    /**
     * @return Detected markers
     */
    public final List<Marker> getMarkers() {
        return this.markers;
    }
}
