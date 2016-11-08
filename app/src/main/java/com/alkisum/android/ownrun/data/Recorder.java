package com.alkisum.android.ownrun.data;

import android.os.Handler;

import com.alkisum.android.ownrun.event.CoordinateEvent;
import com.alkisum.android.ownrun.event.DistanceEvent;
import com.alkisum.android.ownrun.location.Coordinate;
import com.alkisum.android.ownrun.model.DataPoint;
import com.alkisum.android.ownrun.model.DataPointDao;
import com.alkisum.android.ownrun.model.Session;
import com.alkisum.android.ownrun.model.SessionDao;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Class recording GPS data.
 *
 * @author Alkisum
 * @version 2.0
 * @since 1.0
 */
public class Recorder {

    /**
     * Listener for recorder.
     */
    private final RecorderListener mCallback;

    /**
     * EventBus instance.
     */
    private EventBus mEventBus;

    /**
     * Session instance.
     */
    private Session mSession;

    /**
     * Handler for duration task.
     */
    private final Handler mDurationHandler = new Handler();

    /**
     * Recorder constructor.
     *
     * @param callback Recorder listener
     */
    public Recorder(final RecorderListener callback) {
        mCallback = callback;
    }

    /**
     * Start recording: insert new session.
     */
    public final void start() {
        mSession = new Session();
        mSession.setStart(System.currentTimeMillis());
        mSession.setDuration(0L);
        mSession.setDistance(0f);
        SessionDao sessionDao = Db.getInstance().getDaoSession()
                .getSessionDao();
        sessionDao.insert(mSession);

        mDurationHandler.postDelayed(mDurationTask, 1000);

        mEventBus = EventBus.getDefault();
        mEventBus.register(this);
    }

    /**
     * Resume the recorder.
     */
    public final void resume() {
        mDurationHandler.postDelayed(mDurationTask, 1000);
        mEventBus.register(this);
    }

    /**
     * Pause the recorder.
     */
    public final void pause() {
        mEventBus.unregister(this);
        mDurationHandler.removeCallbacks(mDurationTask);
    }

    /**
     * Stop recording: update current session.
     */
    public final void stop() {
        mEventBus.unregister(this);

        mDurationHandler.removeCallbacks(mDurationTask);

        mSession.setEnd(System.currentTimeMillis());
        mSession.update();
    }

    /**
     * Task to increment the duration and update the value in the database.
     */
    private final Runnable mDurationTask = new Runnable() {
        @Override
        public void run() {
            mDurationHandler.postDelayed(this, 1000);
            Long currentDuration = mSession.getDuration();
            long newDuration = currentDuration + 1000;
            mSession.setDuration(newDuration);
            mSession.update();
            mCallback.onDurationUpdated(newDuration);
        }
    };

    /**
     * Triggered when a new distance is calculated. Add distance to session.
     *
     * @param event Distance event
     */
    @Subscribe
    public final void onDistanceEvent(final DistanceEvent event) {
        Float currentDistance = mSession.getDistance();
        float newDistance = currentDistance + event.getValue();
        mSession.setDistance(newDistance);
        mSession.update();
        mCallback.onDistanceUpdated(newDistance);
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
                mSession.getId());
        DataPointDao dataPointDao = Db.getInstance().getDaoSession()
                .getDataPointDao();
        dataPointDao.insert(dataPoint);
    }

    /**
     * @return Session instance
     */
    public final Session getSession() {
        return mSession;
    }

    /**
     * @return Current session's duration
     */
    public final long getCurrentDuration() {
        return mSession.getDuration();
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
