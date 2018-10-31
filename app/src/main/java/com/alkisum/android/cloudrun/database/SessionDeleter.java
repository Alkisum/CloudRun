package com.alkisum.android.cloudrun.database;

import android.os.AsyncTask;

import com.alkisum.android.cloudrun.events.SessionDeletedEvent;
import com.alkisum.android.cloudrun.model.DaoSession;
import com.alkisum.android.cloudrun.model.Session;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Class deleting the selected sessions from the database.
 *
 * @author Alkisum
 * @version 4.0
 * @since 2.0
 */
public class SessionDeleter extends AsyncTask<Void, Void, List<Session>> {

    /**
     * Subscriber ids allowed to process the events.
     */
    private final Integer[] subscriberIds;

    /**
     * SessionDeleter constructor.
     *
     * @param subscriberIds Subscriber ids allowed to process the events
     */
    public SessionDeleter(final Integer[] subscriberIds) {
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
        EventBus.getDefault().post(new SessionDeletedEvent(subscriberIds,
                sessions));
    }
}
