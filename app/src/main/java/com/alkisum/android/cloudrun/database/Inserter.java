package com.alkisum.android.cloudrun.database;

import android.os.AsyncTask;

import com.alkisum.android.cloudrun.events.InsertEvent;
import com.alkisum.android.cloudrun.model.DaoSession;
import com.alkisum.android.cloudrun.model.DataPoint;
import com.alkisum.android.cloudrun.model.DataPointDao;
import com.alkisum.android.cloudrun.model.Session;
import com.alkisum.android.cloudrun.model.SessionDao;
import com.alkisum.android.cloudrun.utils.Json;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Task to insert sessions in database from JSON objects.
 *
 * @author Alkisum
 * @version 3.0
 * @since 2.4
 */
public class Inserter extends AsyncTask<Void, Void, Void> {

    /**
     * List of JSON objects to read.
     */
    private final List<JSONObject> jsonObjects;

    /**
     * Session dao.
     */
    private final SessionDao sessionDao;

    /**
     * DataPoint dao.
     */
    private final DataPointDao dataPointDao;

    /**
     * List of dataPoints to insert into the database.
     */
    private List<DataPoint> dataPoints;

    /**
     * Exception that can be set when an exception is caught during the task.
     */
    private Exception exception;

    /**
     * Inserter constructor.
     *
     * @param jsonObjects List of JSON objects to read
     */
    public Inserter(final List<JSONObject> jsonObjects) {
        this.jsonObjects = jsonObjects;
        DaoSession daoSession = Db.getInstance().getDaoSession();
        sessionDao = daoSession.getSessionDao();
        dataPointDao = daoSession.getDataPointDao();
    }

    @Override
    protected final Void doInBackground(final Void... params) {
        dataPoints = new ArrayList<>();
        try {
            for (JSONObject jsonObject : jsonObjects) {
                buildSessionFromJson(jsonObject);
            }
            dataPointDao.insertInTx(dataPoints);
        } catch (JSONException e) {
            exception = e;
        }
        return null;
    }

    @Override
    protected final void onPostExecute(final Void param) {
        if (exception == null) {
            EventBus.getDefault().post(new InsertEvent(InsertEvent.OK));
        } else {
            EventBus.getDefault().post(new InsertEvent(InsertEvent.ERROR,
                    exception));
        }
    }

    /**
     * Get session and dataPoint data from JSON, insert sessions into database,
     * add dataPoints to list be inserted in transaction when all the files
     * are all parsed.
     *
     * @param jsonBase JSONObject the structure is based on
     * @throws JSONException An error occurred while parsing the JSON object
     */
    private void buildSessionFromJson(final JSONObject jsonBase)
            throws JSONException {
        int version = jsonBase.getInt(Json.VERSION);
        switch (version) {
            case 1:
                fromVersion1(jsonBase);
                break;
            case 2:
                fromVersion2(jsonBase);
                break;
            default:
                break;
        }
    }

    /**
     * Build session from Json file version 1.
     *
     * @param jsonBase JSONObject the structure is based on
     * @throws JSONException An error occurred while parsing the JSON object
     */
    private void fromVersion1(final JSONObject jsonBase) throws JSONException {
        JSONObject jsonSession = jsonBase.getJSONObject(Json.SESSION);
        Session session = new Session();
        long start = jsonSession.getLong(Json.SESSION_START);
        long end = jsonSession.getLong(Json.SESSION_END);
        session.setStart(start);
        session.setEnd(end);
        session.setDuration(end - start);
        session.setDistance(BigDecimal.valueOf(jsonSession.getDouble(
                Json.SESSION_DISTANCE)).floatValue());
        sessionDao.insert(session);

        JSONArray jsonDataPoints = jsonBase.getJSONArray(
                Json.DATAPOINTS);
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
    }

    /**
     * Build session from Json file version 2. Changes:
     * - The duration is written in the session object
     *
     * @param jsonBase JSONObject the structure is based on
     * @throws JSONException An error occurred while parsing the JSON object
     */
    private void fromVersion2(final JSONObject jsonBase) throws JSONException {
        JSONObject jsonSession = jsonBase.getJSONObject(Json.SESSION);
        Session session = new Session();
        session.setStart(jsonSession.getLong(Json.SESSION_START));
        session.setEnd(jsonSession.getLong(Json.SESSION_END));
        session.setDuration(jsonSession.getLong(Json.SESSION_DURATION));
        session.setDistance(BigDecimal.valueOf(jsonSession.getDouble(
                Json.SESSION_DISTANCE)).floatValue());
        sessionDao.insert(session);

        JSONArray jsonDataPoints = jsonBase.getJSONArray(
                Json.DATAPOINTS);
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
    }
}
