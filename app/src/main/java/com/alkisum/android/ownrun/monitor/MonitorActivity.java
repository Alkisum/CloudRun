package com.alkisum.android.ownrun.monitor;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alkisum.android.ownrun.R;
import com.alkisum.android.ownrun.data.Recorder;
import com.alkisum.android.ownrun.dialog.ErrorDialog;
import com.alkisum.android.ownrun.event.CoordinateEvent;
import com.alkisum.android.ownrun.event.DistanceEvent;
import com.alkisum.android.ownrun.event.PaceEvent;
import com.alkisum.android.ownrun.event.SpeedEvent;
import com.alkisum.android.ownrun.history.HistoryActivity;
import com.alkisum.android.ownrun.location.LocationHandler;
import com.alkisum.android.ownrun.location.LocationHelper;
import com.alkisum.android.ownrun.settings.SettingsActivity;
import com.alkisum.android.ownrun.utils.Format;
import com.alkisum.android.ownrun.utils.Pref;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Main activity showing location values.
 *
 * @author Alkisum
 * @version 2.2
 * @since 1.0
 */
public class MonitorActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        Tile.TileListener, Recorder.RecorderListener {

    /**
     * Request code for denied permissions.
     */
    private static final int REQUEST_CODE = 683;

    /**
     * Delay before the screen get locked.
     */
    private static final long LOCK_DELAY = 5000;

    /**
     * Delay between each stopwatch blink when the session is paused.
     */
    private static final long BLINK_DELAY = 500;

    /**
     * Action constant for session start.
     */
    private static final int ACTION_START = 1;

    /**
     * Action constant for session resume.
     */
    private static final int ACTION_RESUME = 2;

    /**
     * Action constant for session pause.
     */
    private static final int ACTION_PAUSE = 3;

    /**
     * Action constant for session stop.
     */
    private static final int ACTION_STOP = 4;

    /**
     * Action constant for session lock.
     */
    private static final int ACTION_LOCK = 5;

    /**
     * Action constant for session unlock.
     */
    private static final int ACTION_UNLOCK = 6;

    /**
     * SharedPreferences instance.
     */
    private SharedPreferences mSharedPref;

    /**
     * EventBus instance.
     */
    private EventBus mEventBus;

    /**
     * LocationHelper instance.
     */
    private LocationHelper mLocationHelper;

    /**
     * Recorder instance to start and stop recording data.
     */
    private Recorder mRecorder;

    /**
     * Flag set to true if the session is currently running, false otherwise.
     */
    private boolean mSessionRunning;

    /**
     * Flag set to true if the session is currently paused, false otherwise.
     */
    private boolean mSessionPaused;

    /**
     * Flag set to true if the session is locked, false otherwise.
     */
    private boolean mLocked;

    /**
     * Flag set to true when new GPS data has been received, false otherwise.
     */
    private boolean mNewGpsDataReceived;

    /**
     * Handler for the task to make the stopwatch blink when the session is
     * paused.
     */
    private final Handler mStopwatchBlinkHandler = new Handler();

    /**
     * Handler for the task checking for the GPS status.
     */
    private final Handler mGpsStatusHandler = new Handler();

    /**
     * Handler for the lock timeout.
     */
    private final Handler mLockTimeoutHandler = new Handler();

    /**
     * Flag set to true when the lock timeout task delay is on, false otherwise.
     */
    private boolean mLockTimeoutOn;

    /**
     * List of layout containing GPS data.
     */
    private List<Tile> mTiles;

    /**
     * Array containing the last values for each data type. The index is based
     * on the data type constants.
     */
    private String[] mCurrentValues;

    /**
     * Id for current GPS status icon.
     */
    private int mGpsStatusIconId = R.drawable.ic_gps_not_fixed_white_24dp;

    /**
     * Flag set to true if the GPS accuracy must be shown, false otherwise.
     */
    private boolean mShowGpsAccuracy;

    /**
     * TextView showing the GPS accuracy by displaying the distance in meter
     * between 2 coordinates.
     */
    @BindView(R.id.monitor_txt_gps_accuracy)
    TextView mTextGpsAccuracy;

    /**
     * TextView at the top of the layout.
     */
    @BindView(R.id.monitor_txt_top)
    TextView mTextTop;

    /**
     * Drawer layout.
     */
    @BindView(R.id.monitor_drawer_layout)
    DrawerLayout mDrawerLayout;

    /**
     * ActionBarDrawerToggle instance.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    /**
     * Layout containing the start button.
     */
    @BindView(R.id.monitor_layout_start)
    RelativeLayout mLayoutStart;

    /**
     * Layout containing the lock button.
     */
    @BindView(R.id.monitor_layout_lock)
    RelativeLayout mLayoutLock;

    /**
     * Layout containing the pause button.
     */
    @BindView(R.id.monitor_layout_pause)
    RelativeLayout mLayoutPause;

    /**
     * Layout containing the stop button.
     */
    @BindView(R.id.monitor_layout_stop)
    RelativeLayout mLayoutStop;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] deniedPermissions = getDeniedPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }
        );
        if (deniedPermissions.length > 0
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(deniedPermissions, REQUEST_CODE);
        } else {
            init();
        }

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        mSharedPref.registerOnSharedPreferenceChangeListener(this);

        mEventBus = EventBus.getDefault();
        mEventBus.register(this);

        initCurrentValues();

        setContentView(R.layout.activity_monitor);
        ButterKnife.bind(this);

        setKeepScreenOn();

        setGui();
    }

    @Override
    protected final void onDestroy() {
        mSharedPref.unregisterOnSharedPreferenceChangeListener(this);
        mEventBus.unregister(this);

        mGpsStatusHandler.removeCallbacks(mGpsStatusTask);

        if (mLocationHelper != null) {
            mLocationHelper.stop();
            mLocationHelper.onDestroy();
        }
        super.onDestroy();
    }

    /**
     * Get denied permissions from given list.
     *
     * @param permissions List of permissions
     * @return Array of denied permissions
     */
    private String[] getDeniedPermissions(final String[] permissions) {
        List<String> deniedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permission);
            }
        }
        return deniedPermissions.toArray(new String[deniedPermissions.size()]);
    }

    @Override
    public final void onRequestPermissionsResult(
            final int requestCode, @NonNull final String[] permissions,
            @NonNull final int[] grantResults) {
        if (requestCode != REQUEST_CODE) {
            return;
        }
        boolean permissionsGranted = true;
        String errorMessage = "";
        for (int i = 0; i < permissions.length; i++) {
            switch (permissions[i]) {
                case Manifest.permission.ACCESS_FINE_LOCATION:
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        if (!permissionsGranted) {
                            errorMessage += "\n\n";
                        }
                        permissionsGranted = false;
                        errorMessage += getString(R.string.permission_location);
                    }
                    break;
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        if (!permissionsGranted) {
                            errorMessage += "\n\n";
                        }
                        permissionsGranted = false;
                        errorMessage += getString(R.string.permission_storage);
                    }
                    break;
                default:
                    break;
            }
        }
        if (permissionsGranted) {
            init();
        } else {
            ErrorDialog.build(this, getString(R.string.permission_title),
                    errorMessage, mExit).show();
        }
    }

    /**
     * OnClickListener to exit the application.
     */
    private final DialogInterface.OnClickListener mExit =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog,
                                    final int which) {
                    finish();
                }
            };

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_monitor, menu);
        return true;
    }

    @Override
    public final boolean onPrepareOptionsMenu(final Menu menu) {
        MenuItem gps = menu.findItem(R.id.action_gps);
        gps.setIcon(mGpsStatusIconId);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_gps) {
            mShowGpsAccuracy = !mShowGpsAccuracy;
            mTextGpsAccuracy.setText("");
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Initialise activity.
     */
    private void init() {
        mLocationHelper = new LocationHelper(this);
    }

    /**
     * Set the GUI.
     */
    private void setGui() {
        Toolbar toolbar = ButterKnife.findById(this, R.id.monitor_toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        NavigationView navigationView = ButterKnife.findById(this,
                R.id.monitor_navigation_view);
        View header = getLayoutInflater().inflate(
                R.layout.nav_header_monitor, navigationView, false);
        navigationView.addHeaderView(header);
        navigationView.setNavigationItemSelectedListener(this);

        mGpsStatusHandler.postDelayed(mGpsStatusTask,
                LocationHandler.LOCATION_REQUEST_INTERVAL * 2);

        setTiles();
    }

    /**
     * Create and add tiles to the Tile List.
     */
    private void setTiles() {

        int leftData = mSharedPref.getInt(Pref.TILE_LEFT, Tile.DISTANCE);
        int topRightData = mSharedPref.getInt(Pref.TILE_RIGHT_TOP, Tile.SPEED);
        int bottomRightData = mSharedPref.getInt(
                Pref.TILE_RIGHT_BOTTOM, Tile.PACE);

        mTiles = new ArrayList<>();
        mTiles.add(new Tile(this, this, Pref.TILE_LEFT, leftData,
                (RelativeLayout) ButterKnife.findById(
                        this, R.id.monitor_layout_left),
                (TextView) ButterKnife.findById(
                        this, R.id.monitor_txt_left_value),
                (TextView) ButterKnife.findById(
                        this, R.id.monitor_txt_left_unit)));
        mTiles.add(new Tile(this, this, Pref.TILE_RIGHT_TOP, topRightData,
                (RelativeLayout) ButterKnife.findById(
                        this, R.id.monitor_layout_right_top),
                (TextView) ButterKnife.findById(
                        this, R.id.monitor_txt_right_top_value),
                (TextView) ButterKnife.findById(
                        this, R.id.monitor_txt_right_top_unit)));
        mTiles.add(new Tile(this, this, Pref.TILE_RIGHT_BOTTOM, bottomRightData,
                (RelativeLayout) ButterKnife.findById(
                        this, R.id.monitor_layout_right_bottom),
                (TextView) ButterKnife.findById(
                        this, R.id.monitor_txt_right_bottom_value),
                (TextView) ButterKnife.findById(
                        this, R.id.monitor_txt_right_bottom_unit)));
    }

    @Override
    public final void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (mLocked) {
                Toast.makeText(this, R.string.monitor_session_locked,
                        Toast.LENGTH_LONG).show();
            } else {
                if (mSessionRunning) {
                    stopSession();
                }
                super.onBackPressed();
            }
        }
    }

    @Override
    public final boolean onNavigationItemSelected(
            @NonNull final MenuItem item) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        // Start activity a bit later to avoid the lag when closing the drawer
        mDrawerLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                int id = item.getItemId();
                if (id == R.id.nav_history) {
                    Intent intent = new Intent(MonitorActivity.this,
                            HistoryActivity.class);
                    if (mSessionRunning) {
                        intent.putExtra(HistoryActivity.ARG_IGNORE_SESSION_ID,
                                mRecorder.getSession().getId());
                    }
                    startActivity(intent);
                } else if (id == R.id.nav_settings) {
                    Intent intent = new Intent(MonitorActivity.this,
                            SettingsActivity.class);
                    startActivity(intent);
                }
            }
        }, 300);

        return true;
    }

    @Override
    protected final void onActivityResult(final int requestCode,
                                          final int resultCode,
                                          final Intent data) {
        switch (requestCode) {
            case LocationHandler.REQUEST_LOCATION_AUTO:
                if (resultCode == RESULT_OK) {
                    if (mLocationHelper != null) {
                        mLocationHelper.start();
                    }
                }
                break;
            case LocationHelper.REQUEST_LOCATION_MANUAL:
                if (LocationHelper.isLocationEnabled(this)) {
                    if (mLocationHelper != null) {
                        mLocationHelper.start();
                    }
                }
            default:
                break;
        }
    }

    @Override
    public final void onDurationUpdated(final long duration) {
        mTextTop.setText(Format.formatDuration(duration));
    }

    @Override
    public final void onDistanceUpdated(final float distance) {
        updateTile(Tile.DISTANCE, Format.formatDistance(distance));
        long elapsedTime = mRecorder.getCurrentDuration();

        // Speed average
        updateTile(Tile.SPEED_AVG,
                Format.formatSpeedAvg(elapsedTime, distance));

        // Pace average
        updateTile(Tile.PACE_AVG,
                Format.formatPaceAvg(elapsedTime, distance));
    }

    /**
     * Triggered when new coordinates are received.
     *
     * @param event Coordinate event
     */
    @Subscribe
    public final void onCoordinateEvent(final CoordinateEvent event) {
        mNewGpsDataReceived = true;
    }

    /**
     * Triggered when a new distance is calculated.
     *
     * @param event Distance event
     */
    @Subscribe
    public final void onDistanceEvent(final DistanceEvent event) {
        if ((!mSessionRunning || mSessionPaused) && mShowGpsAccuracy) {
            mTextGpsAccuracy.setText(Format.formatGpsAccuracy(
                    event.getValue()));
        }
    }

    /**
     * Triggered when a new speed is calculated.
     *
     * @param event Speed event
     */
    @Subscribe
    public final void onSpeedEvent(final SpeedEvent event) {
        if (mSessionRunning && !mSessionPaused) {
            updateTile(Tile.SPEED, Format.formatSpeed(event.getValue()));
        }
    }

    /**
     * Triggered when a new pace is calculated.
     *
     * @param event Pace event
     */
    @Subscribe
    public final void onPaceEvent(final PaceEvent event) {
        if (mSessionRunning && !mSessionPaused) {
            updateTile(Tile.PACE, Format.formatPace(event.getValue()));
        }
    }

    /**
     * Update the tiles containing the data type with the new value.
     *
     * @param data  Data type
     * @param value New value to set
     */
    private void updateTile(final int data, final String value) {
        mCurrentValues[data] = value;
        for (Tile tile : mTiles) {
            if (tile.getData() == data) {
                tile.setValue(value);
            }
        }
    }

    @Override
    public final void onTileLongClicked() {
        if (mSessionRunning && !mLocked) {
            if (mLockTimeoutOn) {
                mLockTimeoutHandler.removeCallbacks(mLockTimeoutTask);
                mLockTimeoutOn = false;
            }
            mLockTimeoutHandler.postDelayed(mLockTimeoutTask, LOCK_DELAY);
            mLockTimeoutOn = true;
        }
    }

    @Override
    public final void onTileValueRequested(final Tile tile, final int data) {
        tile.setValue(mCurrentValues[data]);
    }

    /**
     * Triggered when the start button is clicked.
     */
    @OnClick(R.id.monitor_button_start)
    public final void onStartButtonClicked() {
        if (!mSessionRunning) {
            startSession();
        } else {
            resumeSession();
        }
    }

    /**
     * Triggered when the pause button is clicked.
     */
    @OnClick(R.id.monitor_button_pause)
    public final void onPauseButtonClicked() {
        if (mSessionRunning) {
            pauseSession();
        }
    }

    /**
     * Triggered when the stop button is clicked.
     */
    @OnClick(R.id.monitor_button_stop)
    public final void onStopButtonClicked() {
        if (mSessionRunning) {
            stopSession();
        }
    }

    /**
     * Triggered when the lock button is clicked.
     */
    @OnClick(R.id.monitor_button_lock)
    public final void onLockButtonClicked() {
        //if (mSessionRunning) {
            setLocked(false);
            mLockTimeoutHandler.postDelayed(mLockTimeoutTask, LOCK_DELAY);
        mLockTimeoutOn = true;
        //}
    }

    /**
     * Task to make the stopwatch blink when the session is paused.
     */
    private final Runnable mStopwatchBlinkTask = new Runnable() {
        @Override
        public void run() {
            mStopwatchBlinkHandler.postDelayed(this, BLINK_DELAY);
            int currentVisibility = mTextTop.getVisibility();
            if (currentVisibility == View.VISIBLE) {
                mTextTop.setVisibility(View.GONE);
            } else {
                mTextTop.setVisibility(View.VISIBLE);
            }
        }
    };

    /**
     * Task to lock the screen again after timeout.
     */
    private final Runnable mLockTimeoutTask = new Runnable() {
        @Override
        public void run() {
            mLockTimeoutOn = false;
            setLocked(true);
        }
    };

    /**
     * Task checking for the GPS status and updating the Views according to the
     * status.
     */
    private final Runnable mGpsStatusTask = new Runnable() {
        @Override
        public void run() {
            mGpsStatusHandler.postDelayed(this,
                    LocationHandler.LOCATION_REQUEST_INTERVAL * 2);
            if (!LocationHelper.isLocationEnabled(MonitorActivity.this)) {
                mGpsStatusIconId = R.drawable.ic_gps_off_white_24dp;
                mTextGpsAccuracy.setText("");
            } else if (mNewGpsDataReceived) {
                mGpsStatusIconId = R.drawable.ic_gps_fixed_white_24dp;
            } else {
                mGpsStatusIconId = R.drawable.ic_gps_not_fixed_white_24dp;
                mTextGpsAccuracy.setText("");
            }
            mNewGpsDataReceived = false;
            invalidateOptionsMenu();
        }
    };

    /**
     * Set keep screen on flag according to the preferences.
     */
    private void setKeepScreenOn() {
        boolean keepScreenOn = mSharedPref.getBoolean(
                Pref.KEEP_SCREEN_ON, false);
        if (keepScreenOn) {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * Start a session.
     */
    private void startSession() {
        mRecorder = new Recorder(this);
        mRecorder.start();
        setLocked(true);
        updateActionButton(ACTION_START);
        mTextGpsAccuracy.setText("");
        mSessionRunning = true;
    }

    /**
     * Resume the session, called only when the session was paused.
     */
    private void resumeSession() {
        setLocked(true);
        mStopwatchBlinkHandler.removeCallbacks(mStopwatchBlinkTask);
        mTextTop.setVisibility(View.VISIBLE);
        updateActionButton(ACTION_RESUME);
        mTextGpsAccuracy.setText("");
        mSessionPaused = false;
        mRecorder.resume();
    }

    /**
     * Set the session on pause.
     */
    private void pauseSession() {
        if (mLockTimeoutOn) {
            mLockTimeoutHandler.removeCallbacks(mLockTimeoutTask);
            mLockTimeoutOn = false;
        }
        mStopwatchBlinkHandler.post(mStopwatchBlinkTask);
        updateActionButton(ACTION_PAUSE);
        mSessionPaused = true;
        mRecorder.pause();
    }

    /**
     * Stop the current session.
     */
    private void stopSession() {
        mRecorder.stop();

        if (mLockTimeoutOn) {
            mLockTimeoutHandler.removeCallbacks(mLockTimeoutTask);
            mLockTimeoutOn = false;
        }
        if (mSessionPaused) {
            mStopwatchBlinkHandler.removeCallbacks(mStopwatchBlinkTask);
            mTextTop.setVisibility(View.VISIBLE);
        }

        updateActionButton(ACTION_STOP);

        Intent intent = new Intent(this, HistoryActivity.class);
        intent.putExtra(HistoryActivity.ARG_HIGHLIGHTED_SESSION_ID,
                mRecorder.getSession().getId());
        startActivity(intent);

        resetViews();
        initCurrentValues();

        mSessionRunning = false;
    }

    /**
     * Set the lock status and set the view according to the status.
     *
     * @param locked True if the session is to be locked, false otherwise
     */
    private void setLocked(final boolean locked) {
        mLocked = locked;

        for (Tile tile : mTiles) {
            tile.setEnabled(!locked);
        }

        if (mLocked) {
            updateActionButton(ACTION_LOCK);
            mDrawerLayout.setDrawerLockMode(
                    DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            updateActionButton(ACTION_UNLOCK);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
        mDrawerToggle.setDrawerIndicatorEnabled(!locked);
        mDrawerToggle.syncState();

        invalidateOptionsMenu();
    }

    /**
     * Reset the views that are updated during a session.
     */
    private void resetViews() {
        mTextTop.setText(R.string.default_stopwatch);
        for (Tile tile : mTiles) {
            tile.resetValues();
        }
    }

    /**
     * Initialise the current values strings with their default values.
     */
    private void initCurrentValues() {
        mCurrentValues = new String[]{
                getString(R.string.default_distance),
                getString(R.string.default_speed),
                getString(R.string.default_pace),
                getString(R.string.default_speed),
                getString(R.string.default_pace),
        };
    }

    /**
     * Update the action buttons according to the given action.
     *
     * @param action Action triggered.
     */
    private void updateActionButton(final int action) {
        switch (action) {
            case ACTION_START:
                mLayoutStart.setVisibility(View.GONE);
                mLayoutLock.setVisibility(View.VISIBLE);
                mLayoutPause.setVisibility(View.GONE);
                mLayoutStop.setVisibility(View.GONE);
                break;
            case ACTION_RESUME:
                mLayoutStart.setVisibility(View.GONE);
                mLayoutLock.setVisibility(View.VISIBLE);
                mLayoutPause.setVisibility(View.GONE);
                mLayoutStop.setVisibility(View.GONE);
                break;
            case ACTION_PAUSE:
                mLayoutStart.setVisibility(View.VISIBLE);
                mLayoutLock.setVisibility(View.GONE);
                mLayoutPause.setVisibility(View.GONE);
                mLayoutStop.setVisibility(View.VISIBLE);
                break;
            case ACTION_STOP:
                mLayoutStart.setVisibility(View.VISIBLE);
                mLayoutLock.setVisibility(View.GONE);
                mLayoutPause.setVisibility(View.GONE);
                mLayoutStop.setVisibility(View.GONE);
                break;
            case ACTION_LOCK:
                mLayoutStart.setVisibility(View.GONE);
                mLayoutLock.setVisibility(View.VISIBLE);
                mLayoutPause.setVisibility(View.GONE);
                mLayoutStop.setVisibility(View.GONE);
                break;
            case ACTION_UNLOCK:
                mLayoutStart.setVisibility(View.GONE);
                mLayoutLock.setVisibility(View.GONE);
                mLayoutPause.setVisibility(View.VISIBLE);
                mLayoutStop.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    @Override
    public final void onSharedPreferenceChanged(
            final SharedPreferences sharedPreferences, final String key) {
        if (key.equals(Pref.KEEP_SCREEN_ON)) {
            setKeepScreenOn();
        }
    }
}
