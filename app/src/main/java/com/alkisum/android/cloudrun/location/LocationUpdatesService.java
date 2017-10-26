package com.alkisum.android.cloudrun.location;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.activities.MonitorActivity;
import com.alkisum.android.cloudrun.model.Session;
import com.alkisum.android.cloudrun.utils.Format;
import com.alkisum.android.cloudrun.utils.Sessions;

/**
 * Bound and started service that can be promoted to a foreground service.
 *
 * @author Alkisum
 * @version 3.1
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

    @Override
    public final void onCreate() {
        HandlerThread handlerThread = new HandlerThread(
                LocationUpdatesService.class.getSimpleName());
        handlerThread.start();
        serviceHandler = new Handler(handlerThread.getLooper());
        initNotification();
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
            notificationUpdateHandler.postDelayed(this, 1000);
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
                this, "CloudRunLocation");
        notificationBuilder.setContentIntent(activityPendingIntent);
        notificationBuilder.setSmallIcon(
                R.drawable.ic_directions_run_white_24dp);
    }

    /**
     * Update notification.
     */
    private void updateNotification() {
        Session session = Sessions.getLastSession();
        String duration = "00:00:00";
        String distance = "0 " + getString(R.string.unit_distance);
        if (session != null) {
            duration = Format.formatDuration(session.getDuration());
            distance = Format.formatDistance(session.getDistance())
                    + " " + getString(R.string.unit_distance);
        }
        notificationBuilder.setContentTitle(duration);
        notificationBuilder.setContentText(distance);
        notificationBuilder.setWhen(System.currentTimeMillis());
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
