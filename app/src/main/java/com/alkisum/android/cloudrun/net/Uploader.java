package com.alkisum.android.cloudrun.net;

import android.content.Context;
import android.content.Intent;

import com.alkisum.android.cloudops.events.JsonFileWriterEvent;
import com.alkisum.android.cloudops.events.UploadEvent;
import com.alkisum.android.cloudops.file.json.JsonFileWriter;
import com.alkisum.android.cloudops.net.ConnectInfo;
import com.alkisum.android.cloudops.net.owncloud.OcUploader;
import com.alkisum.android.cloudrun.model.Session;
import com.alkisum.android.cloudrun.utils.Json;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.util.List;

/**
 * Subscriber for background logic upload operations.
 *
 * @author Alkisum
 * @version 3.0
 * @since 3.0
 */
public class Uploader {

    /**
     * Subscriber id to use when receiving event.
     */
    private static final int SUBSCRIBER_ID = 808;

    /**
     * OcUploader instance to start.
     */
    private final OcUploader ocUploader;

    /**
     * Uploader constructor.
     *
     * @param context      Context
     * @param connectInfo  Connection information
     * @param intent       Intent for notification
     * @param sessions     List of session to upload
     * @param subscriberId Subscriber id allowed to process the events
     * @throws JSONException An error occurred while building the JSON object
     */
    public Uploader(final Context context, final ConnectInfo connectInfo,
                    final Intent intent, final List<Session> sessions,
                    final int subscriberId) throws JSONException {
        EventBus.getDefault().register(this);

        ocUploader = new OcUploader(context, intent, new Integer[]{
                SUBSCRIBER_ID, subscriberId});
        ocUploader.init(
                connectInfo.getAddress(),
                connectInfo.getPath(),
                connectInfo.getUsername(),
                connectInfo.getPassword());

        // Execute the JsonFileWriter task to write JSON objects into temporary
        // JSON files
        new JsonFileWriter(context, Json.buildJsonFilesFromSessions(sessions),
                new Integer[]{SUBSCRIBER_ID, subscriberId}).execute();
    }

    /**
     * Triggered on JSON file writer event.
     *
     * @param event JSON file writer event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onJsonFileWriterEvent(final JsonFileWriterEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        switch (event.getResult()) {
            case JsonFileWriterEvent.OK:
                ocUploader.start(event.getJsonFiles());
                break;
            case JsonFileWriterEvent.ERROR:
                EventBus.getDefault().unregister(this);
                break;
            default:
                break;
        }
    }

    /**
     * Triggered on upload event.
     *
     * @param event Upload event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onUploadEvent(final UploadEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        switch (event.getResult()) {
            case UploadEvent.OK:
                EventBus.getDefault().unregister(this);
                break;
            case UploadEvent.ERROR:
                EventBus.getDefault().unregister(this);
                break;
            default:
                break;
        }
    }
}
