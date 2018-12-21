package com.alkisum.android.cloudrun.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.alkisum.android.cloudlib.file.json.JsonFile;
import com.alkisum.android.cloudrun.database.Db;
import com.alkisum.android.cloudrun.model.DaoSession;
import com.alkisum.android.cloudrun.model.Marker;
import com.alkisum.android.cloudrun.model.MarkerDao;
import com.alkisum.android.cloudrun.model.Route;
import com.alkisum.android.cloudrun.model.RouteDao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    /**
     * Build JSON object from the given route.
     *
     * @param route Route to build the JSON object from
     * @return JSON object
     * @throws JSONException Exception thrown while building JSON object
     */
    public static JSONObject buildJson(final Route route) throws JSONException {
        // build JSON object for route
        JSONObject jsonRoute = new JSONObject();
        jsonRoute.put(Json.ROUTE_NAME, route.getName());

        // build JSON array for markers
        JSONArray jsonMarkers = new JSONArray();
        List<Marker> markers = route.getMarkers();
        for (Marker marker : markers) {
            JSONObject jsonMarker = new JSONObject();
            jsonMarker.put(Json.MARKER_LABEL, marker.getLabel());
            jsonMarker.put(Json.MARKER_LATITUDE, marker.getLatitude());
            jsonMarker.put(Json.MARKER_LONGITUDE, marker.getLongitude());
            jsonMarkers.put(jsonMarker);
        }

        // build final JSON object
        JSONObject jsonBase = new JSONObject();
        jsonBase.put(Json.VERSION, Json.JSON_VERSION);
        jsonBase.put(Json.ROUTE, jsonRoute);
        jsonBase.put(Json.MARKERS, jsonMarkers);

        return jsonBase;
    }

    /**
     * Build route from Json file version 1.
     *
     * @param jsonBase JSONObject the structure is based on
     * @throws JSONException An error occurred while parsing the JSON object
     */
    public static void buildFromJsonVersion1(final JSONObject jsonBase)
            throws JSONException {
        // get DAOs
        DaoSession daoSession = Db.getInstance().getDaoSession();
        RouteDao routeDao = daoSession.getRouteDao();
        MarkerDao markerDao = daoSession.getMarkerDao();

        // build route
        JSONObject jsonRoute = jsonBase.getJSONObject(Json.ROUTE);
        Route route = new Route();
        route.setName(jsonRoute.getString(Json.ROUTE_NAME));

        // insert route
        routeDao.insert(route);

        // build datapoints
        List<Marker> markers = new ArrayList<>();
        JSONArray jsonMarkers = jsonBase.getJSONArray(Json.MARKERS);
        for (int i = 0; i < jsonMarkers.length(); i++) {
            JSONObject jsonMarker = jsonMarkers.getJSONObject(i);
            Marker marker = new Marker();
            marker.setLabel(jsonMarker.getString(Json.MARKER_LABEL));
            marker.setLatitude(jsonMarker.getDouble(Json.MARKER_LATITUDE));
            marker.setLongitude(jsonMarker.getDouble(Json.MARKER_LONGITUDE));
            marker.setRoute(route);
            markers.add(marker);
        }

        // insert markers
        markerDao.insertInTx(markers);
    }

    /**
     * Class defining constants for route JSON.
     *
     * @author Alkisum
     * @version 4.0
     * @since 4.0
     */
    public static class Json {
        /**
         * JSON file version.
         */
        static final int JSON_VERSION = 1;

        /**
         * JSON file name prefix.
         */
        public static final String FILE_PREFIX = "CloudRun_";

        /**
         * Regex for the Json file.
         */
        public static final String FILE_REGEX = FILE_PREFIX
                + "(.)*" + JsonFile.FILE_EXT + "$";

        /**
         * JSON name for JSON file version number.
         */
        public static final String VERSION = "version";

        /**
         * JSON name for route object.
         */
        static final String ROUTE = "route";

        /**
         * JSON name for route name.
         */
        static final String ROUTE_NAME = "name";

        /**
         * JSON name for marker array.
         */
        static final String MARKERS = "markers";

        /**
         * JSON name for marker label.
         */
        static final String MARKER_LABEL = "label";

        /**
         * JSON name for marker latitude.
         */
        static final String MARKER_LATITUDE = "latitude";

        /**
         * JSON name for marker longitude.
         */
        static final String MARKER_LONGITUDE = "longitude";
    }
}
