package com.alkisum.android.ownrun.history;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alkisum.android.jsoncloud.file.json.JsonFile;
import com.alkisum.android.jsoncloud.net.ConnectDialog;
import com.alkisum.android.jsoncloud.net.ConnectInfo;
import com.alkisum.android.jsoncloud.net.owncloud.OcUploader;
import com.alkisum.android.ownrun.BuildConfig;
import com.alkisum.android.ownrun.R;
import com.alkisum.android.ownrun.data.Deleter;
import com.alkisum.android.ownrun.data.Sessions;
import com.alkisum.android.ownrun.dialog.ConfirmDialog;
import com.alkisum.android.ownrun.dialog.ErrorDialog;
import com.alkisum.android.ownrun.model.DataPoint;
import com.alkisum.android.ownrun.model.Session;
import com.alkisum.android.ownrun.utils.Format;
import com.alkisum.android.ownrun.utils.Json;

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
 * @version 2.4
 * @since 2.0
 */
public class SessionActivity extends AppCompatActivity implements
        ConnectDialog.ConnectDialogListener, OcUploader.UploaderListener,
        Deleter.DeleterListener {

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
     * OcUploader instance created when the user presses on the Upload item from
     * the context menu, and initialized when the connect dialog is submit.
     */
    private OcUploader mUploader;

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
     * Progress dialog to show the progress of uploading.
     */
    private ProgressDialog mProgressDialog;

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
    public final boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_session, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload:
                List<Session> sessions = new ArrayList<>();
                sessions.add(mSession);
                DialogFragment connectDialogUpload =
                        ConnectDialog.newInstance(UPLOAD_OPERATION);
                connectDialogUpload.show(getSupportFragmentManager(),
                        ConnectDialog.FRAGMENT_TAG);
                try {
                    mUploader = new OcUploader(this,
                            Json.buildJsonFilesFromSessions(sessions));
                } catch (JSONException e) {
                    ErrorDialog.build(this,
                            getString(R.string.upload_failure_title),
                            e.getMessage(), null).show();
                }
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
     * Set the GUI.
     */
    private void setGui() {
        Toolbar toolbar = ButterKnife.findById(this, R.id.session_toolbar);
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

    /**
     * Initialize the tiles.
     */
    private void initTiles() {
        long duration = mSession.getDuration();
        float distance = mSession.getDistance();
        TextView durationTextView = ButterKnife.findById(
                this, R.id.session_txt_duration);
        durationTextView.setText(Format.formatDuration(duration));
        TextView distanceTextView = ButterKnife.findById(
                this, R.id.session_txt_distance);
        distanceTextView.setText(Format.formatDistance(distance));
        TextView speedTextView = ButterKnife.findById(
                this, R.id.session_txt_speed);
        speedTextView.setText(Format.formatSpeedAvg(duration, distance));
        TextView paceTextView = ButterKnife.findById(
                this, R.id.session_txt_pace);
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
        final RelativeLayout layout = ButterKnife.findById(
                this, R.id.session_layout);
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
     * Start the upload operation.
     *
     * @param connectInfo Connection information given by user
     */
    private void startUpload(final ConnectInfo connectInfo) {
        if (mUploader == null) {
            return;
        }
        mUploader.init(
                connectInfo.getAddress(),
                connectInfo.getPath(),
                connectInfo.getUsername(),
                connectInfo.getPassword()).start();
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
        new Deleter(this).execute();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setProgressNumberFormat(null);
        mProgressDialog.setMessage(getString(R.string.session_delete_progress));
        mProgressDialog.show();
    }

    @Override
    public final void onSubmit(final int operation,
                               final ConnectInfo connectInfo) {
        if (operation == UPLOAD_OPERATION) {
            startUpload(connectInfo);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog = new ProgressDialog(SessionActivity.this);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(
                        ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setProgressNumberFormat(null);
                mProgressDialog.setMessage(getString(
                        R.string.operation_progress_init_msg));
                mProgressDialog.show();
            }
        });
    }

    @Override
    public final void onWritingFileFailed(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
                ErrorDialog.build(SessionActivity.this,
                        getString(R.string.upload_writing_failure_title),
                        e.getMessage(), null).show();
            }
        });
    }

    @Override
    public final void onUploadStart(final JsonFile jsonFile) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.setMessage("Uploading "
                            + jsonFile.getName() + Json.FILE_EXT + " ...");
                    mProgressDialog.setIndeterminate(false);
                }
            }
        });
    }

    @Override
    public final void onUploading(final int percentage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.setProgress(percentage);
                }
            }
        });
    }

    @Override
    public final void onAllUploadComplete() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
                Toast.makeText(SessionActivity.this,
                        getString(R.string.session_upload_success_toast),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public final void onUploadFailed(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
                ErrorDialog.build(SessionActivity.this, getString(
                        R.string.upload_failure_title), message, null).show();
            }
        });
    }

    @Override
    public final void onSessionsDeleted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
                setResult(HistoryActivity.SESSION_DELETED);
                finish();
            }
        });
    }
}
