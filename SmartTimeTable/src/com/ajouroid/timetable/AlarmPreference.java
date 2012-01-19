package com.ajouroid.timetable;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.RingtonePreference;

public class AlarmPreference extends PreferenceActivity implements
		Preference.OnPreferenceChangeListener {

	TimePickerPreference goingtime;
	RingtonePreference alarmmusic;
	ListPreference alarmtime;
	ListPreference tasktime;
	CheckBoxPreference morningcall;
	CheckBoxPreference alarm;
	CheckBoxPreference task;
	
	SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		this.addPreferencesFromResource(R.xml.alarmpref);
		
		prefs = getSharedPreferences("com.ajouroid.timetable_preferences", 0);

		goingtime = (TimePickerPreference) findPreference("goingtime");
		alarmmusic = (RingtonePreference) findPreference("alarm_music");
		alarmtime = (ListPreference) findPreference("alarm_time");
		tasktime = (ListPreference) findPreference("task_time");

		alarm = (CheckBoxPreference) findPreference("alarm");
		morningcall = (CheckBoxPreference) findPreference("morningcall");
		task = (CheckBoxPreference) findPreference("task");
	}

	@Override
	protected void onResume() {
		alarm.setOnPreferenceChangeListener(this);
		morningcall.setOnPreferenceChangeListener(this);
		task.setOnPreferenceChangeListener(this);

		if (prefs.getBoolean("alarm", false)) {
			alarmtime.setEnabled(true);
		}

		if (prefs.getBoolean("morningcall", false)) {
			alarmmusic.setEnabled(true);
			goingtime.setEnabled(true);
		}
		
		if (prefs.getBoolean("task", false)) {
			tasktime.setEnabled(true);
		}
		super.onResume();
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == alarm)
		{
			if (!alarm.isChecked())
			{
				alarmtime.setEnabled(true);
			}
			else
			{
				alarmtime.setEnabled(false);
			}
			
			return true;
		}
		
		else if (preference == morningcall)
		{
			if (!morningcall.isChecked())
			{
				alarmmusic.setEnabled(true);
				goingtime.setEnabled(true);
			}
			else
			{
				alarmmusic.setEnabled(false);
				goingtime.setEnabled(false);
			}
			
			return true;
		}
		
		else if (preference == task)
		{
			if (!task.isChecked())
			{
				tasktime.setEnabled(true);
			}
			else
			{
				tasktime.setEnabled(false);
			}
			
			return true;
		}
		
		return false;
	}

}
