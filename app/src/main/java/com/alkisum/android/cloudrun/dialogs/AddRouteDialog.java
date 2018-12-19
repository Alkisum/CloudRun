package com.alkisum.android.cloudrun.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.events.RefreshEvent;
import com.alkisum.android.cloudrun.utils.Routes;

import org.greenrobot.eventbus.EventBus;

/**
 * Dialog to add a new route.
 *
 * @author Alkisum
 * @version 4.0
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
        builder.setTitle(R.string.route_add_dialog);

        // get edit text for route name
        final EditText routeName = view.findViewById(R.id.route_name);

        // positive button
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        Routes.insertRoute(routeName.getText().toString());
                        EventBus.getDefault().post(new RefreshEvent());
                    }
                });

        // negative button
        builder.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        dialog.cancel();
                    }
                });

        // show dialog
        builder.show();
    }
}
