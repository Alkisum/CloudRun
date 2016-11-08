package com.alkisum.android.ownrun.data;

import com.alkisum.android.ownrun.model.DataPoint;
import com.alkisum.android.ownrun.model.Session;
import com.alkisum.android.ownrun.model.SessionDao;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for session operations.
 *
 * @author Alkisum
 * @version 2.0
 * @since 1.1
 */
public final class Sessions {

    /**
     * Sessions constructor.
     */
    private Sessions() {

    }

    /**
     * Load all the sessions from the database except the one that is currently
     * running (if exists).
     *
     * @param ignoreSessionId ID of the session that should be ignored because
     *                        it is still running. The ID is null if no session
     *                        is running.
     * @return List of sessions in the anti-chronological order
     */
    public static List<Session> loadSessions(final Long ignoreSessionId) {
        SessionDao dao = Db.getInstance().getDaoSession()
                .getSessionDao();
        List<Session> sessions = dao.queryBuilder().orderDesc(
                SessionDao.Properties.Start).list();
        if (ignoreSessionId != null) {
            sessions.remove(dao.load(ignoreSessionId));
        }
        return sessions;
    }

    /**
     * Load all the sessions from the database and return only the selected
     * ones.
     *
     * @return List of selected sessions.
     */
    public static List<Session> getSelectedSessions() {
        SessionDao dao = Db.getInstance().getDaoSession()
                .getSessionDao();
        List<Session> selectedSessions = new ArrayList<>();
        for (Session session : dao.loadAll()) {
            if (session.getSelected()) {
                selectedSessions.add(session);
            }
        }
        return selectedSessions;
    }

    /**
     * Fix the sessions if needed.
     *
     * @param ignoreSessionId ID of the session that should be ignored because
     *                        it is still running. The ID is null if no session
     *                        is running.
     */
    public static void fixSessions(final Long ignoreSessionId) {
        List<Session> sessions = loadSessions(ignoreSessionId);

        for (Session session : sessions) {
            if (session.getEnd() == null) {
                fixSession(session);
            }
        }
    }

    /**
     * Fix the session. Use DataPoints of the session to get the end and to
     * calculate the distance.
     *
     * @param session Session to fix
     */
    private static void fixSession(final Session session) {
        List<DataPoint> dataPoints = session.getDataPoints();

        // Initialize end time and total distance
        Long end = session.getStart();

        if (!dataPoints.isEmpty()) {
            // Get end from the last datapoint recorded during the session
            end = dataPoints.get(dataPoints.size() - 1).getTime();
        }

        // Update database with the new session's information
        session.setEnd(end);
        session.update();
    }
}
