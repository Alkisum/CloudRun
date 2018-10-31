package com.alkisum.android.cloudrun.location;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.activities.MapActivity;
import com.alkisum.android.cloudrun.database.Markers;
import com.alkisum.android.cloudrun.events.MarkerAlertEvent;
import com.alkisum.android.cloudrun.model.Marker;
import com.google.android.gms.location.LocationCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Class notifying when a marker alert occurred.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
class MarkerNotifier {

    /**
     * The identifier for the notification.
     */
    private static final int NOTIFICATION_ID = 234;

    /**
     * Activity instance wrapped into WeakReference object to avoid the activity
     * to be leaked with {@link LocationCallback}.
     */
    private final WeakReference<Activity> activity;

    /**
     * MarkerNotifier constructor.
     *
     * @param activity Activity
     */
    MarkerNotifier(final Activity activity) {
        this.activity = new WeakReference<>(activity);
        EventBus.getDefault().register(this);
    }

    /**
     * Called when {@link LocationUpdatesService} is destroyed.
     */
    final void onDestroy() {
        EventBus.getDefault().unregister(this);
    }


    /**
     * Triggered when a surrounding marker has been detected.
     *
     * @param event Marker alert event
     */
    @Subscribe
    public final void onMarkerAlertEvent(final MarkerAlertEvent event) {
        // initialize notification manager
        NotificationManager manager = initNotification();

        // build notification
        NotificationCompat.Builder builder = buildNotification(
                event.getMarkers());

        // notify
        manager.notify(NOTIFICATION_ID, builder.build());
    }

    /**
     * Initialize notification.
     *
     * @return Notification manager
     */
    private NotificationManager initNotification() {
        NotificationManager notificationManager = (NotificationManager)
                activity.get().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    "CloudRunMarker", "CloudRun marker",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        return notificationManager;
    }

    /**
     * Build notification using the given marker.
     *
     * @param markers Markers
     * @return Notification builder
     */
    private NotificationCompat.Builder buildNotification(
            final List<Marker> markers) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                activity.get(), "CloudRunMarker");
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.ic_near_me_white_24dp);
        builder.setContentIntent(buildPendingIntent(
                Markers.toCoordinate(markers.get(0))));
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle(this.buildTitle(markers));
        return builder;
    }

    /**
     * Build title by concatenating the marker labels.
     *
     * @param markers List of markers
     * @return Content title
     */
    private StringBuilder buildTitle(final List<Marker> markers) {
        StringBuilder title = new StringBuilder();
        for (int i = 0; i < markers.size(); i++) {
            title.append(markers.get(i).getLabel());
            if (i < markers.size() - 1) {
                title.append(" > ");
            }
        }
        return title;
    }

    /**
     * Build pending intent to {@link MapActivity} putting the current
     * coordinate as extra.
     *
     * @param coordinate Coordinate
     * @return Pending intent
     */
    private PendingIntent buildPendingIntent(final Coordinate coordinate) {
        Intent intent = new Intent(activity.get(), MapActivity.class);
        intent.putExtra(MapActivity.ARG_COORDINATE, coordinate);
        return PendingIntent.getActivity(activity.get(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
