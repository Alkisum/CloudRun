package com.alkisum.android.cloudrun.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.alkisum.android.cloudlib.events.JsonFileWriterEvent;
import com.alkisum.android.cloudlib.events.UploadEvent;
import com.alkisum.android.cloudlib.net.ConnectDialog;
import com.alkisum.android.cloudlib.net.ConnectInfo;
import com.alkisum.android.cloudrun.BuildConfig;
import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.dialogs.AddMarkerDialog;
import com.alkisum.android.cloudrun.dialogs.EditMarkerDialog;
import com.alkisum.android.cloudrun.dialogs.EditRouteDialog;
import com.alkisum.android.cloudrun.dialogs.ErrorDialog;
import com.alkisum.android.cloudrun.events.DeletedEvent;
import com.alkisum.android.cloudrun.events.RefreshEvent;
import com.alkisum.android.cloudrun.events.RestoredEvent;
import com.alkisum.android.cloudrun.interfaces.Deletable;
import com.alkisum.android.cloudrun.interfaces.Jsonable;
import com.alkisum.android.cloudrun.interfaces.Restorable;
import com.alkisum.android.cloudrun.model.Marker;
import com.alkisum.android.cloudrun.model.Route;
import com.alkisum.android.cloudrun.net.Uploader;
import com.alkisum.android.cloudrun.tasks.Deleter;
import com.alkisum.android.cloudrun.tasks.Restorer;
import com.alkisum.android.cloudrun.utils.Deletables;
import com.alkisum.android.cloudrun.utils.Markers;
import com.alkisum.android.cloudrun.utils.Routes;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity to manage markers.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public class RouteActivity extends AppCompatActivity implements
        ConnectDialog.ConnectDialogListener {

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
     * Operation id for upload.
     */
    private static final int UPLOAD_OPERATION = 1;

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
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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
            case R.id.action_upload:
                ConnectDialog connectDialogUpload =
                        ConnectDialog.newInstance(UPLOAD_OPERATION);
                connectDialogUpload.setCallback(this);
                connectDialogUpload.show(getSupportFragmentManager(),
                        ConnectDialog.FRAGMENT_TAG);
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
        mapView.getZoomController().setVisibility(
                CustomZoomButtonsController.Visibility.NEVER);

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
        final RelativeLayout layout = findViewById(R.id.route_layout);
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
        Deletable[] routes = Routes.getSelectedRoutes().toArray(
                new Deletable[0]);
        new Deleter(new Integer[]{SUBSCRIBER_ID}, new Route()).execute(routes);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Execute the task to delete the given marker.
     *
     * @param marker Marker to delete
     */
    private void deleteMarker(final Marker marker) {
        new Deleter(new Integer[]{SUBSCRIBER_ID}, new Marker()).execute(marker);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Execute the task to restore the given markers.
     *
     * @param markers Markers to restore
     */
    private void restoreMarkers(final Restorable... markers) {
        new Restorer(new Marker()).execute(markers);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public final void onSubmit(final int operation,
                               final ConnectInfo connectInfo) {
        if (operation == UPLOAD_OPERATION) {
            try {
                Intent intent = new Intent(this, RouteActivity.class);
                intent.putExtra(ARG_ROUTE_ID, route.getId());
                List<Jsonable> routes = new ArrayList<>();
                routes.add(route);
                new Uploader(getApplicationContext(), connectInfo, intent,
                        routes, SUBSCRIBER_ID);
            } catch (JSONException e) {
                ErrorDialog.show(this,
                        getString(R.string.upload_failure_title),
                        e.getMessage(), null);
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setIndeterminate(true);
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Triggered on JSON file writer event.
     *
     * @param event JSON file writer event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onJsonFileWriterEvent(final JsonFileWriterEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        switch (event.getResult()) {
            case JsonFileWriterEvent.OK:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case JsonFileWriterEvent.ERROR:
                ErrorDialog.show(this,
                        getString(R.string.upload_writing_failure_title),
                        event.getException().getMessage(), null);
                progressBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     * Triggered on upload event.
     *
     * @param event Upload event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onUploadEvent(final UploadEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        switch (event.getResult()) {
            case UploadEvent.UPLOADING:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case UploadEvent.OK:
                Snackbar.make(findViewById(R.id.route_layout),
                        R.string.route_upload_success_snackbar,
                        Snackbar.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                break;
            case UploadEvent.ERROR:
                ErrorDialog.show(this, getString(
                        R.string.upload_failure_title), event.getMessage(),
                        null);
                progressBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     * Triggered on refresh event.
     *
     * @param event Refresh event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onRefreshEvent(final RefreshEvent event) {
        Toolbar toolbar = findViewById(R.id.route_toolbar);
        toolbar.setTitle(route.getName());
        refreshMarkers();
    }

    /**
     * Triggered on deleted event.
     *
     * @param event Deleted event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onDeletedEvent(final DeletedEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        if (event.getDeletable() instanceof Route) {
            // handle Route entities
            Intent intent = new Intent();
            intent.putExtra(ARG_ROUTE_JSON, new Gson().toJson(route));
            setResult(RouteListActivity.ROUTE_DELETED, intent);
            finish();

        } else if (event.getDeletable() instanceof Marker) {
            // handle Marker entities
            progressBar.setVisibility(View.GONE);
            refreshMarkers();

            // create snackbar
            Snackbar snackbar = Snackbar.make(findViewById(R.id.route_layout),
                    R.string.marker_delete_snackbar, Snackbar.LENGTH_LONG);

            // if the deleted entities are restorable, show UNDO action
            if (Restorable.class.isAssignableFrom(
                    event.getDeletable().getClass())) {
                // convert deletable entities to restorable entities
                final Restorable[] markers = Deletables.toRestorables(
                        event.getDeletedEntities());
                snackbar.setAction(R.string.action_undo,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                restoreMarkers(markers);
                            }
                        });
            }
            snackbar.show();
        }
    }

    /**
     * Triggered on restored event.
     *
     * @param event Restored event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onRestoredEvent(final RestoredEvent event) {
        if (!(event.getRestorable() instanceof Marker)) {
            return;
        }
        progressBar.setVisibility(View.GONE);
        refreshMarkers();
    }
}
