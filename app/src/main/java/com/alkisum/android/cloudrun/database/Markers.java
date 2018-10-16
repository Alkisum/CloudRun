package com.alkisum.android.cloudrun.database;

import android.content.Context;
import android.location.Location;

import com.alkisum.android.cloudrun.location.Coordinate;
import com.alkisum.android.cloudrun.model.Marker;
import com.alkisum.android.cloudrun.model.MarkerDao;
import com.alkisum.android.cloudrun.model.Route;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for marker operations.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public final class Markers {

    /**
     * Maximum distance a marker must be to trigger marker alert (in meter).
     */
    private static final int MARKER_DISTANCE = 10;

    /**
     * Markers constructor.
     */
    private Markers() {

    }

    /**
     * Insert a marker with the given information into the database.
     *
     * @param label     Marker label
     * @param latitude  Marker latitude
     * @param longitude Marker longitude
     * @param routeId   Route id attached to the marker
     */
    public static void insertMarker(final String label,
                                    final double latitude,
                                    final double longitude,
                                    final long routeId) {
        MarkerDao dao = Db.getInstance().getDaoSession().getMarkerDao();
        Marker marker = new Marker();
        marker.setLabel(label);
        marker.setLatitude(latitude);
        marker.setLongitude(longitude);
        marker.setRouteId(routeId);
        dao.insert(marker);
    }

    /**
     * Update the given marker with the given label.
     *
     * @param marker Marker to update
     * @param label  New marker label
     */
    public static void updateMarker(final Marker marker, final String label) {
        MarkerDao dao = Db.getInstance().getDaoSession().getMarkerDao();
        marker.setLabel(label);
        dao.update(marker);
    }

    /**
     * Find marker attached to the given route using the given coordinates.
     *
     * @param route     Route containing the marker to find
     * @param latitude  Latitude of the marker to find
     * @param longitude Longitude of the marker to find
     * @return Marker if found, null otherwise
     */
    public static Marker getMarkerByCoordinates(final Route route,
                                                final double latitude,
                                                final double longitude) {
        for (Marker marker : route.getMarkers()) {
            if (marker.getLatitude() == latitude
                    && marker.getLongitude() == longitude) {
                return marker;
            }
        }
        return null;
    }

    /**
     * Retrieve all markers from active routes.
     *
     * @param context Context
     * @return List of active marker
     */
    public static List<Marker> getActiveMarkers(final Context context) {
        List<Marker> markers = new ArrayList<>();
        List<Route> routes = Routes.getActiveRoutes(context);
        for (Route route : routes) {
            markers.addAll(route.getMarkers());
        }
        return markers;
    }

    /**
     * Check distance between current location and each active marker.
     *
     * @param context Context
     * @param current Current location
     * @return List of markers located within {@link Markers#MARKER_DISTANCE}
     */
    public static List<Marker> getSurroundingMarkers(final Context context,
                                                     final Coordinate current) {
        List<Marker> surroundingMarkers = new ArrayList<>();

        // check distance to each active marker
        for (Marker marker : getActiveMarkers(context)) {
            if (distanceToMarker(current, marker) < MARKER_DISTANCE) {
                surroundingMarkers.add(marker);
            }
        }

        return surroundingMarkers;
    }

    /**
     * Calculate the distance between the current location and the given marker.
     *
     * @param current Current location
     * @param marker  Marker
     * @return Distance between the 2 locations
     */
    private static float distanceToMarker(final Coordinate current,
                                          final Marker marker) {
        Location currentLocation = new Location("Current");
        currentLocation.setLatitude(current.getLatitude());
        currentLocation.setLongitude(current.getLongitude());

        Location markerLocation = new Location("Marker");
        markerLocation.setLatitude(marker.getLatitude());
        markerLocation.setLongitude(marker.getLongitude());

        return currentLocation.distanceTo(markerLocation);
    }

    /**
     * Convert the given marker to a coordinate.
     * The coordinate time is set to the current time.
     * The elevation is set to 0.
     *
     * @param marker Marker to convert
     * @return Converted marker
     */
    public static Coordinate toCoordinate(final Marker marker) {
        return new Coordinate(
                System.currentTimeMillis(),
                marker.getLatitude(),
                marker.getLongitude(),
                0);
    }
}
