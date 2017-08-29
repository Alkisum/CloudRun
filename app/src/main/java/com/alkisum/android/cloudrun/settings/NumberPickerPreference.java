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
 * @version 3.0
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
    private final int minValue;

    /**
     * Upper value of the range of numbers allowed for the NumberPicker.
     */
    private final int maxValue;

    /**
     * User choice on whether the selector wheel should be wrapped.
     */
    private final boolean wrapSelectorWheel;

    /**
     * NumberPicker instance.
     */
    private NumberPicker picker;

    /**
     * Current value of the NumberPicker.
     */
    private int value;

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
        minValue = a.getInteger(R.styleable.NumberPickerPreference_minValue,
                DEFAULT_MIN_VALUE);
        maxValue = a.getInteger(R.styleable.NumberPickerPreference_maxValue,
                DEFAULT_MAX_VALUE);
        wrapSelectorWheel = a.getBoolean(
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

        picker = new NumberPicker(getContext());
        picker.setLayoutParams(layoutParams);

        FrameLayout dialogView = new FrameLayout(getContext());
        dialogView.addView(picker);

        return dialogView;
    }

    @Override
    protected final void onBindDialogView(final View view) {
        super.onBindDialogView(view);
        picker.setMinValue(minValue);
        picker.setMaxValue(maxValue);
        picker.setWrapSelectorWheel(wrapSelectorWheel);
        picker.setValue(getValue());
    }

    @Override
    protected final void onDialogClosed(final boolean positiveResult) {
        if (positiveResult) {
            picker.clearFocus();
            int newValue = picker.getValue();
            if (callChangeListener(newValue)) {
                setValue(newValue);
            }
        }
    }

    @Override
    protected final Object onGetDefaultValue(final TypedArray a,
                                             final int index) {
        return a.getInt(index, minValue);
    }

    @Override
    protected final void onSetInitialValue(final boolean restorePersistedValue,
                                           final Object defaultValue) {
        if (restorePersistedValue) {
            setValue(getPersistedInt(minValue));
        } else {
            setValue((Integer) defaultValue);
        }
    }

    /**
     * @param value Current value to set
     */
    public final void setValue(final int value) {
        this.value = value;
        persistInt(this.value);
    }

    /**
     * @return Current value
     */
    public final int getValue() {
        return value;
    }
}
