package com.alkisum.android.cloudrun.net;

import android.content.Context;
import android.content.Intent;

import com.alkisum.android.cloudlib.events.DownloadEvent;
import com.alkisum.android.cloudlib.events.JsonFileReaderEvent;
import com.alkisum.android.cloudlib.file.json.JsonFile;
import com.alkisum.android.cloudlib.file.json.JsonFileReader;
import com.alkisum.android.cloudlib.net.ConnectInfo;
import com.alkisum.android.cloudlib.net.nextcloud.NcDownloader;
import com.alkisum.android.cloudrun.database.Inserter;
import com.alkisum.android.cloudrun.events.InsertedEvent;
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
 * @param <T> Jsonable
 * @author Alkisum
 * @version 4.0
 * @since 3.0
 */
public class Downloader<T extends Jsonable> {

    /**
     * Subscriber id to use when receiving event.
     */
    private static final int SUBSCRIBER_ID = 735;

    /**
     * Subscriber id allowed to process the events.
     */
    private final Integer subscriberId;

    /**
     * Jsonable class.
     */
    private final Class<T> jsonableClass;

    /**
     * Regex to use on names when checking downloaded file.
     */
    private final String fileNameRegex;

    /**
     * Downloader constructor.
     *
     * @param context       Context
     * @param connectInfo   Connection information
     * @param intent        Intent for notification
     * @param subscriberId  Subscriber id allowed to process the events
     * @param jsonableClass Jsonable class
     * @param fileNameRegex Regex to use on names when checking downloaded file
     */
    public Downloader(final Context context, final ConnectInfo connectInfo,
                      final Intent intent, final Integer subscriberId,
                      final Class<T> jsonableClass,
                      final String fileNameRegex) {
        EventBus.getDefault().register(this);

        this.subscriberId = subscriberId;
        this.jsonableClass = jsonableClass;
        this.fileNameRegex = fileNameRegex;

        NcDownloader ncDownloader = new NcDownloader(context, intent,
                "CloudRunDownloader", "CloudRun download",
                new Integer[]{SUBSCRIBER_ID, subscriberId},
                new String[]{JsonFile.FILE_EXT});

        ncDownloader.init(
                connectInfo.getAddress(),
                connectInfo.getPath(),
                connectInfo.getUsername(),
                connectInfo.getPassword());
        ncDownloader.setExcludeFileNames(Json.getJsonFileNames(jsonableClass));
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
     * @throws InstantiationException The entity cannot be instantiated
     * @throws IllegalAccessException The entity cannot be instantiated
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public final void onJsonFileReaderEvent(final JsonFileReaderEvent event)
            throws InstantiationException, IllegalAccessException {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        switch (event.getResult()) {
            case JsonFileReaderEvent.OK:
                // get JSON objects from reader
                List<JSONObject> jsonObjects = new ArrayList<>();
                for (JsonFile jsonFile : event.getJsonFiles()) {
                    if (Json.isFileNameValid(jsonFile, fileNameRegex)
                            && !Json.isAlreadyInDb(jsonFile, jsonableClass)) {
                        jsonObjects.add(jsonFile.getJsonObject());
                    }
                }

                // convert JSON objects into entities and insert them into db
                if (Insertable.class.isAssignableFrom(jsonableClass)) {
                    Insertable insertable = (Insertable)
                            jsonableClass.newInstance();
                    new Inserter(insertable).execute(
                            jsonObjects.toArray(new JSONObject[0]));
                }
                break;
            case JsonFileReaderEvent.ERROR:
                EventBus.getDefault().unregister(this);
                break;
            default:
                break;
        }
    }

    /**
     * Triggered on inserted event.
     *
     * @param event Inserted event
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public final void onInsertedEvent(final InsertedEvent event) {
        switch (event.getResult()) {
            case InsertedEvent.OK:
                EventBus.getDefault().unregister(this);
                break;
            case InsertedEvent.ERROR:
                EventBus.getDefault().unregister(this);
                break;
            default:
                break;
        }
    }
}
