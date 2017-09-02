package com.alkisum.android.cloudrun.events;

import com.alkisum.android.cloudlib.events.FilteredEvent;

/**
 * Class defining inserter event for EventBus.
 *
 * @author Alkisum
 * @version 3.0
 * @since 3.0
 */
public class DeleteEvent extends FilteredEvent {

    /**
     * DeleteEvent constructor.
     *
     * @param subscriberIds Subscriber ids allowed to process the events
     */
    public DeleteEvent(final Integer[] subscriberIds) {
        super(subscriberIds);
    }
}