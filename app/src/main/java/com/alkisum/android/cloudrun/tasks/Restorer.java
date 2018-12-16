package com.alkisum.android.cloudrun.tasks;

import android.os.AsyncTask;

import com.alkisum.android.cloudrun.events.RestoredEvent;
import com.alkisum.android.cloudrun.interfaces.Restorable;

import org.greenrobot.eventbus.EventBus;

/**
 * Class restoring the deleted entities in the database.
 *
 * @author Alkisum
 * @version 4.0
 * @since 3.0
 */
public class Restorer extends AsyncTask<Restorable, Void, Void> {

    /**
     * Restorable entity to restore.
     */
    private final Restorable restorable;

    /**
     * Restorer constructor.
     *
     * @param restorable Restorable entity to restore
     */
    public Restorer(final Restorable restorable) {
        this.restorable = restorable;
    }

    @Override
    protected final Void doInBackground(final Restorable... restorables) {
        restorable.restore(restorables);
        return null;
    }

    @Override
    protected final void onPostExecute(final Void param) {
        EventBus.getDefault().post(new RestoredEvent(restorable));
    }
}
