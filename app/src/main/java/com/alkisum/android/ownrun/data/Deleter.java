package com.alkisum.android.ownrun.data;

import android.os.AsyncTask;

import com.alkisum.android.ownrun.model.DaoSession;
import com.alkisum.android.ownrun.model.Session;

import java.util.List;

/**
 * Class deleting the selected sessions from the database.
 *
 * @author Alkisum
 * @version 2.0
 * @since 2.0
 */
public class Deleter extends AsyncTask<Void, Void, Void> {

    /**
     * Listener to get notification when the task finishes.
     */
    private final DeleterListener mCallback;

    /**
     * Deleter constructor.
     *
     * @param callback Listener of the task
     */
    public Deleter(final DeleterListener callback) {
        mCallback = callback;
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
        mCallback.onSessionsDeleted();
    }

    /**
     * Listener for the Deleter.
     */
    public interface DeleterListener {

        /**
         * Called when the selected sessions are deleted from the database.
         */
        void onSessionsDeleted();
    }
}
