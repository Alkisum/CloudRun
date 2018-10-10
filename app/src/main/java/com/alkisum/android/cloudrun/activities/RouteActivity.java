package com.alkisum.android.cloudrun.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;

import com.alkisum.android.cloudrun.BuildConfig;
import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.database.MarkerDeleter;
import com.alkisum.android.cloudrun.database.MarkerRestorer;
import com.alkisum.android.cloudrun.database.Markers;
import com.alkisum.android.cloudrun.database.RouteDeleter;
import com.alkisum.android.cloudrun.database.Routes;
import com.alkisum.android.cloudrun.dialogs.AddMarkerDialog;
import com.alkisum.android.cloudrun.dialogs.EditMarkerDialog;
import com.alkisum.android.cloudrun.dialogs.EditRouteDialog;
import com.alkisum.android.cloudrun.events.DeleteMarkerEvent;
import com.alkisum.android.cloudrun.events.DeleteRouteEvent;
import com.alkisum.android.cloudrun.events.InsertMarkerEvent;
import com.alkisum.android.cloudrun.events.MarkerUpdatedEvent;
import com.alkisum.android.cloudrun.events.RestoreMarkerEvent;
import com.alkisum.android.cloudrun.events.UpdateRouteEvent;
import com.alkisum.android.cloudrun.model.Marker;
import com.alkisum.android.cloudrun.model.Route;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity to manage markers.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public class RouteActivity extends AppCompatActivity {

    /**
     * Subscriber id to use when receiving event.
     */
    private static final int SUBSCRIBER_ID = 324;

    /**
     * Argument for route id.
     */
    public static final String ARG_ROUTE_ID = "arg_route_id";

    /**
     * Argument for the JSON representation of the route, sent to the
     * RouteListActivity when the route has been deleted.
     */
    static final String ARG_ROUTE_JSON = "arg_route_json";

    /**
     * Route instance.
     */
    private Route route;

    /**
     * Overlay to receive event on map interaction.
     */
    private MapEventsOverlay mapEventsOverlay;

    /**
     * Progress bar to show the progress of operations.
     */
    @BindView(R.id.route_progressbar)
    ProgressBar progressBar;

    /**
     * View containing the OSM.
     */
    @BindView(R.id.map)
    MapView mapView;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        ButterKnife.bind(this);

        // get route by given id
        if (getIntent().hasExtra(ARG_ROUTE_ID)
                && getIntent().getExtras() != null) {
            long routeId = getIntent().getExtras().getLong(ARG_ROUTE_ID);
            route = Routes.getRouteById(routeId);
        }

        // set toolbar
        Toolbar toolbar = findViewById(R.id.route_toolbar);
        toolbar.setTitle(route.getName());
        setSupportActionBar(toolbar);
        // TODO set parent activity in manifest (do it for every activity,
        // TODO see CloudNotes)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });

        // initialize map
        this.initMap();
    }

    @Override
    public final void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public final void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_route, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                EditRouteDialog.show(this, route);
                return true;
            case R.id.action_delete:
                route.setSelected(true);
                if (!Routes.getSelectedRoutes().isEmpty()) {
                    this.deleteRoute();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Initialize map.
     */
    private void initMap() {
        // configure map view
        Configuration.getInstance().setUserAgentValue(
                BuildConfig.APPLICATION_ID);
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(4d);
        mapView.setBuiltInZoomControls(false);

        // add overlay for map events
        mapEventsOverlay = new MapEventsOverlay(mapEventsReceiver);

        // center map
        if (route.getMarkers().isEmpty()) {
            this.centerMapFromLocation();
        } else {
            this.centerMapFromMarkers();
        }

        // show markers
        this.refreshMarkers();
    }

    /**
     * Set map overlays and invalidate map.
     */
    private void refreshMarkers() {
        // initialize markers
        this.setOverlays();

        // invalidate
        mapView.invalidate();
    }

    /**
     * Center the map based on markers.
     */
    private void centerMapFromMarkers() {
        // create geopoints from markers
        List<GeoPoint> geoPoints = new ArrayList<>();
        for (Marker marker : route.getMarkers()) {
            geoPoints.add(new GeoPoint(
                    marker.getLatitude(), marker.getLongitude()));
        }

        if (route.getMarkers().size() == 1) {
            // center map on single marker
            this.centerMap(route.getMarkers().get(0).getLatitude(),
                    route.getMarkers().get(0).getLongitude());
        } else if (route.getMarkers().size() > 1) {
            // set map bounds based on markers
            this.setMapBounds(geoPoints);
        }
    }

    /**
     * Set the map bounds based on the given geopoints.
     *
     * @param geoPoints Geopoints to use to set the bounds
     */
    private void setMapBounds(final List<GeoPoint> geoPoints) {
        final BoundingBox boundingBox = BoundingBox.fromGeoPoints(geoPoints);
        final ConstraintLayout layout = findViewById(R.id.route_layout);
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // will be called when the layout is ready
                        mapView.zoomToBoundingBox(boundingBox, false, 64);
                        mapView.invalidate();
                        layout.getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                    }
                });
    }

    /**
     * Center the map based on the last known location.
     */
    private void centerMapFromLocation() {
        // get fused location provider client
        FusedLocationProviderClient fusedLocationClient = LocationServices
                .getFusedLocationProviderClient(this);

        // check permission for location
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // get last location
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this,
                            new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(final Location location) {
                                    // check if location is valid
                                    if (location != null) {
                                        // zoom on last location
                                        centerMap(location.getLatitude(),
                                                location.getLongitude());
                                    }
                                }
                            });
        }
    }

    /**
     * Center the map to the given coordinates.
     *
     * @param latitude  Latitude
     * @param longitude Longitude
     */
    private void centerMap(final double latitude, final double longitude) {
        mapView.getController().setCenter(new GeoPoint(latitude, longitude));
        mapView.getController().setZoom(17d);
    }

    /**
     * Set map overlays.
     */
    private void setOverlays() {
        // reset overlays
        mapView.getOverlays().clear();

        // add map events overlay
        mapView.getOverlays().add(mapEventsOverlay);

        // add each marker to map
        route.resetMarkers();
        for (Marker marker : route.getMarkers()) {
            mapView.getOverlays().add(this.buildOverlay(marker));
        }
    }

    /**
     * Build the overlay for the given marker.
     *
     * @param marker Marker to build the overlay for
     * @return Built overlay
     */
    private Overlay buildOverlay(final Marker marker) {
        // create OverlayItem
        OverlayItem overlayItem = new OverlayItem("", "",
                new GeoPoint(marker.getLatitude(), marker.getLongitude()));

        // create marker
        Drawable drawable = ContextCompat.getDrawable(this,
                R.drawable.ic_place_red_24dp);
        overlayItem.setMarker(drawable);
        overlayItem.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);

        // build ItemizedIconOverlay
        final ArrayList<OverlayItem> items = new ArrayList<>();
        items.add(overlayItem);
        return new ItemizedIconOverlay<>(this, items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index,
                                                     final OverlayItem item) {
                        // get pressed marker by its coordinates
                        Marker markerToEdit = Markers.getMarkerByCoordinates(
                                route, item.getPoint().getLatitude(),
                                item.getPoint().getLongitude());

                        // edit marker
                        if (markerToEdit != null) {
                            EditMarkerDialog.show(RouteActivity.this,
                                    markerToEdit);
                        }
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index,
                                                   final OverlayItem item) {
                        // get pressed marker by its coordinates
                        Marker markerToDelete = Markers.getMarkerByCoordinates(
                                route, item.getPoint().getLatitude(),
                                item.getPoint().getLongitude());

                        // delete marker
                        if (markerToDelete != null) {
                            deleteMarker(markerToDelete);
                        }
                        return true;
                    }
                }
        );
    }

    /**
     * Receiver for map events.
     */
    private final MapEventsReceiver mapEventsReceiver =
            new MapEventsReceiver() {
                @Override
                public boolean singleTapConfirmedHelper(final GeoPoint p) {
                    return false;
                }

                @Override
                public boolean longPressHelper(final GeoPoint p) {
                    // show dialog to add marker to route
                    AddMarkerDialog.show(RouteActivity.this, p.getLatitude(),
                            p.getLongitude(), route.getId());
                    return false;
                }
            };

    /**
     * Execute the task to delete the selected routes.
     */
    private void deleteRoute() {
        new RouteDeleter(new Integer[]{SUBSCRIBER_ID}).execute();
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Execute the task to delete the given marker.
     *
     * @param marker Marker to delete
     */
    private void deleteMarker(final Marker marker) {
        new MarkerDeleter().execute(marker);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Execute the task to restore the given markers.
     *
     * @param markers Markers to restore
     */
    private void restoreMarkers(final Marker... markers) {
        new MarkerRestorer().execute(markers);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Triggered on update route event.
     *
     * @param event UpdateRoute event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onUpdateRouteEvent(final UpdateRouteEvent event) {
        Toolbar toolbar = findViewById(R.id.route_toolbar);
        toolbar.setTitle(route.getName());
    }

    /**
     * Triggered on delete route event.
     *
     * @param event DeleteRoute event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onDeleteRouteEvent(final DeleteRouteEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(ARG_ROUTE_JSON, new Gson().toJson(route));
        setResult(RouteListActivity.ROUTE_DELETED, intent);
        finish();
    }

    /**
     * Triggered on insert marker event.
     *
     * @param event InsertMarker event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onInsertMarkerEvent(final InsertMarkerEvent event) {
        refreshMarkers();
    }

    /**
     * Triggered on delete marker event.
     *
     * @param event DeleteMarker event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onDeleteMarkerEvent(final DeleteMarkerEvent event) {
        progressBar.setVisibility(View.GONE);
        refreshMarkers();

        Snackbar.make(findViewById(R.id.route_layout),
                R.string.marker_delete_snackbar,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.action_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        restoreMarkers(event.getDeletedMarkers());
                    }
                }).show();
    }

    /**
     * Triggered on restore marker event.
     *
     * @param event RestoreMarker event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onRestoreMarkerEvent(final RestoreMarkerEvent event) {
        progressBar.setVisibility(View.GONE);
        refreshMarkers();
    }

    /**
     * Triggered on marker updated event.
     *
     * @param event MarkerUpdated event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onMarkerUpdatedEvent(final MarkerUpdatedEvent event) {
        refreshMarkers();
    }
}
