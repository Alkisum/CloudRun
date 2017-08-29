package com.alkisum.android.cloudrun.history;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.Toast;

import com.alkisum.android.cloudops.events.JsonFileWriterEvent;
import com.alkisum.android.cloudops.events.UploadEvent;
import com.alkisum.android.cloudops.net.ConnectDialog;
import com.alkisum.android.cloudops.net.ConnectInfo;
import com.alkisum.android.cloudrun.BuildConfig;
import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.data.Deleter;
import com.alkisum.android.cloudrun.data.Sessions;
import com.alkisum.android.cloudrun.dialog.ConfirmDialog;
import com.alkisum.android.cloudrun.dialog.ErrorDialog;
import com.alkisum.android.cloudrun.event.DeleteEvent;
import com.alkisum.android.cloudrun.model.DataPoint;
import com.alkisum.android.cloudrun.model.Session;
import com.alkisum.android.cloudrun.net.Uploader;
import com.alkisum.android.cloudrun.utils.Format;

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
 * @version 3.0
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
     * Operation id for upload.
     */
    private static final int UPLOAD_OPERATION = 1;

    /**
     * Instance of the current session.
     */
    private Session mSession;

    /**
     * View containing the OSM.
     */
    @BindView(R.id.session_map)
    MapView mMapView;

    /**
     * TextView informing the user that there no data available to show the map.
     */
    @BindView(R.id.session_txt_no_data)
    TextView mTextViewNoData;

    /**
     * Progress bar to show the progress of operations.
     */
    @BindView(R.id.session_progressbar)
    ProgressBar mProgressBar;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_session);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            long sessionId = extras.getLong(ARG_SESSION_ID);
            mSession = Sessions.getSessionById(sessionId);
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
        toolbar.setTitle(Format.DATE_TIME_HISTORY.format(mSession.getStart()));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });

        initTiles();

        if (!mSession.getDataPoints().isEmpty()) {
            initMap();
        } else {
            mMapView.setVisibility(View.GONE);
            mTextViewNoData.setVisibility(View.VISIBLE);
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
                mSession.setSelected(true);
                if (!Sessions.getSelectedSessions().isEmpty()) {
                    showDeleteConfirmation();
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
        long duration = mSession.getDuration();
        float distance = mSession.getDistance();
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
        mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mMapView.setMultiTouchControls(true);

        ArrayList<GeoPoint> wayPoints = new ArrayList<>();
        for (DataPoint dp : mSession.getDataPoints()) {
            wayPoints.add(new GeoPoint(dp.getLatitude(), dp.getLongitude()));
        }

        Polyline polyline = new Polyline();
        polyline.setColor(ContextCompat.getColor(this,
                android.R.color.holo_red_light));
        polyline.setPoints(wayPoints);
        polyline.setWidth(2f);

        final BoundingBox boundingBox = BoundingBox.fromGeoPoints(wayPoints);
        final RelativeLayout layout = findViewById(R.id.session_layout);
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // Will be called when the layout is ready
                        mMapView.zoomToBoundingBox(boundingBox, false);
                        mMapView.invalidate();
                        layout.getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                    }
                });
        IMapController mapController = mMapView.getController();
        // Set zoom to max
        mapController.setZoom(19);
        // Set center with the start GeoPoint
        DataPoint dpStart = mSession.getDataPoints().get(0);
        mapController.setCenter(new GeoPoint(
                dpStart.getLatitude(), dpStart.getLongitude()));
        mMapView.getOverlays().add(polyline);
        mMapView.invalidate();
    }

    /**
     * Show dialog to confirm the deletion of the selected sessions.
     */
    private void showDeleteConfirmation() {
        ConfirmDialog.build(this,
                getString(R.string.session_delete_title),
                getString(R.string.session_delete_msg),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface,
                                        final int i) {
                        deleteSession();
                    }
                }).show();
    }

    /**
     * Execute the task to delete the selected sessions.
     */
    private void deleteSession() {
        new Deleter(new Integer[]{SUBSCRIBER_ID}).execute();
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public final void onSubmit(final int operation,
                               final ConnectInfo connectInfo) {
        if (operation == UPLOAD_OPERATION) {
            try {
                Intent intent = new Intent(this, SessionActivity.class);
                intent.putExtra(ARG_SESSION_ID, mSession.getId());
                List<Session> sessions = new ArrayList<>();
                sessions.add(mSession);
                new Uploader(getApplicationContext(), connectInfo, intent,
                        sessions, SUBSCRIBER_ID);
            } catch (JSONException e) {
                ErrorDialog.build(this,
                        getString(R.string.upload_failure_title),
                        e.getMessage(), null).show();
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setIndeterminate(true);
                mProgressBar.setVisibility(View.VISIBLE);
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
                mProgressBar.setVisibility(View.VISIBLE);
                break;
            case JsonFileWriterEvent.ERROR:
                ErrorDialog.build(this,
                        getString(R.string.upload_writing_failure_title),
                        event.getException().getMessage(), null).show();
                mProgressBar.setVisibility(View.GONE);
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
                mProgressBar.setVisibility(View.VISIBLE);
                break;
            case UploadEvent.OK:
                Toast.makeText(this,
                        getString(R.string.session_upload_success_toast),
                        Toast.LENGTH_LONG).show();
                mProgressBar.setVisibility(View.GONE);
                break;
            case UploadEvent.ERROR:
                ErrorDialog.build(SessionActivity.this, getString(
                        R.string.upload_failure_title), event.getMessage(),
                        null).show();
                mProgressBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     * Triggered on delete event.
     *
     * @param event Delete event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onDeleteEvent(final DeleteEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        setResult(HistoryActivity.SESSION_DELETED);
        finish();
    }
}
