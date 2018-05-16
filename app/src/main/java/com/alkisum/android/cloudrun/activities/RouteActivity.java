package com.alkisum.android.cloudrun.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.database.Routes;
import com.alkisum.android.cloudrun.model.Route;

import org.osmdroid.views.MapView;

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
     * Argument for route id.
     */
    public static final String ARG_ROUTE_ID = "arg_route_id";

    private Route route;

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
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });
    }
}
