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
import java.util.ArrayList;
import java.util.List;

/**
 * Task to write the session in JSON object into a file.
 *
 * @author Alkisum
 * @version 1.1
 * @since 1.0
 */
public class JsonFileWriter extends AsyncTask<Void, Void,
        List<JsonFileWriter.Wrapper>> {

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
     * List of sessions to write to files.
     */
    private final List<Session> mSessions;

    /**
     * Exception that can be set when an exception is caught during the task.
     */
    private Exception mException;

    /**
     * JsonFileWriter constructor.
     *
     * @param context  Context
     * @param callback Listener of the task
     * @param sessions List of sessions to write to files
     */
    JsonFileWriter(final Context context,
                   final JsonFileWriterListener callback,
                   final List<Session> sessions) {
        mContext = context;
        mCallback = callback;
        mSessions = sessions;
    }

    @Override
    protected final List<Wrapper> doInBackground(final Void... voids) {

        List<Wrapper> wrappers = new ArrayList<>();

        try {
            for (Session session : mSessions) {
                // Create temporary file, its name does not matter
                File file = File.createTempFile(Uploader.FILE_PREFIX
                                + session.getStart(), Uploader.FILE_EXT,
                        mContext.getCacheDir());

                JSONObject jsonObject = buildJsonFromSession(session);

                FileWriter writer = new FileWriter(file);
                writer.write(jsonObject.toString());
                writer.flush();
                writer.close();

                wrappers.add(new Wrapper(session, file));
            }
        } catch (IOException | JSONException e) {
            mException = e;
        }

        return wrappers;
    }

    @Override
    protected final void onPostExecute(final List<Wrapper> wrappers) {
        if (mException == null) {
            mCallback.onJsonFileWritten(wrappers);
        } else {
            mCallback.onJsonFileFailed(mException);
        }
    }

    /**
     * Build a JSON object from the session data.
     *
     * @param session Session to build to JSON object from
     * @return JSONObject
     * @throws JSONException An error occurred while building the JSON object
     */
    private JSONObject buildJsonFromSession(final Session session)
            throws JSONException {

        JSONObject jsonSession = new JSONObject();
        jsonSession.put("id", session.getId());
        jsonSession.put("start", session.getStart());
        jsonSession.put("end", session.getEnd());
        jsonSession.put("distance", session.getDistance());

        JSONArray jsonArrayDataPoints = new JSONArray();

        List<DataPoint> dataPoints = session.getDataPoints();
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
         * Called when the JSON files are written with the sessions data in it.
         *
         * @param wrappers List of wrappers containing the session and the file
         *                 where the data is written
         */
        void onJsonFileWritten(List<Wrapper> wrappers);

        /**
         * Called when an exception has been caught during the task.
         *
         * @param exception Exception caught
         */
        void onJsonFileFailed(Exception exception);
    }

    /**
     * Wrapper class containing a session and its JSON file.
     */
    public class Wrapper {

        /**
         * Session.
         */
        private Session mSession;

        /**
         * JSON file containing the session's data.
         */
        private File mFile;

        /**
         * Wrapper constructor.
         * @param session Session
         * @param file JSON file containing the session's data
         */
        public Wrapper(final Session session, final File file) {
            mFile = file;
            mSession = session;
        }

        /**
         * @return Session
         */
        public final Session getSession() {
            return mSession;
        }

        /**
         * @return JSON file containing the session's data
         */
        public final File getFile() {
            return mFile;
        }
    }
}
