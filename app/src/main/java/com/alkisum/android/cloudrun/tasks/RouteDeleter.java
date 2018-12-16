package com.alkisum.android.cloudrun.tasks;

import android.os.AsyncTask;

import com.alkisum.android.cloudrun.database.Db;
import com.alkisum.android.cloudrun.utils.Routes;
import com.alkisum.android.cloudrun.events.RouteDeletedEvent;
import com.alkisum.android.cloudrun.model.DaoSession;
import com.alkisum.android.cloudrun.model.Route;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Class deleting the selected routes from the database.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public class RouteDeleter extends AsyncTask<Void, Void, List<Route>> {

    /**
     * Subscriber ids allowed to process the events.
     */
    private final Integer[] subscriberIds;

    /**
     * RouteDeleter constructor.
     *
     * @param subscriberIds Subscriber ids allowed to process the events
     */
    public RouteDeleter(final Integer[] subscriberIds) {
        this.subscriberIds = subscriberIds;
    }

    @Override
    protected final List<Route> doInBackground(final Void... voids) {
        DaoSession daoSession = Db.getInstance().getDaoSession();
        List<Route> routes = Routes.getSelectedRoutes();
        for (Route route : routes) {
            daoSession.getMarkerDao().deleteInTx(route.getMarkers());
            route.delete();
        }
        return routes;
    }

    @Override
    protected final void onPostExecute(final List<Route> routes) {
        EventBus.getDefault().post(new RouteDeletedEvent(subscriberIds, routes));
    }
}
