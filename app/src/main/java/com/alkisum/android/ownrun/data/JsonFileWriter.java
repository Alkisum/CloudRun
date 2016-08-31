package com.alkisum.android.ownrun.data;

import android.content.Context;
import android.os.AsyncTask;

import com.alkisum.android.ownrun.model.DataPoint;
import com.alkisum.android.ownrun.model.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Task to write the session in JSON object into a file.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
class JsonFileWriter extends AsyncTask<Void, Void, File> {

    /**
     * JSON file version number. Must be incremented when the JSON file
     * structure is changed.
     */
    private static final int VERSION = 1;

    /**
     * Context.
     */
    private final Context mContext;

    /**
     * Listener to get notification when the task finishes.
     */
    private final JsonFileWriterListener mCallback;

    /**
     * Session to write to file.
     */
    private final Session mSession;

    /**
     * Exception that can be set when an exception is caught during the task.
     */
    private Exception mException;

    /**
     * JsonFileWriter constructor.
     *
     * @param context  Context
     * @param callback Listener of the task
     * @param session  Session to write to file
     */
    JsonFileWriter(final Context context,
                          final JsonFileWriterListener callback,
                          final Session session) {
        mContext = context;
        mCallback = callback;
        mSession = session;
    }

    @Override
    protected final File doInBackground(final Void... voids) {

        File file = null;

        try {
            // Create temporary file, its name does not matter
            file = File.createTempFile("ownRun_" + mSession.getStart(),
                    ".json", mContext.getCacheDir());

            JSONObject jsonObject = buildJsonFromSession();

            FileWriter writer = new FileWriter(file);
            writer.write(jsonObject.toString());
            writer.flush();
            writer.close();
        } catch (IOException | JSONException e) {
            mException = e;
        }

        return file;
    }

    @Override
    protected final void onPostExecute(final File file) {
        if (mException == null) {
            mCallback.onJsonFileWritten(file);
        } else {
            mCallback.onJsonFileFailed(mException);
        }
    }

    /**
     * Build a JSON object from the session data.
     *
     * @return JSONObject
     * @throws JSONException An error occurred while building the JSON object
     */
    private JSONObject buildJsonFromSession() throws JSONException {

        JSONObject jsonSession = new JSONObject();
        jsonSession.put("id", mSession.getId());
        jsonSession.put("start", mSession.getStart());
        jsonSession.put("end", mSession.getEnd());
        jsonSession.put("distance", mSession.getDistance());

        JSONArray jsonArrayDataPoints = new JSONArray();

        List<DataPoint> dataPoints = mSession.getDataPoints();
        for (DataPoint dataPoint : dataPoints) {
            JSONObject jsonDataPoint = new JSONObject();
            jsonDataPoint.put("id", dataPoint.getId());
            jsonDataPoint.put("time", dataPoint.getTime());
            jsonDataPoint.put("latitude", dataPoint.getLatitude());
            jsonDataPoint.put("longitude", dataPoint.getLongitude());
            jsonDataPoint.put("elevation", dataPoint.getElevation());
            jsonArrayDataPoints.put(jsonDataPoint);
        }

        JSONObject jsonBase = new JSONObject();
        jsonBase.put("version", VERSION);
        jsonBase.put("session", jsonSession);
        jsonBase.put("dataPoints", jsonArrayDataPoints);

        return jsonBase;
    }

    /**
     * Listener for the JsonFileWriter.
     */
    public interface JsonFileWriterListener {

        /**
         * Called when the JSON file is written with the session data in it.
         *
         * @param file File where the data is written
         */
        void onJsonFileWritten(File file);

        /**
         * Called when an exception has been caught during the task.
         *
         * @param exception Exception caught
         */
        void onJsonFileFailed(Exception exception);
    }
}
