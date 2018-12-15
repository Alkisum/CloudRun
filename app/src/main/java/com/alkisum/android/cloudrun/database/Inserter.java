package com.alkisum.android.cloudrun.database;

import android.os.AsyncTask;

import com.alkisum.android.cloudrun.events.InsertedEvent;
import com.alkisum.android.cloudrun.net.Insertable;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Task to insert Insertable entities in database from JSON objects.
 *
 * @author Alkisum
 * @version 4.0
 * @since 2.4
 */
public class Inserter extends AsyncTask<JSONObject, Void, Void> {

    /**
     * Exception that can be set when an exception is caught during the task.
     */
    private Exception exception;

    /**
     * Insertable entity to insert.
     */
    private final Insertable insertable;

    /**
     * Inserter constructor.
     *
     * @param insertable Insertable entity to insert
     */
    public Inserter(final Insertable insertable) {
        this.insertable = insertable;
    }

    @Override
    protected final Void doInBackground(final JSONObject... jsonObjects) {
        try {
            for (JSONObject jsonObject : jsonObjects) {
                insertable.insertFromJson(jsonObject);
            }
        } catch (JSONException e) {
            exception = e;
        }
        return null;
    }

    @Override
    protected final void onPostExecute(final Void param) {
        if (exception == null) {
            EventBus.getDefault().post(new InsertedEvent(
                    insertable, InsertedEvent.OK));
        } else {
            EventBus.getDefault().post(new InsertedEvent(
                    insertable, InsertedEvent.ERROR, exception));
        }
    }
}
