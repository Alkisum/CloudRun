package com.alkisum.android.cloudrun.database;

import android.os.AsyncTask;

import com.alkisum.android.cloudrun.events.RestoreRouteEvent;
import com.alkisum.android.cloudrun.model.DaoSession;
import com.alkisum.android.cloudrun.model.MarkerDao;
import com.alkisum.android.cloudrun.model.Route;
import com.alkisum.android.cloudrun.model.RouteDao;

import org.greenrobot.eventbus.EventBus;

/**
 * Class restoring the deleted routes in the database.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public class RouteRestorer extends AsyncTask<Route, Void, Void> {

    /**
     * RouteRestorer constructor.
     */
    public RouteRestorer() {

    }

    @Override
    protected final Void doInBackground(final Route... routes) {
        DaoSession daoSession = Db.getInstance().getDaoSession();
        RouteDao routeDao = daoSession.getRouteDao();
        MarkerDao markerDao = daoSession.getMarkerDao();
        for (Route route : routes) {
            routeDao.insert(route);
            markerDao.insertInTx(route.getMarkers());
        }
        return null;
    }

    @Override
    protected final void onPostExecute(final Void param) {
        EventBus.getDefault().post(new RestoreRouteEvent());
    }
}
