package com.alkisum.android.cloudrun.location;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.activities.MonitorActivity;
import com.alkisum.android.cloudrun.events.SessionActionEvent;
import com.alkisum.android.cloudrun.model.Session;
import com.alkisum.android.cloudrun.utils.Format;
import com.alkisum.android.cloudrun.utils.Sessions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import androidx.core.app.NotificationCompat;

/**
 * Bound and started service that can be promoted to a foreground service.
 *
 * @author Alkisum
 * @version 4.1
 * @since 3.1
 */
public class LocationUpdatesService extends Service {

    /**
     * Binder.
     */
    private final IBinder binder = new LocalBinder();

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 691;

    /**
     * Delay to update notification.
     */
    private long notificationDelay = 1000;

    /**
     * Service handler.
     */
    private Handler serviceHandler;

    /**
     * Handler for notification update task.
     */
    private final Handler notificationUpdateHandler = new Handler();

    /**
     * Flag set to true if the notification update task is on, false otherwise.
     */
    private boolean notificationUpdateOn = false;

    /**
     * Notification builder.
     */
    private NotificationCompat.Builder notificationBuilder;

    /**
     * Notification manager.
     */
    private NotificationManager notificationManager;

    /**
     * EventBus instance.
     */
    private EventBus eventBus;

    /**
     * Notification action to pause session. Can be replaced by the resume
     * action if the session is on pause.
     */
    private NotificationCompat.Action pauseAction;

    /**
     * Notification action to resume session. Can be replaced by the pause
     * action if the session is running.
     */
    private NotificationCompat.Action resumeAction;

    /**
     * Notification action to stop session.
     */
    private NotificationCompat.Action stopAction;

    /**
     * Flag set to true when the blinking mode is enabled (session on pause),
     * false otherwise.
     */
    private boolean blinkEnabled = false;

    /**
     * Flag set to true if the duration is visible (used for blinking mode),
     * false otherwise.
     */
    private boolean durationVisible = true;

    @Override
    public final void onCreate() {
        HandlerThread handlerThread = new HandlerThread(
                LocationUpdatesService.class.getSimpleName());
        handlerThread.start();
        serviceHandler = new Handler(handlerThread.getLooper());
        initNotification();
        notificationBuilder.addAction(pauseAction);
        notificationBuilder.addAction(stopAction);
        eventBus = EventBus.getDefault();
        eventBus.register(this);
    }

    @Override
    public final int onStartCommand(final Intent intent, final int flags,
                                    final int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public final IBinder onBind(final Intent intent) {
        return binder;
    }

    @Override
    public final boolean onUnbind(final Intent intent) {
        return false;
    }

    @Override
    public final void onDestroy() {
        eventBus.unregister(this);
        unregisterReceiver(actionReceiver);
        serviceHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Make the service run in the foreground.
     */
    public final void startForeground() {
        startForeground(NOTIFICATION_ID, getNotification());
        if (!notificationUpdateOn) {
            notificationUpdateHandler.post(notificationUpdateTask);
        }
    }

    /**
     * Remove the service from foreground state.
     */
    public final void stopForeground() {
        stopForeground(true);
        if (notificationUpdateOn) {
            notificationUpdateHandler.removeCallbacks(notificationUpdateTask);
            notificationUpdateOn = false;
        }
    }

    /**
     * @return the notification used as part of the foreground service
     */
    private Notification getNotification() {
        updateNotification();
        return notificationBuilder.build();
    }

    /**
     * Notification update task.
     */
    private final Runnable notificationUpdateTask = new Runnable() {
        @Override
        public void run() {
            notificationUpdateOn = true;
            notificationUpdateHandler.postDelayed(this, notificationDelay);
            updateNotification();
            notificationManager.notify(NOTIFICATION_ID,
                    notificationBuilder.build());
        }
    };

    /**
     * Initialize notification.
     */
    private void initNotification() {
        PendingIntent activityPendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, MonitorActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    "CloudRunLocation", "CloudRun location",
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationBuilder = new NotificationCompat.Builder(
                this, "CloudRunLocation")
                .setContentIntent(activityPendingIntent)
                .setSmallIcon(R.drawable.ic_directions_run_white_24dp);

        // add actions to intent filter
        this.addIntentFilterActions();

        // add actions to notification builder
        this.createNotificationActions();
    }

    /**
     * BroadcastReceiver for notification actions.
     */
    private final BroadcastReceiver actionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction() == null) {
                return;
            }
            EventBus.getDefault().post(
                    new SessionActionEvent(intent.getAction()));
        }
    };

    /**
     * Add actions to intent filter to be able to receive them using the
     * BroadcastReceiver.
     */
    private void addIntentFilterActions() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SessionActionEvent.PAUSE);
        intentFilter.addAction(SessionActionEvent.RESUME);
        intentFilter.addAction(SessionActionEvent.STOP);
        registerReceiver(actionReceiver, intentFilter);
    }

    /**
     * Create notification actions.
     */
    private void createNotificationActions() {
        // create pause action
        pauseAction = new NotificationCompat.Action(
                R.drawable.ic_pause_white_48dp,
                getString(R.string.action_pause),
                PendingIntent.getBroadcast(
                        this, 0, new Intent(SessionActionEvent.PAUSE), 0));

        // create resume action
        resumeAction = new NotificationCompat.Action(
                R.drawable.ic_play_arrow_white_48dp,
                getString(R.string.action_resume),
                PendingIntent.getBroadcast(
                        this, 0, new Intent(SessionActionEvent.RESUME), 0));

        // create stop action
        stopAction = new NotificationCompat.Action(
                R.drawable.ic_stop_white_48dp,
                getString(R.string.action_stop),
                PendingIntent.getBroadcast(
                        this, 0, new Intent(SessionActionEvent.STOP), 0));
    }

    /**
     * Update notification.
     */
    private void updateNotification() {
        // get current session
        Session session = Sessions.getLastSession();

        // init values
        String duration = "00:00:00";
        String distance = "0 " + getString(R.string.unit_distance);

        if (session != null) {
            // get values from current session
            duration = Format.formatDuration(session.getDuration());
            distance = Format.formatDistance(session.getDistance())
                    + " " + getString(R.string.unit_distance);
        }

        // set duration (handle blink when on pause)
        if (blinkEnabled) {
            if (durationVisible) {
                duration = "";
            }
            durationVisible = !durationVisible;
        }
        notificationBuilder.setContentTitle(duration);

        // set distance
        notificationBuilder.setContentText(distance);

        // set notification time
        notificationBuilder.setWhen(System.currentTimeMillis());
    }

    /**
     * Called when a session action has been performed.
     *
     * @param event Session action event
     */
    @Subscribe
    public final void onSessionActionEvent(final SessionActionEvent event) {
        switch (event.getAction()) {
            case SessionActionEvent.START:
                // set actions
                initNotification();
                notificationBuilder.addAction(pauseAction);
                notificationBuilder.addAction(stopAction);

                // deactivate blinking
                this.setBlink(false);

                // update notification
                updateNotification();
                break;
            case SessionActionEvent.RESUME:
                // set actions
                initNotification();
                notificationBuilder.addAction(pauseAction);
                notificationBuilder.addAction(stopAction);

                // deactivate blinking
                this.setBlink(false);

                // update notification
                updateNotification();

                // push new notification
                if (notificationUpdateOn) {
                    notificationManager.notify(NOTIFICATION_ID,
                            notificationBuilder.build());
                }
                break;
            case SessionActionEvent.PAUSE:
                // set actions
                initNotification();
                notificationBuilder.addAction(resumeAction);
                notificationBuilder.addAction(stopAction);

                // activate blinking
                this.setBlink(true);

                // update notification
                updateNotification();

                // push new notification
                if (notificationUpdateOn) {
                    notificationManager.notify(NOTIFICATION_ID,
                            notificationBuilder.build());
                }
                break;
            case SessionActionEvent.STOP:
                // remove notification
                this.stopForeground();
                break;
            default:
                break;
        }
    }

    /**
     * Set blinking mode (enabled or disabled) and adjust notification delay
     * according to the given mode.
     *
     * @param enabled Blinking mode
     */
    private void setBlink(final boolean enabled) {
        blinkEnabled = enabled;
        if (enabled) {
            notificationDelay = 500;
        } else {
            notificationDelay = 1000;
        }
    }

    /**
     * Class used for the client Binder.
     */
    final class LocalBinder extends Binder {

        /**
         * @return LocationUpdatesService
         */
        LocationUpdatesService getService() {
            return LocationUpdatesService.this;
        }
    }
}
