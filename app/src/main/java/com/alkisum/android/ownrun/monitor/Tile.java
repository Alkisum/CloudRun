package com.alkisum.android.ownrun.monitor;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alkisum.android.ownrun.R;
import com.alkisum.android.ownrun.dialog.TileDialog;

/**
 * Layout containing a value and a unit TextView.
 *
 * @author Alkisum
 * @version 2.2
 * @since 1.3
 */
class Tile {

    /**
     * Constant for distance data type.
     */
    static final int DISTANCE = 0;

    /**
     * Constant for speed data type.
     */
    static final int SPEED = 1;

    /**
     * Constant for pace data type.
     */
    static final int PACE = 2;

    /**
     * Constant for speed average data type.
     */
    static final int SPEED_AVG = 3;

    /**
     * Constant for pace average data type.
     */
    static final int PACE_AVG = 4;

    /**
     * Context.
     */
    private final Context mContext;

    /**
     * TileListener instance.
     */
    private final TileListener mCallback;

    /**
     * Preference key to edit when the data type is changed.
     */
    private final String mPrefKey;

    /**
     * Data type shown on the tile.
     */
    private int mData;

    /**
     * Layout used for the tile.
     */
    private final RelativeLayout mLayout;

    /**
     * Value shown on the tile.
     */
    private final TextView mValue;

    /**
     * Unit shown on the tile.
     */
    private final TextView mUnit;

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
    Tile(final Context context, final TileListener callback,
         final String prefKey, final int data,
         final RelativeLayout layout, final TextView value,
         final TextView unit) {
        mContext = context;
        mCallback = callback;
        mData = data;
        mPrefKey = prefKey;
        mLayout = layout;
        mLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                mCallback.onTileLongClicked();
                // Dialog's checked item is set according to the data constant
                TileDialog.build(mContext, mData,
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
        mValue = value;
        mUnit = unit;
        reset();
    }

    /**
     * Reset value and unit with default strings according to the data type.
     */
    private void reset() {
        switch (mData) {
            case DISTANCE:
                mValue.setText(R.string.default_distance);
                mUnit.setText(R.string.unit_distance);
                break;
            case SPEED:
                mValue.setText(R.string.default_speed);
                mUnit.setText(R.string.unit_speed);
                break;
            case PACE:
                mValue.setText(R.string.default_pace);
                mUnit.setText(R.string.unit_pace);
                break;
            case SPEED_AVG:
                mValue.setText(R.string.default_speed);
                mUnit.setText(R.string.unit_speed_avg);
                break;
            case PACE_AVG:
                mValue.setText(R.string.default_pace);
                mUnit.setText(R.string.unit_pace_avg);
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
        mData = newData;
        mCallback.onTileValueRequested(this, newData);

        // Update SharedPreferences
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(mPrefKey, newData);
        editor.apply();
    }

    /**
     * Reset the values with default strings according to the data type.
     */
    final void resetValues() {
        switch (mData) {
            case DISTANCE:
                mValue.setText(R.string.default_distance);
                break;
            case SPEED:
                mValue.setText(R.string.default_speed);
                break;
            case PACE:
                mValue.setText(R.string.default_pace);
                break;
            case SPEED_AVG:
                mValue.setText(R.string.default_speed);
                break;
            case PACE_AVG:
                mValue.setText(R.string.default_pace);
                break;
            default:
                break;
        }
    }

    /**
     * Reset the units with default strings according to the data type.
     */
    private void resetUnits() {
        switch (mData) {
            case DISTANCE:
                mUnit.setText(R.string.unit_distance);
                break;
            case SPEED:
                mUnit.setText(R.string.unit_speed);
                break;
            case PACE:
                mUnit.setText(R.string.unit_pace);
                break;
            case SPEED_AVG:
                mUnit.setText(R.string.unit_speed_avg);
                break;
            case PACE_AVG:
                mUnit.setText(R.string.unit_pace_avg);
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
    final void setEnabled(final boolean enabled) {
        mLayout.setEnabled(enabled);
    }

    /**
     * @return Data type shown on the tile
     */
    public final int getData() {
        return mData;
    }

    /**
     * @param value Data type shown on the tile to set
     */
    public final void setValue(final String value) {
        mValue.setText(value);
    }

    /**
     * Interface to listen to actions performed by the user on the tile.
     */
    interface TileListener {

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
