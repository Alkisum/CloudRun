package com.alkisum.android.cloudrun.database;

import android.os.AsyncTask;

import com.alkisum.android.cloudrun.events.RestoreEvent;
import com.alkisum.android.cloudrun.model.DaoSession;
import com.alkisum.android.cloudrun.model.DataPointDao;
import com.alkisum.android.cloudrun.model.Session;
import com.alkisum.android.cloudrun.model.SessionDao;

import org.greenrobot.eventbus.EventBus;

/**
 * Class restoring the deleted sessions in the database.
 *
 * @author Alkisum
 * @version 3.0
 * @since 3.0
 */
public class Restorer extends AsyncTask<Session, Void, Void> {

    /**
     * Restorer constructor.
     */
    public Restorer() {
    }

    @Override
    protected final Void doInBackground(final Session... sessions) {
        DaoSession daoSession = Db.getInstance().getDaoSession();
        SessionDao sessionDao = daoSession.getSessionDao();
        DataPointDao dataPointDao = daoSession.getDataPointDao();
        for (Session session : sessions) {
            sessionDao.insert(session);
            dataPointDao.insertInTx(session.getDataPoints());
        }
        return null;
    }

    @Override
    protected final void onPostExecute(final Void param) {
        EventBus.getDefault().post(new RestoreEvent());
    }
}
