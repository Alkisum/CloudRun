package com.alkisum.android.cloudrun.dialogs;

import android.content.Context;
import android.view.View;
import android.widget.EditText;

import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.events.RefreshEvent;
import com.alkisum.android.cloudrun.model.Route;
import com.alkisum.android.cloudrun.utils.Routes;

import org.greenrobot.eventbus.EventBus;

import androidx.appcompat.app.AlertDialog;

/**
 * Dialog to edit an existing route.
 *
 * @author Alkisum
 * @version 4.1
 * @since 4.0
 */
public final class EditRouteDialog {

    /**
     * EditRouteDialog constructor.
     */
    private EditRouteDialog() {

    }

    /**
     * Show dialog to edit an existing route.
     *
     * @param context Context
     * @param route   Route to edit
     */
    public static void show(final Context context, final Route route) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // set view
        View view = View.inflate(context, R.layout.dialog_edit_route, null);
        builder.setView(view);

        // set dialog title
        builder.setTitle(R.string.route_edit_dialog);

        // get edit text for route name
        final EditText routeName = view.findViewById(R.id.route_name);
        routeName.setHint(route.getName());

        // positive button
        builder.setPositiveButton(android.R.string.ok,
                (dialog, which) -> {
                    Routes.updateRoute(route, routeName.getText().toString());
                    EventBus.getDefault().post(new RefreshEvent());
                    dialog.dismiss();
                });

        // negative button
        builder.setNegativeButton(android.R.string.cancel,
                (dialog, which) -> dialog.cancel());

        // show dialog
        builder.show();
    }
}
