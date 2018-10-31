package com.alkisum.android.cloudrun.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.database.Markers;
import com.alkisum.android.cloudrun.events.MarkerInsertedEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Dialog to add a new marker.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public final class AddMarkerDialog {

    /**
     * AddMarkerDialog constructor.
     */
    private AddMarkerDialog() {

    }

    /**
     * Show dialog to create a new marker.
     *
     * @param context   Context
     * @param latitude  Latitude
     * @param longitude Longitude
     * @param routeId   Route id
     */
    public static void show(final Context context, final double latitude,
                            final double longitude, final long routeId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // set view
        View view = View.inflate(context, R.layout.dialog_add_marker, null);
        builder.setView(view);

        // set dialog title
        builder.setTitle(R.string.marker_add_dialog);

        // get edit text for marker name
        final EditText markerLabel = view.findViewById(R.id.marker_label);

        // positive button
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        Markers.insertMarker(markerLabel.getText().toString(),
                                latitude, longitude, routeId);
                        EventBus.getDefault().post(new MarkerInsertedEvent());
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
