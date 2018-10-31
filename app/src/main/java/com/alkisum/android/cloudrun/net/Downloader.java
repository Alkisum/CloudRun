package com.alkisum.android.cloudrun.net;

import android.content.Context;
import android.content.Intent;

import com.alkisum.android.cloudlib.events.DownloadEvent;
import com.alkisum.android.cloudlib.events.JsonFileReaderEvent;
import com.alkisum.android.cloudlib.file.json.JsonFile;
import com.alkisum.android.cloudlib.file.json.JsonFileReader;
import com.alkisum.android.cloudlib.net.ConnectInfo;
import com.alkisum.android.cloudlib.net.nextcloud.NcDownloader;
import com.alkisum.android.cloudrun.database.SessionInserter;
import com.alkisum.android.cloudrun.events.SessionInsertedEvent;
import com.alkisum.android.cloudrun.files.Json;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class starting download operation and subscribing to download events.
 *
 * @author Alkisum
 * @version 4.0
 * @since 3.0
 */
public class Downloader {

    /**
     * Subscriber id to use when receiving event.
     */
    private static final int SUBSCRIBER_ID = 735;

    /**
     * Subscriber id allowed to process the events.
     */
    private final Integer subscriberId;

    /**
     * Downloader constructor.
     *
     * @param context      Context
     * @param connectInfo  Connection information
     * @param intent       Intent for notification
     * @param subscriberId Subscriber id allowed to process the events
     */
    public Downloader(final Context context, final ConnectInfo connectInfo,
                      final Intent intent, final Integer subscriberId) {
        EventBus.getDefault().register(this);

        this.subscriberId = subscriberId;
        NcDownloader ncDownloader = new NcDownloader(context, intent,
                "CloudRunDownloader", "CloudRun download",
                new Integer[]{SUBSCRIBER_ID, subscriberId},
                new String[]{JsonFile.FILE_EXT});

        ncDownloader.init(
                connectInfo.getAddress(),
                connectInfo.getPath(),
                connectInfo.getUsername(),
                connectInfo.getPassword());
        ncDownloader.setExcludeFileNames(Json.getSessionJsonFileNames());
        ncDownloader.start();
    }

    /**
     * Triggered on download event.
     *
     * @param event Download event
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public final void onDownloadEvent(final DownloadEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        switch (event.getResult()) {
            case DownloadEvent.OK:
                new JsonFileReader(event.getFiles(),
                        new Integer[]{SUBSCRIBER_ID, subscriberId}).execute();
                break;
            case DownloadEvent.NO_FILE:
                EventBus.getDefault().unregister(this);
                break;
            case DownloadEvent.ERROR:
                EventBus.getDefault().unregister(this);
                break;
            default:
                break;
        }
    }

    /**
     * Triggered on JSON file reader event.
     *
     * @param event JSON file reader event
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public final void onJsonFileReaderEvent(final JsonFileReaderEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        switch (event.getResult()) {
            case JsonFileReaderEvent.OK:
                List<JSONObject> jsonObjects = new ArrayList<>();
                for (JsonFile jsonFile : event.getJsonFiles()) {
                    if (Json.isFileNameValid(jsonFile)
                            && !Json.isSessionAlreadyInDb(jsonFile)) {
                        jsonObjects.add(jsonFile.getJsonObject());
                    }
                }
                new SessionInserter(jsonObjects).execute();
                break;
            case JsonFileReaderEvent.ERROR:
                EventBus.getDefault().unregister(this);
                break;
            default:
                break;
        }
    }

    /**
     * Triggered on session inserted event.
     *
     * @param event Session inserted event
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public final void onSessionInsertedEvent(final SessionInsertedEvent event) {
        switch (event.getResult()) {
            case SessionInsertedEvent.OK:
                EventBus.getDefault().unregister(this);
                break;
            case SessionInsertedEvent.ERROR:
                EventBus.getDefault().unregister(this);
                break;
            default:
                break;
        }
    }
}
