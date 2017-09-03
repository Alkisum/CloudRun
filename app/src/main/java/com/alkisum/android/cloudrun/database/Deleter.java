package com.alkisum.android.cloudrun.database;

import android.os.AsyncTask;

import com.alkisum.android.cloudrun.events.DeleteEvent;
import com.alkisum.android.cloudrun.model.DaoSession;
import com.alkisum.android.cloudrun.model.Session;
import com.alkisum.android.cloudrun.utils.Sessions;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Class deleting the selected sessions from the database.
 *
 * @author Alkisum
 * @version 3.0
 * @since 2.0
 */
public class Deleter extends AsyncTask<Void, Void, List<Session>> {

    /**
     * Subscriber ids allowed to process the events.
     */
    private final Integer[] subscriberIds;

    /**
     * Deleter constructor.
     *
     * @param subscriberIds Subscriber ids allowed to process the events
     */
    public Deleter(final Integer[] subscriberIds) {
        this.subscriberIds = subscriberIds;
    }

    @Override
    protected final List<Session> doInBackground(final Void... voids) {
        DaoSession daoSession = Db.getInstance().getDaoSession();
        List<Session> sessions = Sessions.getSelectedSessions();
        for (Session session : sessions) {
            daoSession.getDataPointDao().deleteInTx(session.getDataPoints());
            session.delete();
        }
        return sessions;
    }

    @Override
    protected final void onPostExecute(final List<Session> sessions) {
        EventBus.getDefault().post(new DeleteEvent(subscriberIds, sessions));
    }
}
