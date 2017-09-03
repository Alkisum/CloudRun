package com.alkisum.android.cloudrun.events;

import com.alkisum.android.cloudlib.events.FilteredEvent;
import com.alkisum.android.cloudrun.model.Session;

import java.util.ArrayList;
import java.util.List;

/**
 * Class defining delete event for EventBus.
 *
 * @author Alkisum
 * @version 3.0
 * @since 3.0
 */
public class DeleteEvent extends FilteredEvent {

    /**
     * Deleted sessions.
     */
    private List<Session> deletedSessions = new ArrayList<>();

    /**
     * DeleteEvent constructor.
     *
     * @param subscriberIds   Subscriber ids allowed to process the events
     * @param deletedSessions Deleted sessions
     */
    public DeleteEvent(final Integer[] subscriberIds,
                       final List<Session> deletedSessions) {
        super(subscriberIds);
        this.deletedSessions = deletedSessions;
    }

    /**
     * @return Deleted sessions
     */
    public final List<Session> getDeletedSessions() {
        return deletedSessions;
    }
}
