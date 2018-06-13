package com.alkisum.android.cloudrun.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.database.RouteDeleter;
import com.alkisum.android.cloudrun.database.Routes;
import com.alkisum.android.cloudrun.dialogs.EditRouteDialog;
import com.alkisum.android.cloudrun.events.DeleteRouteEvent;
import com.alkisum.android.cloudrun.events.UpdateRouteEvent;
import com.alkisum.android.cloudrun.model.Route;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
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
                    deleteRoute();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Execute the task to delete the selected routes.
     */
    private void deleteRoute() {
        new RouteDeleter(new Integer[]{SUBSCRIBER_ID}).execute();
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
    public final void onDeleteEvent(final DeleteRouteEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(ARG_ROUTE_JSON, new Gson().toJson(route));
        setResult(RouteListActivity.ROUTE_DELETED, intent);
        finish();
    }
}
