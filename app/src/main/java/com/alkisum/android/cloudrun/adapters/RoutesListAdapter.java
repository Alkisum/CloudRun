package com.alkisum.android.cloudrun.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.model.Route;

import java.util.List;

import androidx.appcompat.widget.AppCompatCheckBox;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter for Routes ListView.
 *
 * @author Alkisum
 * @version 4.1
 * @since 4.0
 */
public class RoutesListAdapter extends BaseAdapter {

    /**
     * Context.
     */
    private final Context context;

    /**
     * List of routes.
     */
    private final List<Route> routes;

    /**
     * Flag set to true if the ListView is in edit mode, false otherwise.
     */
    private boolean editMode;

    /**
     * RoutesListAdapter constructor.
     *
     * @param context Context
     * @param routes  List of routes
     */
    public RoutesListAdapter(final Context context,
                             final List<Route> routes) {
        this.context = context;
        this.routes = routes;
        resetRoutesSelectedStates();
    }

    /**
     * Set new list of routes.
     *
     * @param routes List of routes to set
     */
    public final void setRoutes(final List<Route> routes) {
        this.routes.clear();
        this.routes.addAll(routes);
    }

    /**
     * @return True if the ListView is in edit mode, false otherwise
     */
    public final boolean isEditMode() {
        return editMode;
    }

    /**
     * Disable the edit mode and reset the routes' selected states.
     */
    public final void disableEditMode() {
        editMode = false;
        resetRoutesSelectedStates();
    }

    /**
     * Switch ListView into edit mode and select the checkbox attached to the
     * item that has been pressed long.
     *
     * @param position Position of the item that has been pressed long
     */
    public final void enableEditMode(final int position) {
        editMode = true;
        routes.get(position).setSelected(true);
    }

    /**
     * Reverse the route selected state.
     *
     * @param position Position of the item that has been pressed
     */
    public final void changeRouteSelectedState(final int position) {
        Route route = routes.get(position);
        route.setSelected(!route.getSelected());
    }

    /**
     * Reset the routes' selected states. Mandatory to avoid the routes to
     * be selected from actions performed during last HistoryActivity instance.
     */
    private void resetRoutesSelectedStates() {
        for (Route route : routes) {
            route.setSelected(false);
        }
    }

    @Override
    public final int getCount() {
        return routes.size();
    }

    @Override
    public final Object getItem(final int i) {
        return routes.get(i);
    }

    @Override
    public final long getItemId(final int i) {
        return routes.get(i).getId();
    }

    @Override
    public final View getView(final int i, final View v,
                              final ViewGroup viewGroup) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        View view = v;
        if (view == null || view.getTag() == null) {
            view = inflater.inflate(R.layout.list_item_routes, viewGroup,
                    false);
            final ViewHolder holder = new ViewHolder(view);
            view.setTag(holder);
        }
        final Route route = (Route) getItem(i);
        final ViewHolder holder = (ViewHolder) view.getTag();

        // Set values
        holder.name.setText(route.getName());

        // Handle checkboxes according to the mode
        holder.checkBox.setChecked(route.getSelected());
        if (editMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setOnClickListener(view1 -> route.setSelected(
                    holder.checkBox.isChecked()));
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

        return view;
    }

    /**
     * ViewHolder for routes list adapter.
     */
    static class ViewHolder {

        /**
         * Checkbox to select the item in edit mode.
         */
        @BindView(R.id.routes_list_checkbox)
        AppCompatCheckBox checkBox;

        /**
         * TextView for name.
         */
        @BindView(R.id.routes_list_name)
        TextView name;

        /**
         * ViewHolder constructor.
         *
         * @param view View to bind with ButterKnife
         */
        ViewHolder(final View view) {
            ButterKnife.bind(this, view);
        }
    }
}
