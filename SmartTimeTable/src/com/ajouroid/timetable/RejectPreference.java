package com.ajouroid.timetable;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class RejectPreference extends PreferenceActivity implements
Preference.OnPreferenceChangeListener {

	SharedPreferences prefs;
	
	CheckBoxPreference reject;
	TimePickerPreference resttime;
	EditTextPreference message;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		this.addPreferencesFromResource(R.xml.rejectpref);
		
		prefs = getSharedPreferences("com.ajouroid.timetable_preferences", 0);
		
		reject = (CheckBoxPreference)findPreference("usereject");
		resttime = (TimePickerPreference)findPreference("resttime");
		message = (EditTextPreference)findPreference("message");
	}

	@Override
	protected void onResume() {
		reject.setOnPreferenceChangeListener(this);
		
		if (prefs.getBoolean("usereject", false))
		{
			message.setEnabled(true);
			resttime.setEnabled(true);
		}
		super.onResume();
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == reject)
		{
			if (!reject.isChecked())
			{
				message.setEnabled(true);
				resttime.setEnabled(true);
			}
			else
			{
				message.setEnabled(false);
				resttime.setEnabled(false);
			}
			
			return true;
		}
		return false;
	}
	
}
