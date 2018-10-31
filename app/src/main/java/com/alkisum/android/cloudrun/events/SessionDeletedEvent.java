package com.alkisum.android.cloudrun.events;

import com.alkisum.android.cloudlib.events.FilteredEvent;
import com.alkisum.android.cloudrun.model.Session;

import java.util.List;

/**
 * Class defining session deleted event for EventBus.
 *
 * @author Alkisum
 * @version 4.0
 * @since 3.0
 */
public class SessionDeletedEvent extends FilteredEvent {

    /**
     * Deleted sessions.
     */
    private final List<Session> deletedSessions;

    /**
     * SessionDeletedEvent constructor.
     *
     * @param subscriberIds   Subscriber ids allowed to process the events
     * @param deletedSessions Deleted sessions
     */
    public SessionDeletedEvent(final Integer[] subscriberIds,
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
