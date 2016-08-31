package com.alkisum.android.ownrun.monitor;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alkisum.android.ownrun.R;
import com.alkisum.android.ownrun.data.Db;
import com.alkisum.android.ownrun.data.Recorder;
import com.alkisum.android.ownrun.event.CoordinateEvent;
import com.alkisum.android.ownrun.event.DistanceEvent;
import com.alkisum.android.ownrun.history.HistoryActivity;
import com.alkisum.android.ownrun.location.LocationHandler;
import com.alkisum.android.ownrun.location.LocationHelper;
import com.alkisum.android.ownrun.utils.ErrorDialog;
import com.alkisum.android.ownrun.utils.Format;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Main activity showing location values.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public class MonitorActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * Request code for denied permissions.
     */
    private static final int REQUEST_CODE = 683;

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
     * Flag set to true if the session is locked, false otherwise.
     */
    private boolean mLocked;

    /**
     * Cumulative distance during the current session.
     */
    private float mDistance;

    /**
     * Flag set to true when new GPS data has been received, false otherwise.
     */
    private boolean mNewGpsDataReceived;

    /**
     * Handler for the stopwatch task.
     */
    private final Handler mStopwatchHandler = new Handler();

    /**
     * Handler for the task checking for the GPS status.
     */
    private final Handler mGpsStatusHandler = new Handler();

    /**
     * TextView showing distance value.
     */
    @BindView(R.id.monitor_txt_distance)
    TextView mTextDistance;

    /**
     * TextView showing speed value.
     */
    @BindView(R.id.monitor_txt_speed)
    TextView mTextSpeed;

    /**
     * TextView showing pace value.
     */
    @BindView(R.id.monitor_txt_pace)
    TextView mTextPace;

    /**
     * TextView showing the stopwatch.
     */
    @BindView(R.id.monitor_txt_stopwatch)
    TextView mTextStopwatch;

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
     * Image showing the GPS status in the Toolbar.
     */
    @BindView(R.id.monitor_img_gps_status)
    ImageView mImageGpsStatus;

    /**
     * Button starting or stopping the session.
     */
    @BindView(R.id.monitor_button_action)
    Button mButtonAction;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO Delete statement
        //deleteDatabase(Db.NAME);

        String[] deniedPermissions = getDeniedPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.INTERNET
                }
        );
        if (deniedPermissions.length > 0
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(deniedPermissions, REQUEST_CODE);
        } else {
            initApp();
        }

        mEventBus = EventBus.getDefault();
        mEventBus.register(this);

        setContentView(R.layout.activity_monitor);
        ButterKnife.bind(this);

        setGui();
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
                default:
                    break;
            }
        }
        if (permissionsGranted) {
            initApp();
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

    /**
     * Initialize the application.
     */
    private void initApp() {
        Db.getInstance().init(this);
        mLocationHelper = new LocationHelper(this);
    }

    /**
     * Set the lock status and set the view according to the status.
     *
     * @param locked True if the session is to be locked, false otherwise
     */
    private void setLocked(final boolean locked) {
        mLocked = locked;

        mButtonAction.setEnabled(!mLocked);

        if (!mLocked) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mDrawerToggle.setDrawerIndicatorEnabled(true);
        } else {
            mDrawerLayout.setDrawerLockMode(
                    DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mDrawerToggle.setDrawerIndicatorEnabled(false);
        }
        mDrawerToggle.syncState();

        invalidateOptionsMenu();
    }

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_monitor, menu);
        return true;
    }

    @Override
    public final boolean onPrepareOptionsMenu(final Menu menu) {
        MenuItem locker = menu.findItem(R.id.action_lock);
        if (mLocked) {
            locker.setTitle(R.string.action_unlock);
            locker.setIcon(R.drawable.ic_lock_outline_white_24dp);
        } else {
            locker.setTitle(R.string.action_lock);
            locker.setIcon(R.drawable.ic_lock_open_white_24dp);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_lock) {
            setLocked(!mLocked);
            return true;
        }
        return super.onOptionsItemSelected(item);
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

        setLocked(false);
        mGpsStatusHandler.postDelayed(mGpsStatusTask,
                LocationHandler.LOCATION_REQUEST_INTERVAL * 2);
    }

    @Override
    protected final void onDestroy() {
        mEventBus.unregister(this);

        if (mLocationHelper != null) {
            mLocationHelper.stop();
            mLocationHelper.onDestroy();
        }
        super.onDestroy();
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
            case LocationHandler.REQUEST_CHECK_SETTINGS:
                if (resultCode == RESULT_OK) {
                    if (mLocationHelper != null) {
                        mLocationHelper.start();
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * Triggered when a new distance is calculated. Calculate speed and pace
     * averages from the distance.
     *
     * @param event Distance event
     */
    @Subscribe
    public final void onDistanceEvent(final DistanceEvent event) {
        if (mSessionRunning) {
            mDistance += event.getValue();
            mTextDistance.setText(String.format(Locale.getDefault(),
                    Format.DISTANCE, mDistance / 1000f));
            long elapsedTime = mRecorder.getCurrentDuration();

            // Speed average
            mTextSpeed.setText(Format.formatSpeedAvg(elapsedTime, mDistance));

            // Pace average
            mTextPace.setText(Format.formatPaceAvg(elapsedTime, mDistance));
        }
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
     * Task updating the stopwatch.
     */
    private final Runnable mStopwatchTask = new Runnable() {
        @Override
        public void run() {
            mStopwatchHandler.postDelayed(this, 1000);
            long elapsedTime = mRecorder.getCurrentDuration();
            mTextStopwatch.setText(Format.formatDuration(elapsedTime));
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
                mImageGpsStatus.setImageDrawable(ContextCompat.getDrawable(
                        MonitorActivity.this,
                        R.drawable.ic_gps_off_white_24dp));
            } else if (mNewGpsDataReceived) {
                mImageGpsStatus.setImageDrawable(ContextCompat.getDrawable(
                        MonitorActivity.this,
                        R.drawable.ic_gps_fixed_white_24dp));
            } else {
                mImageGpsStatus.setImageDrawable(ContextCompat.getDrawable(
                        MonitorActivity.this,
                        R.drawable.ic_gps_not_fixed_white_24dp));
            }
            mNewGpsDataReceived = false;
        }
    };

    /**
     * Triggered when the action button is clicked. Start or stop session
     * according to the current session state.
     *
     * @param view View
     */
    public final void onActionButtonClicked(final View view) {
        if (!mSessionRunning) {
            startSession();
        } else {
            stopSession();
        }
        mSessionRunning = !mSessionRunning;
    }

    /**
     * Start a session.
     */
    private void startSession() {
        // Reset distance (do not increment from last session)
        mDistance = 0;

        mRecorder = new Recorder();
        mRecorder.start();

        setLocked(true);
        mStopwatchHandler.post(mStopwatchTask);
        mButtonAction.setText(R.string.action_stop);

        resetViews();
    }

    /**
     * Stop the current session.
     */
    private void stopSession() {
        mRecorder.stop(mDistance);

        mStopwatchHandler.removeCallbacks(mStopwatchTask);
        mButtonAction.setText(R.string.action_start);

        Intent intent = new Intent(this, HistoryActivity.class);
        intent.putExtra(HistoryActivity.ARG_HIGHLIGHTED_SESSION_ID,
                mRecorder.getSession().getId());
        startActivity(intent);

        resetViews();
    }

    /**
     * Reset the views that are updated during a session.
     */
    private void resetViews() {
        mTextStopwatch.setText(R.string.default_stopwatch);
        mTextDistance.setText(R.string.default_distance);
        mTextSpeed.setText(R.string.default_speed);
        mTextPace.setText(R.string.default_pace);
    }
}
