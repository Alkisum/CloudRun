package com.alkisum.android.cloudrun.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.alkisum.android.cloudrun.R;

/**
 * Dialog showing the list of data type that can be applied to a tile.
 *
 * @author Alkisum
 * @version 3.0
 * @since 1.3
 */

public final class TileDialog {

    /**
     * TileDialog constructor.
     */
    private TileDialog() {

    }

    /**
     * Build the TileDialog.
     *
     * @param context         Context
     * @param current         Current data type, used to check the item
     * @param onClickListener Action to perform when an item is checked by user
     * @return TileDialog builder
     */
    public static AlertDialog.Builder build(
            final Context context, final int current,
            final DialogInterface.OnClickListener onClickListener) {
        return new AlertDialog.Builder(context).setSingleChoiceItems(
                R.array.dataTile, current, onClickListener);
    }
}
