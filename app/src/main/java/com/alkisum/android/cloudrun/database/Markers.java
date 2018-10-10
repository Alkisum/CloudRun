package com.alkisum.android.cloudrun.database;

import com.alkisum.android.cloudrun.model.Marker;
import com.alkisum.android.cloudrun.model.MarkerDao;
import com.alkisum.android.cloudrun.model.Route;

/**
 * Utility class for marker operations.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public final class Markers {

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
}
