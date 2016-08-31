package com.alkisum.android.ownrun.data;

import com.alkisum.android.ownrun.event.CoordinateEvent;
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
 * @version 1.0
 * @since 1.0
 */
public class Recorder {

    /**
     * EventBus instance.
     */
    private EventBus mEventBus;

    /**
     * Session instance.
     */
    private Session mSession;

    /**
     * Recorder constructor.
     */
    public Recorder() {

    }

    /**
     * Start recording: insert new session.
     */
    public final void start() {
        mSession = new Session();
        mSession.setStart(System.currentTimeMillis());
        SessionDao sessionDao = Db.getInstance().getDaoSession()
                .getSessionDao();
        sessionDao.insert(mSession);

        mEventBus = EventBus.getDefault();
        mEventBus.register(this);
    }

    /**
     * Stop recording: update current session.
     *
     * @param distance Total distance in meter ran during the session
     */
    public final void stop(final float distance) {
        mEventBus.unregister(this);

        mSession.setEnd(System.currentTimeMillis());
        mSession.setDistance(distance);
        SessionDao sessionDao = Db.getInstance().getDaoSession()
                .getSessionDao();
        sessionDao.update(mSession);
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
        return System.currentTimeMillis() - mSession.getStart();
    }
}
