package com.alkisum.android.cloudrun.tasks;

import android.os.AsyncTask;

import com.alkisum.android.cloudrun.database.Db;
import com.alkisum.android.cloudrun.events.MarkerRestoredEvent;
import com.alkisum.android.cloudrun.model.DaoSession;
import com.alkisum.android.cloudrun.model.Marker;
import com.alkisum.android.cloudrun.model.MarkerDao;

import org.greenrobot.eventbus.EventBus;

/**
 * Class restoring the deleted markers in the database.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public class MarkerRestorer extends AsyncTask<Marker, Void, Void> {

    /**
     * MarkerRestorer constructor.
     */
    public MarkerRestorer() {

    }

    @Override
    protected final Void doInBackground(final Marker... markers) {
        DaoSession daoSession = Db.getInstance().getDaoSession();
        MarkerDao markerDao = daoSession.getMarkerDao();
        markerDao.insertInTx(markers);
        return null;
    }

    @Override
    protected final void onPostExecute(final Void param) {
        EventBus.getDefault().post(new MarkerRestoredEvent());
    }
}
