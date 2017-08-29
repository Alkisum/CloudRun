package com.alkisum.android.cloudrun.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import com.alkisum.android.cloudrun.R;

/**
 * A dialog preference that shows a NumberPicker.
 *
 * @author Alkisum
 * @version 2.2
 * @since 2.2
 */
public class NumberPickerPreference extends DialogPreference {

    /**
     * Default max value.
     */
    private static final int DEFAULT_MAX_VALUE = 100;

    /**
     * Default min value.
     */
    private static final int DEFAULT_MIN_VALUE = 0;

    /**
     * Default wrap selector wheel flag.
     */
    private static final boolean DEFAULT_WRAP_SELECTOR_WHEEL = true;

    /**
     * Lower value of the range of numbers allowed for the NumberPicker.
     */
    private final int mMinValue;

    /**
     * Upper value of the range of numbers allowed for the NumberPicker.
     */
    private final int mMaxValue;

    /**
     * User choice on whether the selector wheel should be wrapped.
     */
    private final boolean mWrapSelectorWheel;

    /**
     * NumberPicker instance.
     */
    private NumberPicker mPicker;

    /**
     * Current value of the NumberPicker.
     */
    private int mValue;

    /**
     * NumberPickerPreference constructor.
     *
     * @param context Context
     * @param attrs   Set of attributes
     */
    public NumberPickerPreference(final Context context,
                                  final AttributeSet attrs) {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);
    }

    /**
     * NumberPickerPreference constructor.
     *
     * @param context      Context
     * @param attrs        Set of attributes
     * @param defStyleAttr Style attribute
     */
    public NumberPickerPreference(final Context context,
                                  final AttributeSet attrs,
                                  final int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.NumberPickerPreference);
        mMinValue = a.getInteger(R.styleable.NumberPickerPreference_minValue,
                DEFAULT_MIN_VALUE);
        mMaxValue = a.getInteger(R.styleable.NumberPickerPreference_maxValue,
                DEFAULT_MAX_VALUE);
        mWrapSelectorWheel = a.getBoolean(
                R.styleable.NumberPickerPreference_wrapSelectorWheel,
                DEFAULT_WRAP_SELECTOR_WHEEL);
        a.recycle();
    }

    @Override
    protected final View onCreateDialogView() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;

        mPicker = new NumberPicker(getContext());
        mPicker.setLayoutParams(layoutParams);

        FrameLayout dialogView = new FrameLayout(getContext());
        dialogView.addView(mPicker);

        return dialogView;
    }

    @Override
    protected final void onBindDialogView(final View view) {
        super.onBindDialogView(view);
        mPicker.setMinValue(mMinValue);
        mPicker.setMaxValue(mMaxValue);
        mPicker.setWrapSelectorWheel(mWrapSelectorWheel);
        mPicker.setValue(getValue());
    }

    @Override
    protected final void onDialogClosed(final boolean positiveResult) {
        if (positiveResult) {
            mPicker.clearFocus();
            int newValue = mPicker.getValue();
            if (callChangeListener(newValue)) {
                setValue(newValue);
            }
        }
    }

    @Override
    protected final Object onGetDefaultValue(final TypedArray a,
                                             final int index) {
        return a.getInt(index, mMinValue);
    }

    @Override
    protected final void onSetInitialValue(final boolean restorePersistedValue,
                                           final Object defaultValue) {
        if (restorePersistedValue) {
            setValue(getPersistedInt(mMinValue));
        } else {
            setValue((Integer) defaultValue);
        }
    }

    /**
     * @param value Current value to set
     */
    public final void setValue(final int value) {
        mValue = value;
        persistInt(mValue);
    }

    /**
     * @return Current value
     */
    public final int getValue() {
        return mValue;
    }
}
