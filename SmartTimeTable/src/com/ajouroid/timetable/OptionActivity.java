package com.ajouroid.timetable;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.widget.Toast;

public class OptionActivity extends PreferenceActivity 
	implements Preference.OnPreferenceChangeListener, DialogInterface.OnClickListener {
	
	TimePickerPreference starttime;
	TimePickerPreference endtime;
	TimePickerPreference basetime;
	
	TimePickerPreference resttime;
	EditTextPreference message;
	
	TimePickerPreference goingtime;
	RingtonePreference alarmmusic;
	ListPreference alarmtime;
	CheckBoxPreference morningcall;
	CheckBoxPreference alarm;
	CheckBoxPreference reject;
	
	SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.layout.option);
		
		prefs = getSharedPreferences("com.ajouroid.timetable_preferences", 0);
		
		goingtime = (TimePickerPreference)findPreference("goingtime");
		alarmmusic = (RingtonePreference)findPreference("alarm_music");
		alarmtime = (ListPreference)findPreference("alarm_time");
		
		alarm = (CheckBoxPreference)findPreference("alarm");
		morningcall = (CheckBoxPreference)findPreference("morningcall");
		
		starttime = (TimePickerPreference)findPreference("start");
		endtime = (TimePickerPreference)findPreference("end");
		basetime = (TimePickerPreference)findPreference("base");
		
		resttime = (TimePickerPreference)findPreference("resttime");
		message = (EditTextPreference)findPreference("message");
		reject = (CheckBoxPreference)findPreference("usereject");

	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		starttime.setSummary(getResources().getString(R.string.opt_current) + " " + new Time(prefs.getString("start", "09:00")).to12Hour());
		endtime.setSummary(getResources().getString(R.string.opt_current) + " " + new Time(prefs.getString("end", "18:00")).to12Hour());
		basetime.setSummary(getResources().getString(R.string.opt_current) + " " + new Time(prefs.getString("base", "1:30")).toString());
		
		alarm.setOnPreferenceChangeListener(this);
		morningcall.setOnPreferenceChangeListener(this);
		starttime.setOnPreferenceChangeListener(this);
		endtime.setOnPreferenceChangeListener(this);
		reject.setOnPreferenceChangeListener(this);
		
		
		// üũ�ڽ��� ���� ��� �� �׸��� enable �Ӽ��� ������
		if (prefs.getBoolean("alarm", false))
		{
			alarmtime.setEnabled(true);
		}
		
		if (prefs.getBoolean("morningcall", false))
		{
			alarmmusic.setEnabled(true);
			goingtime.setEnabled(true);
		}
		
		if (prefs.getBoolean("usereject", false))
		{
			message.setEnabled(true);
			resttime.setEnabled(true);
		}
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		
		if (preference.getKey().compareTo("aboutus")==0)
		{
			Intent aboutUsActivity = new Intent(this, AboutUsActivity.class);
			
			startActivity(aboutUsActivity);
		}
		
		else if (preference.getKey().compareTo("init") == 0)
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle(getResources().getString(R.string.warning));
			dialog.setMessage(getResources().getString(R.string.opt_initMsg));
			dialog.setPositiveButton(getResources().getString(R.string.ok), this);
			dialog.setNegativeButton(getResources().getString(R.string.cancel), this);
			AlertDialog alert = dialog.create();
			alert.show();
		}
		else if (preference.getKey().compareTo("backup") == 0)
		{
			DBAdapter dbA = new DBAdapter(this);

			dbA.backup();

			Toast.makeText(this, "데이터 백업이 완료되었습니다.", Toast.LENGTH_SHORT).show();
		}
		else if (preference.getKey().compareTo("restore") == 0)
		{
			DBAdapter dbA = new DBAdapter(this);

			dbA.restore("/sdcard/SmartTimeTable/backup.txt");
			
			Toast.makeText(this, "데이터 복구가 완료되었습니다.", Toast.LENGTH_SHORT).show();
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
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
		
		else if (preference == reject)
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

		else if (preference == starttime)
		{
			Time startTime = new Time((String)newValue);
			Time endTime = new Time(prefs.getString("end", "18:00"));
			
			if (startTime.toMinute() >= endTime.toMinute())
			{
				Toast.makeText(this, getResources().getString(R.string.opt_timeErrMsg), Toast.LENGTH_SHORT).show();
				return false;
			}
			else
			{
				starttime.setSummary(getResources().getString(R.string.opt_current) + " " + new Time((String)newValue).to12Hour());
				return true;
			}
		}
		
		else if (preference == endtime)
		{
			Time endTime = new Time((String)newValue);
			Time startTime = new Time(prefs.getString("start", "09:00"));
			
			if (startTime.toMinute() >= endTime.toMinute())
			{
				Toast.makeText(this, getResources().getString(R.string.opt_timeErrMsg), Toast.LENGTH_SHORT).show();
				return false;
			}
			else
			{
				endtime.setSummary(getResources().getString(R.string.opt_current) + " " + new Time((String)newValue).to12Hour());
				return true;
			}
		}
		
		else if (preference == basetime)
		{
			basetime.setSummary(getResources().getString(R.string.opt_current) + " " + new Time(prefs.getString("base", "1:30")).toString());
			return true;
		}
		// TODO Auto-generated method stub
		return false;
	}

	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		if (which == DialogInterface.BUTTON1)
		{
			(new DBAdapter(this)).init();
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			boolean db_complete = prefs.getBoolean("db_complete", false);
			String db_version = prefs.getString("db_version", "-1");
			Editor edit = prefs.edit();
			edit.clear();
			/*
			edit.putBoolean("db_complete", db_complete);
			edit.putString("db_version", db_version);*/
			edit.commit();
			finish();
		}
	}
}
