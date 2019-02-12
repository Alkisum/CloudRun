package com.alkisum.android.cloudrun.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;

import com.alkisum.android.cloudrun.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

/**
 * Dialog to set the distance.
 *
 * @author Alkisum
 * @version 4.0
 * @since 2.0
 */

public class DistanceDialog extends DialogFragment {

    /**
     * Fragment tag for FragmentManager.
     */
    public static final String FRAGMENT_TAG = "distance_dialog";

    /**
     * Argument for distance.
     */
    private static final String ARG_DISTANCE = "arg_distance";

    /**
     * Listener for the dialog.
     */
    private DistanceDialogListener callback;

    /**
     * Create a new instance of DistanceDialog.
     *
     * @param distance Default distance
     * @return New instance of DistanceDialog
     */
    public static DistanceDialog newInstance(final float distance) {
        DistanceDialog distanceDialog = new DistanceDialog();
        Bundle args = new Bundle();
        args.putFloat(ARG_DISTANCE, distance);
        distanceDialog.setArguments(args);
        return distanceDialog;
    }

    @Override
    public final void onAttach(final Context context) {
        super.onAttach(context);
        try {
            callback = (DistanceDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().getSimpleName()
                    + " must implement DistanceDialogListener");
        }
    }

    @NonNull
    @Override
    public final Dialog onCreateDialog(final Bundle savedInstanceState) {
        float distance = 0;
        if (getArguments() != null) {
            distance = getArguments().getFloat(ARG_DISTANCE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = View.inflate(getActivity(), R.layout.dialog_distance, null);

        final NumberPicker nbInteger = view.findViewById(
                R.id.distance_nb_integer);
        nbInteger.setMinValue(0);
        nbInteger.setMaxValue(99);
        int integerPart = (int) (distance / 1000f);
        nbInteger.setValue(integerPart);
        final NumberPicker nbFractional = view.findViewById(
                R.id.distance_nb_fractional);
        nbFractional.setMinValue(0);
        nbFractional.setMaxValue(99);
        int fractionalPart = Math.round(
                ((distance / 1000f) - integerPart) * 100);
        nbFractional.setValue(fractionalPart);

        builder.setView(view)
                .setTitle(R.string.distance_title)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int id) {
                                float result = (nbInteger.getValue()
                                        + nbFractional.getValue() / 100f)
                                        * 1000;
                                callback.onDistanceSubmit(result);

                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog,
                                                final int id) {
                                DistanceDialog.this.getDialog().cancel();
                            }
                        });
        return builder.create();
    }

    /**
     * Listener for the dialog.
     */
    public interface DistanceDialogListener {

        /**
         * Called when the user submit the dialog.
         *
         * @param distance New distance set
         */
        void onDistanceSubmit(float distance);
    }
}
