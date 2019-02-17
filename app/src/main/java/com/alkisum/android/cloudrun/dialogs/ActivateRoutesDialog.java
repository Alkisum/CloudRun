package com.alkisum.android.cloudrun.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.PreferenceManager;

import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.model.Route;
import com.alkisum.android.cloudrun.utils.Pref;
import com.alkisum.android.cloudrun.utils.Routes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dialog showing list of routes that can be activated.
 *
 * @author Alkisum
 * @version 4.1
 * @since 4.0
 */
public final class ActivateRoutesDialog {

    /**
     * ActivateRoutesDialog constructor.
     */
    private ActivateRoutesDialog() {

    }

    /**
     * Build and show the AlertDialog.
     *
     * @param context Context in which the dialog should be built
     */
    public static void show(final Context context) {
        // get routes from database
        final List<Route> routes = Routes.loadRoutes();

        // get active routes from preferences
        Set<String> activeRoutesPref = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getStringSet(Pref.ACTIVE_ROUTES, null);

        // initialize items and state
        final String[] items = new String[routes.size()];
        boolean[] checkedItems = new boolean[routes.size()];
        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
            items[i] = route.getName();
            if (activeRoutesPref == null) {
                checkedItems[i] = false;
                continue;
            }
            checkedItems[i] = activeRoutesPref.contains(
                    route.getId().toString());
        }

        // initialize active routes to be added to preferences
        final Set<String> activeRoutes = new HashSet<>();
        if (activeRoutesPref != null) {
            activeRoutes.addAll(activeRoutesPref);
        }

        // build dialog
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.action_routes))
                .setMultiChoiceItems(items, checkedItems,
                        (dialog, which, isChecked) -> {
                            String routeId = routes.get(which).getId()
                                    .toString();
                            if (isChecked) {
                                activeRoutes.add(routeId);
                            } else {
                                activeRoutes.remove(routeId);
                            }
                        })
                .setPositiveButton(android.R.string.ok,
                        (dialog, which) -> {
                            Routes.cleanRoutes(activeRoutes);
                            Routes.saveActiveRoutes(context, activeRoutes);
                        })
                .setNegativeButton(android.R.string.cancel, null).show();
    }
}
