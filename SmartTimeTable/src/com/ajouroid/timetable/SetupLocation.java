package com.ajouroid.timetable;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

public class SetupLocation extends PreferenceActivity {
	AlertDialog alert;
	private SharedPreferences mPrefs;
	PreferenceScreen show_version;
	
	Resources r;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.setuplocation);
		show_version = (PreferenceScreen) findPreference("db_version");
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		r = getResources();
		/*
		Editor edit = mPrefs.edit();
		edit.remove("db_complete");
		edit.commit();
		*/
		
		boolean validDb = mPrefs.getBoolean("db_complete", false);
		
		if (validDb)
		{
				String ver = mPrefs.getString("db_version", "-1");
				show_version.setSummary(r.getString(R.string.version) + ": " + ver);
		}
		else
			show_version.setSummary(R.string.dbdown_noDatabase);
		registerReceiver(receiver, new IntentFilter("com.ajouroid.timetable.DOWNLOAD_COMPLETE"));
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		// TODO Auto-generated method stub
		if(preference.getKey().compareToIgnoreCase("find_station") == 0){
			Intent stationActivity = new Intent(this,StationSetting.class);
			startActivity(stationActivity);
		}
		else if (preference.getKey().compareToIgnoreCase("db_version") == 0)
		{
				VersionCheckTask ver_task = new VersionCheckTask(SetupLocation.this);
				ver_task.execute();
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			boolean validDb = mPrefs.getBoolean("db_complete", false);
			
			if (validDb)
			{
					String ver = mPrefs.getString("db_version", "-1");
					show_version.setSummary(r.getString(R.string.version) + ": " + ver);
			}
			else
				show_version.setSummary(R.string.dbdown_noDatabase);
			
			Log.d("SmartTimeTable", "DB Download Completed.");
		}
	};
}
