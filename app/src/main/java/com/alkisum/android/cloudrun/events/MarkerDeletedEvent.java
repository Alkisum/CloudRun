package com.alkisum.android.cloudrun.events;

import com.alkisum.android.cloudrun.model.Marker;

/**
 * Class defining marker deleted event for EventBus.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public class MarkerDeletedEvent {

    /**
     * Deleted markers.
     */
    private final Marker[] deletedMarkers;

    /**
     * MarkerDeletedEvent constructor.
     *
     * @param deletedMarkers Deleted markers
     */
    public MarkerDeletedEvent(final Marker[] deletedMarkers) {
        this.deletedMarkers = deletedMarkers;
    }

    /**
     * @return Deleted markers
     */
    public final Marker[] getDeletedMarkers() {
        return deletedMarkers;
    }
}
