package com.alkisum.android.cloudrun.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alkisum.android.cloudlib.events.JsonFileWriterEvent;
import com.alkisum.android.cloudlib.events.UploadEvent;
import com.alkisum.android.cloudlib.net.ConnectDialog;
import com.alkisum.android.cloudlib.net.ConnectInfo;
import com.alkisum.android.cloudrun.BuildConfig;
import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.dialogs.ErrorDialog;
import com.alkisum.android.cloudrun.events.DeletedEvent;
import com.alkisum.android.cloudrun.interfaces.Deletable;
import com.alkisum.android.cloudrun.interfaces.Jsonable;
import com.alkisum.android.cloudrun.model.DataPoint;
import com.alkisum.android.cloudrun.model.Session;
import com.alkisum.android.cloudrun.net.Uploader;
import com.alkisum.android.cloudrun.tasks.Deleter;
import com.alkisum.android.cloudrun.utils.Format;
import com.alkisum.android.cloudrun.utils.Sessions;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity showing session information.
 *
 * @author Alkisum
 * @version 4.0
 * @since 2.0
 */
public class SessionActivity extends AppCompatActivity implements
        ConnectDialog.ConnectDialogListener {

    /**
     * Subscriber id to use when receiving event.
     */
    private static final int SUBSCRIBER_ID = 639;

    /**
     * Argument for session id.
     */
    public static final String ARG_SESSION_ID = "arg_session_id";

    /**
     * Argument for the JSON representation of the session, sent to the
     * HistoryActivity when the session has been deleted.
     */
    static final String ARG_SESSION_JSON = "arg_session_json";

    /**
     * Operation id for upload.
     */
    private static final int UPLOAD_OPERATION = 1;

    /**
     * Instance of the current session.
     */
    private Session session;

    /**
     * View containing the OSM.
     */
    @BindView(R.id.session_map)
    MapView mapView;

    /**
     * TextView informing the user that there no data available to show the map.
     */
    @BindView(R.id.session_txt_no_data)
    TextView textViewNoData;

    /**
     * Progress bar to show the progress of operations.
     */
    @BindView(R.id.session_progressbar)
    ProgressBar progressBar;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_session);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            long sessionId = extras.getLong(ARG_SESSION_ID);
            session = Sessions.getSessionById(sessionId);
        }

        setGui();
    }

    @Override
    public final void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public final void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Set the GUI.
     */
    private void setGui() {
        Toolbar toolbar = findViewById(R.id.session_toolbar);
        toolbar.setTitle(Format.getDateTimeHistory().format(
                session.getStart()));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initTiles();

        if (!session.getDataPoints().isEmpty()) {
            initMap();
        } else {
            mapView.setVisibility(View.GONE);
            textViewNoData.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_session, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload:
                ConnectDialog connectDialogUpload =
                        ConnectDialog.newInstance(UPLOAD_OPERATION);
                connectDialogUpload.setCallback(this);
                connectDialogUpload.show(getSupportFragmentManager(),
                        ConnectDialog.FRAGMENT_TAG);
                return true;
            case R.id.action_delete:
                session.setSelected(true);
                if (!Sessions.getSelectedSessions().isEmpty()) {
                    deleteSession();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Initialize the tiles.
     */
    private void initTiles() {
        long duration = session.getDuration();
        float distance = session.getDistance();
        TextView durationTextView = findViewById(R.id.session_txt_duration);
        durationTextView.setText(Format.formatDuration(duration));
        TextView distanceTextView = findViewById(R.id.session_txt_distance);
        distanceTextView.setText(Format.formatDistance(distance));
        TextView speedTextView = findViewById(R.id.session_txt_speed);
        speedTextView.setText(Format.formatSpeedAvg(duration, distance));
        TextView paceTextView = findViewById(R.id.session_txt_pace);
        paceTextView.setText(Format.formatPaceAvg(duration, distance));
    }

    /**
     * Initialize the map.
     */
    private void initMap() {
        Configuration.getInstance().setUserAgentValue(
                BuildConfig.APPLICATION_ID);
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.setMultiTouchControls(true);

        ArrayList<GeoPoint> wayPoints = new ArrayList<>();
        session.resetDataPoints();
        for (DataPoint dp : session.getDataPoints()) {
            wayPoints.add(new GeoPoint(dp.getLatitude(), dp.getLongitude()));
        }

        Polyline polyline = new Polyline();
        polyline.setColor(ContextCompat.getColor(this, R.color.map_blue));
        polyline.setPoints(wayPoints);
        polyline.setWidth(10f);

        final BoundingBox boundingBox = BoundingBox.fromGeoPoints(wayPoints);
        final RelativeLayout layout = findViewById(R.id.session_layout);
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // Will be called when the layout is ready
                        mapView.zoomToBoundingBox(boundingBox, false);
                        mapView.invalidate();
                        layout.getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                    }
                });
        IMapController mapController = mapView.getController();
        // Set zoom to max
        mapController.setZoom(19d);
        // Set center with the start GeoPoint
        DataPoint dpStart = session.getDataPoints().get(0);
        mapController.setCenter(new GeoPoint(
                dpStart.getLatitude(), dpStart.getLongitude()));
        mapView.getOverlays().add(polyline);
        mapView.invalidate();
    }

    /**
     * Execute the task to delete the selected sessions.
     */
    private void deleteSession() {
        Deletable[] sessions = Sessions.getSelectedSessions().toArray(
                new Deletable[0]);
        new Deleter(new Integer[]{SUBSCRIBER_ID},
                new Session()).execute(sessions);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public final void onSubmit(final int operation,
                               final ConnectInfo connectInfo) {
        if (operation == UPLOAD_OPERATION) {
            try {
                Intent intent = new Intent(this, SessionActivity.class);
                intent.putExtra(ARG_SESSION_ID, session.getId());
                List<Jsonable> sessions = new ArrayList<>();
                sessions.add(session);
                new Uploader(getApplicationContext(), connectInfo, intent,
                        sessions, SUBSCRIBER_ID);
            } catch (JSONException e) {
                ErrorDialog.show(this,
                        getString(R.string.upload_failure_title),
                        e.getMessage(), null);
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setIndeterminate(true);
                progressBar.setVisibility(View.VISIBLE);
            }
        });
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
                progressBar.setVisibility(View.VISIBLE);
                break;
            case JsonFileWriterEvent.ERROR:
                ErrorDialog.show(this,
                        getString(R.string.upload_writing_failure_title),
                        event.getException().getMessage(), null);
                progressBar.setVisibility(View.GONE);
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
            case UploadEvent.UPLOADING:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case UploadEvent.OK:
                Snackbar.make(findViewById(R.id.session_layout),
                        R.string.session_upload_success_snackbar,
                        Snackbar.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                break;
            case UploadEvent.ERROR:
                ErrorDialog.show(this, getString(
                        R.string.upload_failure_title), event.getMessage(),
                        null);
                progressBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     * Triggered on deleted event.
     *
     * @param event Deleted event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onDeletedEvent(final DeletedEvent event) {
        if (!(event.getDeletable() instanceof Session)
                || !event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }

        // Exclude session field in DataPoint to avoid circular references
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(final FieldAttributes f) {
                        return f.getName().equals("session");
                    }

                    @Override
                    public boolean shouldSkipClass(final Class<?> clazz) {
                        return false;
                    }
                })
                .create();

        // Notify MainActivity that a session has been deleted
        Intent intent = new Intent();
        intent.putExtra(ARG_SESSION_JSON, gson.toJson(session));
        setResult(HistoryActivity.SESSION_DELETED, intent);
        finish();
    }
}
