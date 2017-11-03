package com.alkisum.android.cloudrun.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.alkisum.android.cloudrun.BuildConfig;
import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.events.CoordinateEvent;
import com.alkisum.android.cloudrun.events.GpsStatusEvent;
import com.alkisum.android.cloudrun.location.Coordinate;
import com.alkisum.android.cloudrun.model.DataPoint;
import com.alkisum.android.cloudrun.model.Session;
import com.alkisum.android.cloudrun.database.Sessions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity showing current location and tracking session on map.
 *
 * @author Alkisum
 * @version 3.1
 * @since 3.0
 */
public class MapActivity extends AppCompatActivity {

    /**
     * Argument for session id.
     */
    public static final String ARG_SESSION_ID = "arg_session_id";

    /**
     * Argument for last received coordinate.
     */
    public static final String ARG_COORDINATE = "arg_coordinate";

    /**
     * Argument for the GPS status.
     */
    public static final String ARG_GPS_STATUS = "arg_gps_status";

    /**
     * Instance of the current session.
     */
    private Session session;

    /**
     * Id for current GPS status icon.
     */
    private int gpsStatusIconId = R.drawable.ic_gps_not_fixed_white_24dp;

    /**
     * Flag set to true when the map view is focused on the current position,
     * false otherwise.
     */
    private boolean focused = true;

    /**
     * Last known position from GPS.
     */
    private Coordinate lastPosition;

    /**
     * List to store the overlays locally.
     */
    private List<Overlay> overlays;

    /**
     * View containing the OSM.
     */
    @BindView(R.id.map)
    MapView mapView;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);

        if (getIntent().hasExtra(ARG_SESSION_ID)
                && getIntent().getExtras() != null) {
            long sessionId = getIntent().getExtras().getLong(ARG_SESSION_ID);
            session = Sessions.getSessionById(sessionId);
        }
        Coordinate initPosition = getIntent().getParcelableExtra(
                ARG_COORDINATE);
        gpsStatusIconId = getIntent().getExtras().getInt(ARG_GPS_STATUS);

        Toolbar toolbar = findViewById(R.id.map_toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });

        mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                focused = false;
                return false;
            }
        });

        initMap(initPosition);
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
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public final boolean onPrepareOptionsMenu(final Menu menu) {
        // GPS status icon
        MenuItem gps = menu.findItem(R.id.action_gps);
        gps.setIcon(gpsStatusIconId);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_gps) {
            focused = true;
            setPosition(lastPosition);
            mapView.invalidate();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Initialize the map.
     *
     * @param initPosition Current position when initializing the map
     */
    private void initMap(final Coordinate initPosition) {
        overlays = new ArrayList<>();

        Configuration.getInstance().setUserAgentValue(
                BuildConfig.APPLICATION_ID);
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.setMultiTouchControls(true);

        // Set zoom to max
        mapView.getController().setZoom(19);
        setPosition(initPosition);
        setRoute();
        mapView.invalidate();
    }

    /**
     * Triggered when new coordinates are received.
     *
     * @param event Coordinate event
     */
    @Subscribe
    public final void onCoordinateEvent(final CoordinateEvent event) {
        setPosition(event.getValues());
        setRoute();
        mapView.invalidate();
    }

    /**
     * Triggered when new coordinates are received.
     *
     * @param event Coordinate event
     */
    @Subscribe
    public final void onGpsStatusEvent(final GpsStatusEvent event) {
        gpsStatusIconId = event.getIcon();
        invalidateOptionsMenu();
    }

    /**
     * Set the given current position on the map.
     *
     * @param position Current position
     */
    private void setPosition(final Coordinate position) {
        lastPosition = position;

        // Set center if focused flag is set to true
        if (focused) {
            mapView.getController().setCenter(new GeoPoint(
                    position.getLatitude(), position.getLongitude()));
        }

        // Create OverlayItem
        OverlayItem overlayItem = new OverlayItem("", "",
                new GeoPoint(position.getLatitude(), position.getLongitude()));

        // Create marker
        Drawable marker = ContextCompat.getDrawable(this,
                R.drawable.ic_current_position_blue_24dp);
        overlayItem.setMarker(marker);

        // Build ItemizedIconOverlay
        final ArrayList<OverlayItem> items = new ArrayList<>();
        items.add(overlayItem);
        ItemizedIconOverlay currentLocationOverlay = new ItemizedIconOverlay<>(
                this, items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index,
                                                     final OverlayItem item) {
                        return false;
                    }

                    @Override
                    public boolean onItemLongPress(final int index,
                                                   final OverlayItem item) {
                        return false;
                    }
                });

        // Add ItemizedIconOverlay to map's overlays (index 0)
        if (overlays.isEmpty()) {
            this.overlays.add(currentLocationOverlay);
        } else {
            this.overlays.set(0, currentLocationOverlay);
        }

        setOverlays();
    }

    /**
     * Set the overlays to the map view. Reverse the local overlay list
     * to draw the marker on top of the route.
     */
    private void setOverlays() {
        Collections.reverse(overlays);
        mapView.getOverlays().clear();
        mapView.getOverlays().addAll(overlays);
        Collections.reverse(overlays);
    }

    /**
     * Build the route from the session's datapoints.
     */
    private void setRoute() {
        if (session == null) {
            return;
        }

        // Build route from session's datapoint
        ArrayList<GeoPoint> route = new ArrayList<>();
        session.resetDataPoints();
        for (DataPoint dp : session.getDataPoints()) {
            route.add(new GeoPoint(dp.getLatitude(), dp.getLongitude()));
        }

        // Build polyline
        Polyline polyline = new Polyline();
        polyline.setColor(ContextCompat.getColor(this, R.color.map_blue));
        polyline.setPoints(route);
        polyline.setWidth(10f);

        // Add polyline to overlays (index 1)
        if (overlays.size() == 1) {
            overlays.add(polyline);
        } else if (overlays.size() > 1) {
            overlays.set(1, polyline);
        }

        setOverlays();
    }
}
