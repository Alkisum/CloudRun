package com.alkisum.android.cloudrun.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.alkisum.android.cloudrun.model.Route;
import com.alkisum.android.cloudrun.model.RouteDao;
import com.alkisum.android.cloudrun.utils.Pref;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Utility class for route operations.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public final class Routes {

    /**
     * Routes constructor.
     */
    private Routes() {

    }

    /**
     * Load all routes from database.
     *
     * @return List of routes
     */
    public static List<Route> loadRoutes() {
        RouteDao dao = Db.getInstance().getDaoSession().getRouteDao();
        return dao.loadAll();
    }

    /**
     * Load the route from the database according to the given ID.
     *
     * @param routeId Route id
     * @return Route
     */
    public static Route getRouteById(final long routeId) {
        RouteDao dao = Db.getInstance().getDaoSession().getRouteDao();
        return dao.load(routeId);
    }

    /**
     * Load all the routes from the database and return only the selected
     * ones.
     *
     * @return List of selected routes.
     */
    public static List<Route> getSelectedRoutes() {
        RouteDao dao = Db.getInstance().getDaoSession().getRouteDao();
        List<Route> selectedRoutes = new ArrayList<>();
        for (Route route : dao.loadAll()) {
            if (route.getSelected()) {
                selectedRoutes.add(route);
            }
        }
        return selectedRoutes;
    }

    /**
     * Load the active routes by reading the preferences.
     *
     * @param context Context
     * @return List of active routes
     */
    static List<Route> getActiveRoutes(final Context context) {
        List<Route> activeRoutes = new ArrayList<>();

        // get active route ids from preferences
        Set<String> activeRoutesPref = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getStringSet(Pref.ACTIVE_ROUTES, null);

        // return empty list if preference not set
        if (activeRoutesPref == null) {
            return activeRoutes;
        }

        // get all routes by ids read in preferences
        for (String routeId : activeRoutesPref) {
            Route route = getRouteById(Long.parseLong(routeId));
            if (route != null) {
                activeRoutes.add(route);
            }
        }

        return activeRoutes;
    }

    /**
     * Insert a route with the given name into the database.
     *
     * @param name Name of the route to insert
     */
    public static void insertRoute(final String name) {
        RouteDao dao = Db.getInstance().getDaoSession().getRouteDao();
        Route route = new Route();
        route.setName(name);
        dao.insert(route);
    }

    /**
     * Update the given route with the given name.
     *
     * @param route Route to update
     * @param name  New route name
     */
    public static void updateRoute(final Route route, final String name) {
        RouteDao dao = Db.getInstance().getDaoSession().getRouteDao();
        route.setName(name);
        dao.update(route);
    }

    /**
     * Remove non-existing route ids from the given set.
     *
     * @param routesToClean Set containing route ids
     */
    public static void cleanRoutes(final Set<String> routesToClean) {
        // build list containing all route ids
        List<String> routeIds = new ArrayList<>();
        for (Route route : loadRoutes()) {
            routeIds.add(route.getId().toString());
        }

        // check if the route ids contained in the set exist in the database
        Iterator<String> it = routesToClean.iterator();
        while (it.hasNext()) {
            if (!routeIds.contains(it.next())) {
                it.remove();
            }
        }
    }

    /**
     * Save the given active routes into the preferences.
     *
     * @param context      Context
     * @param activeRoutes Active routes to save
     */
    public static void saveActiveRoutes(final Context context,
                                        final Set<String> activeRoutes) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet(Pref.ACTIVE_ROUTES, activeRoutes);
        editor.apply();
    }
}
