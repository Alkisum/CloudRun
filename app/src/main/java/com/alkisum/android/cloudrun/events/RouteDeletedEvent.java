package com.alkisum.android.cloudrun.events;

import com.alkisum.android.cloudlib.events.FilteredEvent;
import com.alkisum.android.cloudrun.model.Route;

import java.util.List;

/**
 * Class defining route deleted event for EventBus.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public class RouteDeletedEvent extends FilteredEvent {

    /**
     * Deleted routes.
     */
    private final List<Route> deletedRoutes;

    /**
     * RouteDeletedEvent constructor.
     *
     * @param subscriberIds Subscriber ids allowed to process the events
     * @param deletedRoutes Deleted routes
     */
    public RouteDeletedEvent(final Integer[] subscriberIds,
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
