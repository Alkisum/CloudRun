package com.alkisum.android.cloudrun.events;

import com.alkisum.android.cloudrun.model.Marker;

/**
 * Class defining delete marker event for EventBus.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public class DeleteMarkerEvent {

    /**
     * Deleted markers.
     */
    private final Marker[] deletedMarkers;

    /**
     * DeleteMarkerEvent constructor.
     *
     * @param deletedMarkers Deleted markers
     */
    public DeleteMarkerEvent(final Marker[] deletedMarkers) {
        this.deletedMarkers = deletedMarkers;
    }

    /**
     * @return Deleted markers
     */
    public final Marker[] getDeletedMarkers() {
        return deletedMarkers;
    }
}
