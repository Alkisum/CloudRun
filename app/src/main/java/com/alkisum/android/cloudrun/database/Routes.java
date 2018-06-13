package com.alkisum.android.cloudrun.database;

import com.alkisum.android.cloudrun.model.Route;
import com.alkisum.android.cloudrun.model.RouteDao;

import java.util.ArrayList;
import java.util.List;

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
}
