package com.alkisum.android.cloudrun.database;

import android.os.Handler;

import com.alkisum.android.cloudrun.events.CoordinateEvent;
import com.alkisum.android.cloudrun.events.DistanceEvent;
import com.alkisum.android.cloudrun.location.Coordinate;
import com.alkisum.android.cloudrun.model.DataPoint;
import com.alkisum.android.cloudrun.model.DataPointDao;
import com.alkisum.android.cloudrun.model.Session;
import com.alkisum.android.cloudrun.model.SessionDao;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Class recording GPS data.
 *
 * @author Alkisum
 * @version 3.0
 * @since 1.0
 */
public class Recorder {

    /**
     * Listener for recorder.
     */
    private final RecorderListener callback;

    /**
     * EventBus instance.
     */
    private EventBus eventBus;

    /**
     * Session instance.
     */
    private Session session;

    /**
     * Handler for duration task.
     */
    private final Handler durationHandler = new Handler();

    /**
     * Time when the pause started.
     */
    private long pauseStart;

    /**
     * Total duration when the session was on paused.
     */
    private long pauseDuration;

    /**
     * Recorder constructor.
     *
     * @param callback Recorder listener
     */
    public Recorder(final RecorderListener callback) {
        this.callback = callback;
    }

    /**
     * Start recording: insert new session.
     */
    public final void start() {
        session = new Session();
        session.setStart(System.currentTimeMillis());
        session.setDuration(0L);
        session.setDistance(0f);
        SessionDao sessionDao = Db.getInstance().getDaoSession()
                .getSessionDao();
        sessionDao.insert(session);

        durationHandler.postDelayed(durationTask, 1000);

        eventBus = EventBus.getDefault();
        eventBus.register(this);
    }

    /**
     * Resume the recorder.
     */
    public final void resume() {
        pauseDuration += System.currentTimeMillis() - pauseStart;
        durationHandler.postDelayed(durationTask, 1000);
        eventBus.register(this);
    }

    /**
     * Pause the recorder.
     */
    public final void pause() {
        eventBus.unregister(this);
        durationHandler.removeCallbacks(durationTask);
        pauseStart = System.currentTimeMillis();
    }

    /**
     * Stop recording: update current session.
     */
    public final void stop() {
        eventBus.unregister(this);

        durationHandler.removeCallbacks(durationTask);

        session.setEnd(System.currentTimeMillis());
        session.update();
    }

    /**
     * Task to increment the duration and update the value in the database.
     */
    private final Runnable durationTask = new Runnable() {
        @Override
        public void run() {
            durationHandler.postDelayed(this, 1000);
            long newDuration = System.currentTimeMillis() - session.getStart()
                    - pauseDuration;
            session.setDuration(newDuration);
            session.update();
            callback.onDurationUpdated(newDuration);
        }
    };

    /**
     * Triggered when a new distance is calculated. Add distance to session.
     *
     * @param event Distance event
     */
    @Subscribe
    public final void onDistanceEvent(final DistanceEvent event) {
        Float currentDistance = session.getDistance();
        float newDistance = currentDistance + event.getValue();
        session.setDistance(newDistance);
        session.update();
        callback.onDistanceUpdated(newDistance);
    }

    /**
     * Triggered when new coordinates are received. Insert the data into the
     * database.
     *
     * @param event Coordinate event
     */
    @Subscribe
    public final void onCoordinateEvent(final CoordinateEvent event) {
        Coordinate c = event.getValues();
        DataPoint dataPoint = new DataPoint(null, System.currentTimeMillis(),
                c.getLatitude(), c.getLongitude(), c.getElevation(),
                session.getId());
        DataPointDao dataPointDao = Db.getInstance().getDaoSession()
                .getDataPointDao();
        dataPointDao.insert(dataPoint);
    }

    /**
     * @return Session instance
     */
    public final Session getSession() {
        return session;
    }

    /**
     * @return Current session's duration
     */
    public final long getCurrentDuration() {
        return session.getDuration();
    }

    /**
     * Listener for recorder.
     */
    public interface RecorderListener {

        /**
         * Called when the duration is updated.
         *
         * @param duration New duration
         */
        void onDurationUpdated(long duration);

        /**
         * Called when the distance is updated.
         *
         * @param distance New distance.
         */
        void onDistanceUpdated(float distance);
    }
}
