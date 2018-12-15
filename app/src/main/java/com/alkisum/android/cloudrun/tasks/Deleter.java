package com.alkisum.android.cloudrun.tasks;

import android.os.AsyncTask;

import com.alkisum.android.cloudrun.events.DeletedEvent;
import com.alkisum.android.cloudrun.interfaces.Deletable;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Class deleting the selected entities from the database.
 *
 * @author Alkisum
 * @version 4.0
 * @since 2.0
 */
public class Deleter
        extends AsyncTask<Void, Void, List<? extends Deletable>> {

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
    protected final List<? extends Deletable> doInBackground(
            final Void... voids) {
        return deletable.deleteSelected();
    }

    @Override
    protected final void onPostExecute(
            final List<? extends Deletable> deletables) {
        EventBus.getDefault().post(new DeletedEvent(subscriberIds, deletable,
                deletables));
    }
}
