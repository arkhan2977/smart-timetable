package com.ajouroid.timetable;

//Please note this must be the package if you want to use XML-based preferences

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

/**
 * A preference type that allows a user to choose a time
 */
public class TimePickerPreference extends DialogPreference {

	/**
	 * The validation expression for this preference
	 */
	private static final String VALIDATION_EXPRESSION = "[0-2]*[0-9]:[0-5]*[0-9]";

	private boolean hoursystem = false;
	/**
	 * The default value for this preference
	 */
	private String defaultValue;
	
	TimePicker tp;

	/**
	 * @param context
	 * @param attrs
	 */
	public TimePickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.TimePickerPreference);
		hoursystem = a.getBoolean(R.styleable.TimePickerPreference_is24hour,
				false);
		defaultValue = a
				.getString(R.styleable.TimePickerPreference_defaultValue);
		initialize();
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public TimePickerPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.TimePickerPreference);
		hoursystem = a.getBoolean(R.styleable.TimePickerPreference_is24hour,
				false);
		defaultValue = a
				.getString(R.styleable.TimePickerPreference_defaultValue);
		initialize();
	}

	/**
	 * Initialize this preference
	 */
	private void initialize() {
		setPersistent(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.DialogPreference#onCreateDialogView()
	 */
	@Override
	protected View onCreateDialogView() {

		tp = new TimePicker(getContext());
		tp.setIs24HourView(hoursystem);
		// tp.setOnTimeChangedListener(this);

		int h = getHour();
		int m = getMinute();
		if (h >= 0 && m >= 0) {
			tp.setCurrentHour(h);
			tp.setCurrentMinute(m);
		}

		return tp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.TimePicker.OnTimeChangedListener#onTimeChanged(android
	 * .widget.TimePicker, int, int)
	 */

	/*
	 * public void onTimeChanged(TimePicker view, int hour, int minute) {
	 * persistString(hour + ":" + minute); }
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.Preference#setDefaultValue(java.lang.Object)
	 */
	@Override
	public void setDefaultValue(Object defaultValue) {
		// BUG this method is never called if you use the 'android:defaultValue'
		// attribute in your XML preference file, not sure why it isn't

		super.setDefaultValue(defaultValue);

		if (!(defaultValue instanceof String)) {
			return;
		}

		if (!((String) defaultValue).matches(VALIDATION_EXPRESSION)) {
			return;
		}

		this.defaultValue = (String) defaultValue;
	}

	/**
	 * Get the hour value (in 24 hour time)
	 * 
	 * @return The hour value, will be 0 to 23 (inclusive)
	 */
	private int getHour() {
		String time = getPersistedString(this.defaultValue);
		if (time == null || !time.matches(VALIDATION_EXPRESSION)) {
			return -1;
		}

		return Integer.parseInt(time.split(":")[0]);
	}

	/**
	 * Get the minute value
	 * 
	 * @return the minute value, will be 0 to 59 (inclusive)
	 */
	private int getMinute() {
		String time = getPersistedString(this.defaultValue);
		if (time == null || !time.matches(VALIDATION_EXPRESSION)) {
			return -1;
		}

		return Integer.parseInt(time.split(":")[1]);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			tp.clearFocus();
			int hour = tp.getCurrentHour();
			int minute = tp.getCurrentMinute();
			String result = hour + ":" + minute;
			if (callChangeListener(result))
				persistString(result);			
		}
	}
}