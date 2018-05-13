package com.alkisum.android.cloudrun.events;

import com.alkisum.android.cloudlib.events.FilteredEvent;
import com.alkisum.android.cloudrun.model.Session;

import java.util.List;

/**
 * Class defining delete session event for EventBus.
 *
 * @author Alkisum
 * @version 4.0
 * @since 3.0
 */
public class DeleteSessionEvent extends FilteredEvent {

    /**
     * Deleted sessions.
     */
    private List<Session> deletedSessions;

    /**
     * DeleteSessionEvent constructor.
     *
     * @param subscriberIds   Subscriber ids allowed to process the events
     * @param deletedSessions Deleted sessions
     */
    public DeleteSessionEvent(final Integer[] subscriberIds,
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
