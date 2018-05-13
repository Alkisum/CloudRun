package com.alkisum.android.cloudrun.events;

import com.alkisum.android.cloudlib.events.FilteredEvent;
import com.alkisum.android.cloudrun.model.Route;

import java.util.List;

/**
 * Class defining delete route event for EventBus.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public class DeleteRouteEvent extends FilteredEvent {

    /**
     * Deleted routes.
     */
    private List<Route> deletedRoutes;

    /**
     * DeleteRouteEvent constructor.
     *
     * @param subscriberIds Subscriber ids allowed to process the events
     * @param deletedRoutes Deleted routes
     */
    public DeleteRouteEvent(final Integer[] subscriberIds,
                            final List<Route> deletedRoutes) {
        super(subscriberIds);
        this.deletedRoutes = deletedRoutes;
    }

    /**
     * @return Deleted routes
     */
    public final List<Route> getDeletedRoutes() {
        return deletedRoutes;
    }
}
