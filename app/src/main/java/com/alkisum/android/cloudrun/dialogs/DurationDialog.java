package com.alkisum.android.cloudrun.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;

import com.alkisum.android.cloudrun.R;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

/**
 * Dialog to set the duration.
 *
 * @author Alkisum
 * @version 4.1
 * @since 2.0
 */

public class DurationDialog extends DialogFragment {

    /**
     * Fragment tag for FragmentManager.
     */
    public static final String FRAGMENT_TAG = "duration_dialog";

    /**
     * Argument for default hour.
     */
    private static final String ARG_HOUR = "arg_hour";

    /**
     * Argument for default minute.
     */
    private static final String ARG_MINUTE = "arg_minute";

    /**
     * Argument for default second.
     */
    private static final String ARG_SECOND = "arg_second";

    /**
     * Listener for the dialog.
     */
    private DurationDialogListener callback;

    /**
     * Create a new instance of DurationDialog.
     *
     * @param hour   Default hour
     * @param minute Default minute
     * @param second Default second
     * @return New instance of DurationDialog
     */
    public static DurationDialog newInstance(final int hour, final int minute,
                                             final int second) {
        DurationDialog durationDialog = new DurationDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_HOUR, hour);
        args.putInt(ARG_MINUTE, minute);
        args.putInt(ARG_SECOND, second);
        durationDialog.setArguments(args);
        return durationDialog;
    }

    @Override
    public final void onAttach(@NonNull final Context context) {
        super.onAttach(context);
        try {
            callback = (DurationDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().getSimpleName()
                    + " must implement DurationDialogListener");
        }
    }

    @NonNull
    @Override
    public final Dialog onCreateDialog(final Bundle savedInstanceState) {
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (getArguments() != null) {
            hour = getArguments().getInt(ARG_HOUR);
            minute = getArguments().getInt(ARG_MINUTE);
            second = getArguments().getInt(ARG_SECOND);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = View.inflate(getActivity(), R.layout.dialog_duration, null);

        final NumberPicker nbHour = view.findViewById(R.id.duration_nb_hour);
        nbHour.setMinValue(0);
        nbHour.setMaxValue(23);
        nbHour.setValue(hour);
        final NumberPicker nbMinute = view.findViewById(
                R.id.duration_nb_minute);
        nbMinute.setMinValue(0);
        nbMinute.setMaxValue(59);
        nbMinute.setValue(minute);
        final NumberPicker nbSecond = view.findViewById(
                R.id.duration_nb_second);
        nbSecond.setMinValue(0);
        nbSecond.setMaxValue(59);
        nbSecond.setValue(second);

        builder.setView(view)
                .setTitle(R.string.duration_title)
                .setPositiveButton(android.R.string.ok,
                        (dialog, id) -> callback.onDurationSubmit(
                                nbHour.getValue(),
                                nbMinute.getValue(),
                                nbSecond.getValue()))
                .setNegativeButton(android.R.string.cancel,
                        (dialog, id) -> Objects.requireNonNull(
                                DurationDialog.this.getDialog()).cancel());
        return builder.create();
    }

    /**
     * Listener for the dialog.
     */
    public interface DurationDialogListener {

        /**
         * Called when the user submit the dialog.
         *
         * @param hour   Hour set
         * @param minute Minute set
         * @param second Second set
         */
        void onDurationSubmit(int hour, int minute, int second);
    }
}
