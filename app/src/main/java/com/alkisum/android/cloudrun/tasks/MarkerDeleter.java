package com.alkisum.android.cloudrun.tasks;

import android.os.AsyncTask;

import com.alkisum.android.cloudrun.database.Db;
import com.alkisum.android.cloudrun.events.MarkerDeletedEvent;
import com.alkisum.android.cloudrun.model.DaoSession;
import com.alkisum.android.cloudrun.model.Marker;

import org.greenrobot.eventbus.EventBus;

/**
 * Class deleting the given markers from the database.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public class MarkerDeleter extends AsyncTask<Marker, Void, Marker[]> {

    /**
     * MarkerDeleter constructor.
     */
    public MarkerDeleter() {

    }

    @Override
    protected final Marker[] doInBackground(final Marker... markers) {
        DaoSession daoSession = Db.getInstance().getDaoSession();
        daoSession.getMarkerDao().deleteInTx(markers);
        return markers;
    }

    @Override
    protected final void onPostExecute(final Marker[] markers) {
        EventBus.getDefault().post(new MarkerDeletedEvent(markers));
    }
}
