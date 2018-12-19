package com.alkisum.android.cloudrun.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.events.RefreshEvent;
import com.alkisum.android.cloudrun.model.Marker;
import com.alkisum.android.cloudrun.utils.Markers;

import org.greenrobot.eventbus.EventBus;

/**
 * Dialog to edit an existing marker.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public final class EditMarkerDialog {

    /**
     * EditMarkerDialog constructor.
     */
    private EditMarkerDialog() {

    }

    /**
     * Show dialog to edit an existing marker.
     *
     * @param context Context
     * @param marker   Marker to edit
     */
    public static void show(final Context context, final Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // set view
        View view = View.inflate(context, R.layout.dialog_edit_marker, null);
        builder.setView(view);

        // set dialog title
        builder.setTitle(R.string.marker_edit_dialog);

        // get edit text for marker label
        final EditText markerLabel = view.findViewById(R.id.marker_label);
        markerLabel.setHint(marker.getLabel());

        // positive button
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        Markers.updateMarker(marker,
                                markerLabel.getText().toString());
                        EventBus.getDefault().post(new RefreshEvent());
                        dialog.dismiss();
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
