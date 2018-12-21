package com.alkisum.android.cloudrun.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.adapters.RoutesListAdapter;
import com.alkisum.android.cloudrun.dialogs.AddRouteDialog;
import com.alkisum.android.cloudrun.events.DeletedEvent;
import com.alkisum.android.cloudrun.events.RefreshEvent;
import com.alkisum.android.cloudrun.events.RestoredEvent;
import com.alkisum.android.cloudrun.interfaces.Deletable;
import com.alkisum.android.cloudrun.interfaces.Restorable;
import com.alkisum.android.cloudrun.model.Route;
import com.alkisum.android.cloudrun.tasks.Deleter;
import com.alkisum.android.cloudrun.tasks.Restorer;
import com.alkisum.android.cloudrun.utils.Deletables;
import com.alkisum.android.cloudrun.utils.Routes;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Activity listing available routes.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public class RouteListActivity extends AppCompatActivity {
    /**
     * Subscriber id to use when receiving event.
     */
    private static final int SUBSCRIBER_ID = 716;

    /**
     * Request code for RouteActivity result.
     */
    private static final int ROUTE_REQUEST_CODE = 922;

    /**
     * Result returned by RouteActivity when the route was deleted from the
     * RouteActivity.
     */
    public static final int ROUTE_DELETED = 958;

    /**
     * Toolbar.
     */
    @BindView(R.id.routes_toolbar)
    Toolbar toolbar;

    /**
     * SwipeRefreshLayout for list view.
     */
    @BindView(R.id.routes_swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    /**
     * ListView containing the routes.
     */
    @BindView(R.id.routes_list)
    ListView listView;

    /**
     * TextView informing the user that no routes are available.
     */
    @BindView(R.id.routes_no_route)
    TextView noRouteTextView;

    /**
     * Progress bar to show the progress of operations.
     */
    @BindView(R.id.routes_progressbar)
    ProgressBar progressBar;

    /**
     * Floating action button to add routes.
     */
    @BindView(R.id.routes_fab_add)
    FloatingActionButton fab;

    /**
     * List adapter for the list of routes.
     */
    private RoutesListAdapter listAdapter;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);
        ButterKnife.bind(this);

        setGui();
    }

    @Override
    public final void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        refreshList();
    }

    @Override
    public final void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Set the GUI.
     */
    private void setGui() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (isEditMode()) {
                    disableEditMode();
                } else {
                    finish();
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView,
                                    final View view, final int position,
                                    final long id) {
                if (listAdapter.isEditMode()) {
                    listAdapter.changeRouteSelectedState(position);
                    listAdapter.notifyDataSetInvalidated();
                } else {
                    Intent intent = new Intent(RouteListActivity.this,
                            RouteActivity.class);
                    intent.putExtra(RouteActivity.ARG_ROUTE_ID, id);
                    startActivityForResult(intent, ROUTE_REQUEST_CODE);
                }
            }
        });

        listView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(
                            final AdapterView<?> adapterView, final View view,
                            final int i, final long l) {
                        enableEditMode(i);
                        return true;
                    }
                });

        List<Route> routes = Routes.loadRoutes();
        if (routes.isEmpty()) {
            listView.setVisibility(View.GONE);
            noRouteTextView.setVisibility(View.VISIBLE);
        }
        listAdapter = new RoutesListAdapter(this, routes);
        listView.setAdapter(listAdapter);

        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshList();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );
    }

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_routelist, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public final boolean onPrepareOptionsMenu(final Menu menu) {
        boolean editMode = listAdapter.isEditMode();
        menu.findItem(R.id.action_delete).setVisible(editMode);
        menu.findItem(R.id.action_select_all).setVisible(editMode);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                if (!Routes.getSelectedRoutes().isEmpty()) {
                    deleteRoutes();
                }
                return true;
            case R.id.action_select_all:
                List<Route> routes = Routes.loadRoutes();
                for (Route route : routes) {
                    route.setSelected(true);
                }
                listAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected final void onActivityResult(final int requestCode,
                                          final int resultCode,
                                          final Intent data) {
        if (requestCode == ROUTE_REQUEST_CODE) {
            if (resultCode == ROUTE_DELETED) {
                String json = data.getStringExtra(RouteActivity.ARG_ROUTE_JSON);
                final Route route = new Gson().fromJson(json, Route.class);
                Snackbar.make(fab, R.string.route_delete_snackbar,
                        Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_undo,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(final View v) {
                                        restoreRoutes(route);
                                    }
                                }).show();
            }
        }
    }

    /**
     * Check if the list is in edit mode.
     *
     * @return true if the list is in edit mode, false otherwise
     */
    private boolean isEditMode() {
        return listAdapter != null && listAdapter.isEditMode();
    }

    /**
     * Called when the Back button is pressed. If enabled, the edit mode must be
     * disable, otherwise the activity should be finished.
     */
    @SuppressLint("RestrictedApi")
    private void disableEditMode() {
        fab.setVisibility(View.VISIBLE);
        listAdapter.disableEditMode();
        listAdapter.notifyDataSetInvalidated();
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        invalidateOptionsMenu();
    }

    /**
     * Enable the edit mode.
     *
     * @param position Position of the item that has been pressed long
     */
    @SuppressLint("RestrictedApi")
    private void enableEditMode(final int position) {
        if (!listAdapter.isEditMode()) {
            fab.setVisibility(View.GONE);
            listAdapter.enableEditMode(position);
            listAdapter.notifyDataSetInvalidated();
            toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
            invalidateOptionsMenu();
        }
    }

    /**
     * Execute the task to delete the selected routes.
     */
    private void deleteRoutes() {
        Deletable[] routes = Routes.getSelectedRoutes().toArray(
                new Deletable[0]);
        new Deleter(new Integer[]{SUBSCRIBER_ID}, new Route()).execute(routes);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Execute the task to restore the given routes.
     *
     * @param routes Routes to restore
     */
    private void restoreRoutes(final Restorable... routes) {
        new Restorer(new Route()).execute(routes);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Reload the list of routes and notify the list adapter.
     */
    private void refreshList() {
        if (isEditMode()) {
            disableEditMode();
        }
        List<Route> routes = Routes.loadRoutes();
        if (routes.isEmpty()) {
            listView.setVisibility(View.GONE);
            noRouteTextView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            noRouteTextView.setVisibility(View.GONE);
        }
        listAdapter.setRoutes(routes);
        listAdapter.notifyDataSetChanged();
    }

    /**
     * Triggered on deleted event.
     *
     * @param event Deleted event
     */
    @SuppressWarnings("unchecked")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onDeletedEvent(final DeletedEvent event) {
        if (!(event.getDeletable() instanceof Route)
                || !event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        progressBar.setVisibility(View.GONE);
        refreshList();

        // create snackbar
        Snackbar snackbar = Snackbar.make(fab, R.string.route_delete_snackbar,
                Snackbar.LENGTH_LONG);

        // if the deleted entities are restorable, show UNDO action
        if (Restorable.class.isAssignableFrom(
                event.getDeletable().getClass())) {
            // convert deletable entities to restorable entities
            final Restorable[] routes = Deletables.toRestorables(
                    event.getDeletedEntities());
            snackbar.setAction(R.string.action_undo,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            // restore routes
                            restoreRoutes(routes);
                        }
                    });
        }
        snackbar.show();
    }

    /**
     * Triggered on refresh event.
     *
     * @param event Refresh event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onRefreshEvent(final RefreshEvent event) {
        refreshList();
    }

    /**
     * Triggered on restored event.
     *
     * @param event Restored event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onRestoredEvent(final RestoredEvent event) {
        if (!(event.getRestorable() instanceof Route)) {
            return;
        }
        progressBar.setVisibility(View.GONE);
        refreshList();
    }

    @Override
    public final void onBackPressed() {
        if (isEditMode()) {
            disableEditMode();
        } else {
            finish();
        }
    }

    /**
     * Called when the Add route button is clicked.
     */
    @OnClick(R.id.routes_fab_add)
    public final void onAddRouteClicked() {
        AddRouteDialog.show(this);
    }
}
