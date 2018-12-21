package com.alkisum.android.cloudrun.tasks;

import android.os.AsyncTask;

import com.alkisum.android.cloudrun.events.DeletedEvent;
import com.alkisum.android.cloudrun.interfaces.Deletable;

import org.greenrobot.eventbus.EventBus;

/**
 * Class deleting the selected entities from the database.
 *
 * @author Alkisum
 * @version 4.0
 * @since 2.0
 */
public class Deleter extends AsyncTask<Deletable, Void, Deletable[]> {

    /**
     * Subscriber ids allowed to process the events.
     */
    private final Integer[] subscriberIds;

    /**
     * Deletable entity to delete.
     */
    private final Deletable deletable;

    /**
     * Deleter constructor.
     *
     * @param subscriberIds Subscriber ids allowed to process the events
     * @param deletable     Deletable entity to delete
     */
    public Deleter(final Integer[] subscriberIds,
                   final Deletable deletable) {
        this.subscriberIds = subscriberIds;
        this.deletable = deletable;
    }

    @Override
    protected final Deletable[] doInBackground(final Deletable... deletables) {
        return deletable.deleteEntities(deletables);
    }

    @Override
    protected final void onPostExecute(final Deletable[] deletables) {
        EventBus.getDefault().post(new DeletedEvent(subscriberIds, deletable,
                deletables));
    }
}
