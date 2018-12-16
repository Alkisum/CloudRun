package com.alkisum.android.cloudrun.utils;

import com.alkisum.android.cloudlib.file.json.JsonFile;
import com.alkisum.android.cloudrun.database.Db;
import com.alkisum.android.cloudrun.model.DaoSession;
import com.alkisum.android.cloudrun.model.DataPoint;
import com.alkisum.android.cloudrun.model.DataPointDao;
import com.alkisum.android.cloudrun.model.Session;
import com.alkisum.android.cloudrun.model.SessionDao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for session operations.
 *
 * @author Alkisum
 * @version 4.0
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
        SessionDao dao = Db.getInstance().getDaoSession().getSessionDao();
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
        SessionDao dao = Db.getInstance().getDaoSession().getSessionDao();
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
     * Load the session from the database according to the given ID.
     *
     * @param sessionId Session id
     * @return Session
     */
    public static Session getSessionById(final long sessionId) {
        SessionDao dao = Db.getInstance().getDaoSession().getSessionDao();
        return dao.load(sessionId);
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

    /**
     * Retrieve the last session stored in the database.
     *
     * @return Last session
     */
    public static Session getLastSession() {
        SessionDao dao = Db.getInstance().getDaoSession().getSessionDao();
        List<Session> sessions = dao.queryBuilder().orderDesc(
                SessionDao.Properties.Start).limit(1).list();
        if (sessions != null && !sessions.isEmpty()) {
            return sessions.get(0);
        }
        return null;
    }

    /**
     * Build JSON object from the given session.
     *
     * @param session Session to build the JSON object from
     * @return JSON object
     * @throws JSONException Exception thrown while building JSON object
     */
    public static JSONObject buildJson(final Session session)
            throws JSONException {
        // build JSON object for session
        JSONObject jsonSession = new JSONObject();
        jsonSession.put(Json.SESSION_START, session.getStart());
        jsonSession.put(Json.SESSION_END, session.getEnd());
        jsonSession.put(Json.SESSION_DURATION, session.getDuration());
        jsonSession.put(Json.SESSION_DISTANCE, session.getDistance());

        // build JSON array for datapoints
        JSONArray jsonDataPoints = new JSONArray();
        List<DataPoint> dataPoints = session.getDataPoints();
        for (DataPoint dataPoint : dataPoints) {
            JSONObject jsonDataPoint = new JSONObject();
            jsonDataPoint.put(Json.DATAPOINT_TIME, dataPoint.getTime());
            jsonDataPoint.put(Json.DATAPOINT_LATITUDE, dataPoint.getLatitude());
            jsonDataPoint.put(Json.DATAPOINT_LONGITUDE,
                    dataPoint.getLongitude());
            jsonDataPoint.put(Json.DATAPOINT_ELEVATION,
                    dataPoint.getElevation());
            jsonDataPoints.put(jsonDataPoint);
        }

        // build final JSON object
        JSONObject jsonBase = new JSONObject();
        jsonBase.put(Json.VERSION, Json.JSON_VERSION);
        jsonBase.put(Json.SESSION, jsonSession);
        jsonBase.put(Json.DATAPOINTS, jsonDataPoints);

        return jsonBase;
    }

    /**
     * Build session from Json file version 1.
     *
     * @param jsonBase JSONObject the structure is based on
     * @throws JSONException An error occurred while parsing the JSON object
     */
    public static void buildFromJsonVersion1(final JSONObject jsonBase)
            throws JSONException {
        // get DAOs
        DaoSession daoSession = Db.getInstance().getDaoSession();
        SessionDao sessionDao = daoSession.getSessionDao();
        DataPointDao dataPointDao = daoSession.getDataPointDao();

        // build session
        JSONObject jsonSession = jsonBase.getJSONObject(Json.SESSION);
        Session session = new Session();
        long start = jsonSession.getLong(Json.SESSION_START);
        long end = jsonSession.getLong(Json.SESSION_END);
        session.setStart(start);
        session.setEnd(end);
        session.setDuration(end - start);
        session.setDistance(BigDecimal.valueOf(jsonSession.getDouble(
                Json.SESSION_DISTANCE)).floatValue());

        // insert session
        daoSession.insert(session);

        // build datapoints
        List<DataPoint> dataPoints = new ArrayList<>();
        JSONArray jsonDataPoints = jsonBase.getJSONArray(Json.DATAPOINTS);
        for (int i = 0; i < jsonDataPoints.length(); i++) {
            JSONObject jsonDataPoint = jsonDataPoints.getJSONObject(i);
            DataPoint dataPoint = new DataPoint();
            dataPoint.setTime(jsonDataPoint.getLong(
                    Json.DATAPOINT_TIME));
            dataPoint.setLatitude(jsonDataPoint.getDouble(
                    Json.DATAPOINT_LATITUDE));
            dataPoint.setLongitude(jsonDataPoint.getDouble(
                    Json.DATAPOINT_LONGITUDE));
            dataPoint.setElevation(jsonDataPoint.getDouble(
                    Json.DATAPOINT_ELEVATION));
            dataPoint.setSession(session);
            dataPoints.add(dataPoint);
        }

        // insert datapoints
        dataPointDao.insertInTx(dataPoints);
    }

    /**
     * Build session from Json file version 2. Changes:
     * - The duration is written in the session object
     *
     * @param jsonBase JSONObject the structure is based on
     * @throws JSONException An error occurred while parsing the JSON object
     */
    public static void buildFromJsonVersion2(final JSONObject jsonBase)
            throws JSONException {
        // get DAOs
        DaoSession daoSession = Db.getInstance().getDaoSession();
        SessionDao sessionDao = daoSession.getSessionDao();
        DataPointDao dataPointDao = daoSession.getDataPointDao();

        // build session
        JSONObject jsonSession = jsonBase.getJSONObject(Json.SESSION);
        Session session = new Session();
        session.setStart(jsonSession.getLong(Json.SESSION_START));
        session.setEnd(jsonSession.getLong(Json.SESSION_END));
        session.setDuration(jsonSession.getLong(Json.SESSION_DURATION));
        session.setDistance(BigDecimal.valueOf(jsonSession.getDouble(
                Json.SESSION_DISTANCE)).floatValue());

        // insert session
        daoSession.insert(session);

        // build datapoints
        List<DataPoint> dataPoints = new ArrayList<>();
        JSONArray jsonDataPoints = jsonBase.getJSONArray(Json.DATAPOINTS);
        for (int i = 0; i < jsonDataPoints.length(); i++) {
            JSONObject jsonDataPoint = jsonDataPoints.getJSONObject(i);
            DataPoint dataPoint = new DataPoint();
            dataPoint.setTime(jsonDataPoint.getLong(Json.DATAPOINT_TIME));
            dataPoint.setLatitude(jsonDataPoint.getDouble(
                    Json.DATAPOINT_LATITUDE));
            dataPoint.setLongitude(jsonDataPoint.getDouble(
                    Json.DATAPOINT_LONGITUDE));
            dataPoint.setElevation(jsonDataPoint.getDouble(
                    Json.DATAPOINT_ELEVATION));
            dataPoint.setSession(session);
            dataPoints.add(dataPoint);
        }

        // insert datapoints
        dataPointDao.insertInTx(dataPoints);
    }

    /**
     * Class defining constants for session JSON.
     *
     * @author Alkisum
     * @version 4.0
     * @since 4.0
     */
    public static class Json {
        /**
         * JSON file version.
         */
        static final int JSON_VERSION = 2;

        /**
         * JSON file name prefix.
         */
        public static final String FILE_PREFIX = "CloudRun_";

        /**
         * Regex for the Json file.
         */
        public static final String FILE_REGEX = FILE_PREFIX
                + "(\\d{4})-(\\d{2})-(\\d{2})_(\\d{6})"
                + JsonFile.FILE_EXT + "$";

        /**
         * JSON name for JSON file version number.
         */
        public static final String VERSION = "version";

        /**
         * JSON name for session object.
         */
        static final String SESSION = "session";

        /**
         * JSON name for session start.
         */
        static final String SESSION_START = "start";

        /**
         * JSON name for session end.
         */
        static final String SESSION_END = "end";

        /**
         * JSON name for session duration.
         */
        static final String SESSION_DURATION = "duration";

        /**
         * JSON name for session distance.
         */
        static final String SESSION_DISTANCE = "distance";

        /**
         * JSON name for dataPoint array.
         */
        static final String DATAPOINTS = "dataPoints";

        /**
         * JSON name for dataPoint time.
         */
        static final String DATAPOINT_TIME = "time";

        /**
         * JSON name for dataPoint latitude.
         */
        static final String DATAPOINT_LATITUDE = "latitude";

        /**
         * JSON name for dataPoint longitude.
         */
        static final String DATAPOINT_LONGITUDE = "longitude";

        /**
         * JSON name for dataPoint elevation.
         */
        static final String DATAPOINT_ELEVATION = "elevation";
    }
}
