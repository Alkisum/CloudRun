package com.alkisum.android.ownrun.data;

import android.os.AsyncTask;

import com.alkisum.android.ownrun.model.DaoSession;
import com.alkisum.android.ownrun.model.DataPoint;
import com.alkisum.android.ownrun.model.DataPointDao;
import com.alkisum.android.ownrun.model.Session;
import com.alkisum.android.ownrun.model.SessionDao;
import com.alkisum.android.ownrun.utils.Json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Task to read the session's data from a JSON file and insert the session into
 * the database.
 *
 * @author Alkisum
 * @version 2.0
 * @since 2.0
 */
class JsonFileReader extends AsyncTask<Void, Void, Void> {

    /**
     * Listener to get notification when the task finishes.
     */
    private final JsonFileReaderListener mCallback;

    /**
     * List of files to read.
     */
    private final List<File> mFiles;

    /**
     * Session dao.
     */
    private final SessionDao mSessionDao;

    /**
     * DataPoint dao.
     */
    private final DataPointDao mDataPointDao;

    /**
     * List of dataPoints to insert into the database.
     */
    private List<DataPoint> mDataPoints;

    /**
     * Exception that can be set when an exception is caught during the task.
     */
    private Exception mException;

    /**
     * JsonFileReader constructor.
     *
     * @param callback Listener of the task
     * @param files    List of files to read
     */
    JsonFileReader(final JsonFileReaderListener callback,
                   final List<File> files) {
        mCallback = callback;
        mFiles = files;
        DaoSession daoSession = Db.getInstance().getDaoSession();
        mSessionDao = daoSession.getSessionDao();
        mDataPointDao = daoSession.getDataPointDao();
    }

    @Override
    protected final Void doInBackground(final Void... params) {
        BufferedReader br = null;
        mDataPoints = new ArrayList<>();
        try {
            for (File file : mFiles) {
                br = new BufferedReader(new FileReader(file));
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                while (line != null) {
                    sb.append(line);
                    line = br.readLine();
                }
                String jsonString = sb.toString();
                JSONObject jsonObject = new JSONObject(jsonString);
                buildSessionFromJson(jsonObject);
            }
            mDataPointDao.insertInTx(mDataPoints);
        } catch (IOException | JSONException e) {
            mException = e;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                mException = e;
            }
        }
        return null;
    }

    @Override
    protected final void onPostExecute(final Void param) {
        if (mException == null) {
            mCallback.onJsonFileRead();
        } else {
            mCallback.onReadJsonFileFailed(mException);
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
                JSONObject jsonSession = jsonBase.getJSONObject(Json.SESSION);
                Session session = new Session();
                session.setStart(jsonSession.getLong(Json.SESSION_START));
                session.setEnd(jsonSession.getLong(Json.SESSION_END));
                session.setDuration(jsonSession.getLong(Json.SESSION_DURATION));
                session.setDistance(BigDecimal.valueOf(jsonSession.getDouble(
                        Json.SESSION_DISTANCE)).floatValue());
                mSessionDao.insert(session);

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
                    mDataPoints.add(dataPoint);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Listener for the JsonFileReader.
     */
    interface JsonFileReaderListener {

        /**
         * Called when the JSON files are read and the session's data inserted
         * into the database.
         */
        void onJsonFileRead();

        /**
         * Called when an exception has been caught during the task.
         *
         * @param exception Exception caught
         */
        void onReadJsonFileFailed(Exception exception);
    }
}
