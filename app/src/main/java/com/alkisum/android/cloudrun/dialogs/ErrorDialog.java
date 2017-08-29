package com.alkisum.android.cloudrun.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Class to build a simple error dialog.
 *
 * @author Alkisum
 * @version 3.0
 * @since 1.0
 */
public final class ErrorDialog {

    /**
     * ErrorDialog constructor.
     */
    private ErrorDialog() {

    }

    /**
     * Build the AlertDialog.
     *
     * @param context         Context in which the dialog should be built
     * @param title           Dialog title
     * @param message         Dialog message
     * @param onClickListener Task to run when the positive button is clicked
     * @return AlertDialog builder ready to be shown
     */
    public static AlertDialog.Builder build(
            final Context context, final String title, final String message,
            final DialogInterface.OnClickListener onClickListener) {
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, onClickListener)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false);
    }
}
