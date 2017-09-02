package com.alkisum.android.cloudrun.utils;

import com.alkisum.android.cloudlib.file.json.JsonFile;
import com.alkisum.android.cloudrun.database.Db;
import com.alkisum.android.cloudrun.model.DataPoint;
import com.alkisum.android.cloudrun.model.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Class containing constant for Json files.
 *
 * @author Alkisum
 * @version 3.0
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
    private static final String FILE_PREFIX = "CloudRun_";

    /**
     * Regex for the Json file.
     */
    private static final String FILE_REGEX = FILE_PREFIX
            + "(\\d{4})-(\\d{2})-(\\d{2})_(\\d{6})" + JsonFile.FILE_EXT + "$";

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
            String fileName = buildJsonFileName(session);
            JSONObject jsonObject = buildJsonFromSession(session);
            jsonFiles.add(new JsonFile(fileName, jsonObject));
        }
        return jsonFiles;
    }

    /**
     * Build file name (with extension) for JSON file.
     *
     * @param session Session to build the file name from
     * @return File name
     */
    private static String buildJsonFileName(final Session session) {
        return FILE_PREFIX + Format.DATE_TIME_JSON.format(
                new Date(session.getStart())) + JsonFile.FILE_EXT;
    }

    /**
     * Build a JSON object from the given session.
     *
     * @param session Session to build the JSON object from
     * @return JSONObject JSON object built from the session
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
            if (jsonFile.getName().equals(buildJsonFileName(session))) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return List of file names of all sessions stored in the database
     */
    public static List<String> getSessionJsonFileNames() {
        List<Session> sessions = Db.getInstance().getDaoSession()
                .getSessionDao().loadAll();
        List<String> fileNames = new ArrayList<>();
        for (Session session : sessions) {
            fileNames.add(buildJsonFileName(session));
        }
        return fileNames;
    }
}
