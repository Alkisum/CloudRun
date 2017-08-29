package com.alkisum.android.cloudrun.data;

import android.os.AsyncTask;

import com.alkisum.android.cloudrun.event.DeleteEvent;
import com.alkisum.android.cloudrun.model.DaoSession;
import com.alkisum.android.cloudrun.model.Session;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Class deleting the selected sessions from the database.
 *
 * @author Alkisum
 * @version 3.0
 * @since 2.0
 */
public class Deleter extends AsyncTask<Void, Void, Void> {

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
    protected final Void doInBackground(final Void... voids) {
        DaoSession daoSession = Db.getInstance().getDaoSession();
        List<Session> sessions = Sessions.getSelectedSessions();
        for (Session session : sessions) {
            daoSession.getDataPointDao().deleteInTx(session.getDataPoints());
            session.delete();
        }
        return null;
    }

    @Override
    protected final void onPostExecute(final Void param) {
        EventBus.getDefault().post(new DeleteEvent(subscriberIds));
    }
}
