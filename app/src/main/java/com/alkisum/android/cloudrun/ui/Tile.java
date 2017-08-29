package com.alkisum.android.cloudrun.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.dialogs.TileDialog;

/**
 * Layout containing a value and a unit TextView.
 *
 * @author Alkisum
 * @version 3.0
 * @since 1.3
 */
public class Tile {

    /**
     * Constant for distance data type.
     */
    public static final int DISTANCE = 0;

    /**
     * Constant for speed data type.
     */
    public static final int SPEED = 1;

    /**
     * Constant for pace data type.
     */
    public static final int PACE = 2;

    /**
     * Constant for speed average data type.
     */
    public static final int SPEED_AVG = 3;

    /**
     * Constant for pace average data type.
     */
    public static final int PACE_AVG = 4;

    /**
     * Context.
     */
    private final Context context;

    /**
     * TileListener instance.
     */
    private final TileListener callback;

    /**
     * Preference key to edit when the data type is changed.
     */
    private final String prefKey;

    /**
     * Data type shown on the tile.
     */
    private int data;

    /**
     * Layout used for the tile.
     */
    private final RelativeLayout layout;

    /**
     * Value shown on the tile.
     */
    private final TextView value;

    /**
     * Unit shown on the tile.
     */
    private final TextView unit;

    /**
     * Tile constructor.
     *
     * @param context  Context
     * @param callback TileListener instance
     * @param prefKey  Preference key to edit when the data type is changed
     * @param data     Data type shown on the tile
     * @param layout   Layout used for the tile
     * @param value    Value shown on the tile
     * @param unit     Unit shown on the tile
     */
    public Tile(final Context context, final TileListener callback,
                final String prefKey, final int data,
                final RelativeLayout layout, final TextView value,
                final TextView unit) {
        this.context = context;
        this.callback = callback;
        this.data = data;
        this.prefKey = prefKey;
        this.layout = layout;
        this.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                Tile.this.callback.onTileLongClicked();
                // Dialog's checked item is set according to the data constant
                TileDialog.build(Tile.this.context, Tile.this.data,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int which) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        changeData(which);
                                        resetUnits();
                                        dialog.dismiss();
                                    }
                                }, 500);
                            }
                        }).show();
                return false;
            }
        });
        this.value = value;
        this.unit = unit;
        reset();
    }

    /**
     * Reset value and unit with default strings according to the data type.
     */
    private void reset() {
        switch (data) {
            case DISTANCE:
                value.setText(R.string.default_distance);
                unit.setText(R.string.unit_distance);
                break;
            case SPEED:
                value.setText(R.string.default_speed);
                unit.setText(R.string.unit_speed);
                break;
            case PACE:
                value.setText(R.string.default_pace);
                unit.setText(R.string.unit_pace);
                break;
            case SPEED_AVG:
                value.setText(R.string.default_speed);
                unit.setText(R.string.unit_speed_avg);
                break;
            case PACE_AVG:
                value.setText(R.string.default_pace);
                unit.setText(R.string.unit_pace_avg);
                break;
            default:
                break;
        }
    }

    /**
     * Called when the user change tile's data type from the dialog.
     *
     * @param newData New data type chosen by the user
     */
    private void changeData(final int newData) {
        data = newData;
        callback.onTileValueRequested(this, newData);

        // Update SharedPreferences
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(prefKey, newData);
        editor.apply();
    }

    /**
     * Reset the values with default strings according to the data type.
     */
    public final void resetValues() {
        switch (data) {
            case DISTANCE:
                value.setText(R.string.default_distance);
                break;
            case SPEED:
                value.setText(R.string.default_speed);
                break;
            case PACE:
                value.setText(R.string.default_pace);
                break;
            case SPEED_AVG:
                value.setText(R.string.default_speed);
                break;
            case PACE_AVG:
                value.setText(R.string.default_pace);
                break;
            default:
                break;
        }
    }

    /**
     * Reset the units with default strings according to the data type.
     */
    private void resetUnits() {
        switch (data) {
            case DISTANCE:
                unit.setText(R.string.unit_distance);
                break;
            case SPEED:
                unit.setText(R.string.unit_speed);
                break;
            case PACE:
                unit.setText(R.string.unit_pace);
                break;
            case SPEED_AVG:
                unit.setText(R.string.unit_speed_avg);
                break;
            case PACE_AVG:
                unit.setText(R.string.unit_pace_avg);
                break;
            default:
                break;
        }
    }

    /**
     * Enable or disable the long click action on the tile.
     *
     * @param enabled True to enable long click action, false otherwise
     */
    public final void setEnabled(final boolean enabled) {
        layout.setEnabled(enabled);
    }

    /**
     * @return Data type shown on the tile
     */
    public final int getData() {
        return data;
    }

    /**
     * @param value Data type shown on the tile to set
     */
    public final void setValue(final String value) {
        this.value.setText(value);
    }

    /**
     * Interface to listen to actions performed by the user on the tile.
     */
    public interface TileListener {

        /**
         * Called when the user clicks long a the tile.
         */
        void onTileLongClicked();

        /**
         * Called when the user changed the tile's data type. The last chosen
         * data type's value is requested to update the TextView
         *
         * @param tile Changed tile
         * @param data New tile's data type
         */
        void onTileValueRequested(Tile tile, int data);
    }
}
