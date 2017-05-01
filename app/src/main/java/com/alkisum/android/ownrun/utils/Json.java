package com.alkisum.android.ownrun.utils;

import com.alkisum.android.cloudops.file.json.JsonFile;
import com.alkisum.android.ownrun.data.Db;
import com.alkisum.android.ownrun.model.DataPoint;
import com.alkisum.android.ownrun.model.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Class containing constant for Json files.
 *
 * @author Alkisum
 * @version 2.4
 * @since 2.0
 */
public final class Json {

    /**
     * JSON file version.
     */
    private static final int JSON_VERSION = 2;

    /**
     * JSON file name prefix.
     */
    private static final String FILE_PREFIX = "ownRun_";

    /**
     * JSON file extension.
     */
    public static final String FILE_EXT = ".json";

    /**
     * Regex for the Json file.
     */
    private static final String FILE_REGEX = FILE_PREFIX
            + "(\\d{4})-(\\d{2})-(\\d{2})_(\\d{6})" + FILE_EXT;

    /**
     * JSON name for JSON file version number.
     */
    public static final String VERSION = "version";

    /**
     * JSON name for session object.
     */
    public static final String SESSION = "session";

    /**
     * JSON name for session start.
     */
    public static final String SESSION_START = "start";

    /**
     * JSON name for session end.
     */
    public static final String SESSION_END = "end";

    /**
     * JSON name for session duration.
     */
    public static final String SESSION_DURATION = "duration";

    /**
     * JSON name for session distance.
     */
    public static final String SESSION_DISTANCE = "distance";

    /**
     * JSON name for dataPoint array.
     */
    public static final String DATAPOINTS = "dataPoints";

    /**
     * JSON name for dataPoint time.
     */
    public static final String DATAPOINT_TIME = "time";

    /**
     * JSON name for dataPoint latitude.
     */
    public static final String DATAPOINT_LATITUDE = "latitude";

    /**
     * JSON name for dataPoint longitude.
     */
    public static final String DATAPOINT_LONGITUDE = "longitude";

    /**
     * JSON name for dataPoint elevation.
     */
    public static final String DATAPOINT_ELEVATION = "elevation";

    /**
     * Json constructor.
     */
    private Json() {

    }

    /**
     * Build a queue of JSON files from the given sessions.
     *
     * @param selectedSessions Selected sessions
     * @return Queue of JSON files
     * @throws JSONException An error occurred while building the JSON object
     */
    public static Queue<JsonFile> buildJsonFilesFromSessions(
            final List<Session> selectedSessions) throws JSONException {
        Queue<JsonFile> jsonFiles = new LinkedList<>();
        for (Session session : selectedSessions) {
            String fileName = FILE_PREFIX + Format.DATE_TIME_JSON.format(
                    new Date(session.getStart()));
            JSONObject jsonObject = buildJsonFromSession(session);
            jsonFiles.add(new JsonFile(fileName, jsonObject));
        }
        return jsonFiles;
    }

    /**
     * Build a JSON object from the session data.
     *
     * @param session Session to build to JSON object from
     * @return JSONObject
     * @throws JSONException An error occurred while building the JSON object
     */
    private static JSONObject buildJsonFromSession(final Session session)
            throws JSONException {

        JSONObject jsonSession = new JSONObject();
        jsonSession.put(SESSION_START, session.getStart());
        jsonSession.put(SESSION_END, session.getEnd());
        jsonSession.put(SESSION_DURATION, session.getDuration());
        jsonSession.put(SESSION_DISTANCE, session.getDistance());

        JSONArray jsonDataPoints = new JSONArray();

        List<DataPoint> dataPoints = session.getDataPoints();
        for (DataPoint dataPoint : dataPoints) {
            JSONObject jsonDataPoint = new JSONObject();
            jsonDataPoint.put(DATAPOINT_TIME, dataPoint.getTime());
            jsonDataPoint.put(DATAPOINT_LATITUDE, dataPoint.getLatitude());
            jsonDataPoint.put(DATAPOINT_LONGITUDE,
                    dataPoint.getLongitude());
            jsonDataPoint.put(DATAPOINT_ELEVATION,
                    dataPoint.getElevation());
            jsonDataPoints.put(jsonDataPoint);
        }

        JSONObject jsonBase = new JSONObject();
        jsonBase.put(VERSION, JSON_VERSION);
        jsonBase.put(SESSION, jsonSession);
        jsonBase.put(DATAPOINTS, jsonDataPoints);

        return jsonBase;
    }

    /**
     * Check if the file name is valid.
     *
     * @param jsonFile JSON file to check
     * @return true if the file name is valid, false otherwise
     */
    public static boolean isFileNameValid(final JsonFile jsonFile) {
        return jsonFile.getName().matches(FILE_REGEX);
    }

    /**
     * Check if the session is already in the database.
     *
     * @param jsonFile JSON file to check
     * @return true if the session is already in the database, false otherwise
     */
    public static boolean isSessionAlreadyInDb(final JsonFile jsonFile) {
        List<Session> sessions = Db.getInstance().getDaoSession()
                .getSessionDao().loadAll();
        for (Session session : sessions) {
            if (jsonFile.getName().equals(Json.FILE_PREFIX
                    + Format.DATE_TIME_JSON.format(new Date(session.getStart()))
                    + Json.FILE_EXT)) {
                return true;
            }
        }
        return false;
    }
}
