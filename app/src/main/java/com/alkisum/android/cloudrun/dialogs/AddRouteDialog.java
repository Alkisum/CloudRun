package com.alkisum.android.cloudrun.dialogs;

import android.content.Context;
import android.view.View;
import android.widget.EditText;

import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.events.RefreshEvent;
import com.alkisum.android.cloudrun.utils.Routes;

import org.greenrobot.eventbus.EventBus;

import androidx.appcompat.app.AlertDialog;

/**
 * Dialog to add a new route.
 *
 * @author Alkisum
 * @version 4.1
 * @since 4.0
 */
public final class AddRouteDialog {

    /**
     * AddRouteDialog constructor.
     */
    private AddRouteDialog() {

    }

    /**
     * Show dialog to create a new route.
     *
     * @param context Context
     */
    public static void show(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // set view
        View view = View.inflate(context, R.layout.dialog_add_route, null);
        builder.setView(view);

        // set dialog title
        builder.setTitle(R.string.routes_add_dialog);

        // get edit text for route name
        final EditText routeName = view.findViewById(R.id.route_name);

        // positive button
        builder.setPositiveButton(android.R.string.ok,
                (dialog, which) -> {
                    Routes.insertRoute(routeName.getText().toString());
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
