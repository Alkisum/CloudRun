package com.alkisum.android.cloudrun.events;

import com.alkisum.android.cloudlib.events.FilteredEvent;
import com.alkisum.android.cloudrun.interfaces.Deletable;

import java.util.List;

/**
 * Class defining deleted event for EventBus.
 *
 * @author Alkisum
 * @version 4.0
 * @since 3.0
 */
public class DeletedEvent extends FilteredEvent {

    /**
     * Deletable entity deleted.
     */
    private final Deletable deletable;

    /**
     * Deleted entities.
     */
    private final List<? extends Deletable> deletedEntities;

    /**
     * DeletedEvent constructor.
     *
     * @param subscriberIds   Subscriber ids allowed to process the events
     * @param deletable       Deletable entity deleted
     * @param deletedEntities Deleted entities
     */
    public DeletedEvent(final Integer[] subscriberIds,
                        final Deletable deletable,
                        final List<? extends Deletable> deletedEntities) {
        super(subscriberIds);
        this.deletable = deletable;
        this.deletedEntities = deletedEntities;
    }

    /**
     * @return Deletable entity deleted
     */
    public final Deletable getDeletable() {
        return this.deletable;
    }

    /**
     * @return Deleted entities
     */
    public final List<? extends Deletable> getDeletedEntities() {
        return deletedEntities;
    }
}
