package com.ajouroid.timetable;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

public class SetupLocation extends PreferenceActivity {


	AlertDialog alert;
	private SharedPreferences mPrefs;
	PreferenceScreen show_sp;
	PreferenceScreen show_dest;
	PreferenceScreen show_dis;
	PreferenceScreen show_version;

	private String addr_sp;
	private String addr_dest;
	private String msg;
	private double distance = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.layout.setuplocation);
		show_sp = (PreferenceScreen) findPreference("find_map_start");
		show_dest = (PreferenceScreen) findPreference("find_map_dest");
		show_dis = (PreferenceScreen) findPreference("show_distance");
		show_version = (PreferenceScreen) findPreference("db_version");
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		/*
		Editor edit = mPrefs.edit();
		edit.remove("db_complete");
		edit.commit();
		*/
		
		boolean validDb = mPrefs.getBoolean("db_complete", false);
		
		if (validDb)
		{
				String ver = mPrefs.getString("db_version", "-1");
				show_version.setSummary("버전: " + ver);
		}
		else
			show_version.setSummary("DB가 존재하지 않습니다. 다운로드하시려면 클릭하세요.");
		registerReceiver(receiver, new IntentFilter("com.ajouroid.timetable.DOWNLOAD_COMPLETE"));
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		addr_sp = mPrefs.getString("START_ADDRESS", getResources().getString(R.string.loc_unset));
		addr_dest = mPrefs.getString("DEST_ADDRESS", getResources().getString(R.string.loc_unset));
		show_sp.setSummary(addr_sp);
		show_dest.setSummary(addr_dest);
			

		String temp_sp_lat = mPrefs.getString("SP_LAT", "NULL");
		String temp_sp_lng = mPrefs.getString("SP_LNG", "NULL");
		String temp_dest_lat = mPrefs.getString("DEST_LAT", "NULL");
		String temp_dest_lng = mPrefs.getString("DEST_LNG", "NULL");

		if(temp_sp_lat.compareToIgnoreCase("NULL")==0 || temp_sp_lng.compareToIgnoreCase("NULL")==0){
			msg = "출발지가 올바르지 않습니다.";
		}
		else if(temp_dest_lat.compareToIgnoreCase("NULL")==0 || temp_dest_lng.compareToIgnoreCase("NULL")==0){
			msg = "도착지가 올바르지 않습니다.";
		}
		else{
			Double sp_lat = Double.parseDouble(temp_sp_lat);
			Double sp_lng = Double.parseDouble(temp_sp_lng);
			Double dest_lat = Double.parseDouble(temp_dest_lat);
			Double dest_lng = Double.parseDouble(temp_dest_lng);
			
			calc_distance(sp_lat, sp_lng, dest_lat, dest_lng);
		}
		
		show_dis.setSummary(msg);
		show_dis.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
			public boolean onPreferenceClick(Preference preference) { 
				alert = new AlertDialog.Builder(SetupLocation.this)
				.setTitle( "출발지 - 도착지 간 거리" )
				.setMessage(msg)
				.setPositiveButton( "확인", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				})
				.show();
				return true; 
			} 
		});		
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
		if(preference.getKey().compareToIgnoreCase("find_map_start") == 0){
			Intent mapActivity = new Intent(this,MapViewer.class);
			mapActivity.putExtra("TYPE", 0);
			startActivity(mapActivity);
		}
		else if(preference.getKey().compareToIgnoreCase("find_map_dest") == 0){
			Intent mapActivity = new Intent(this,MapViewer.class);
			mapActivity.putExtra("TYPE", 1);
			startActivity(mapActivity);
		}
		else if(preference.getKey().compareToIgnoreCase("find_station") == 0){
			Intent stationActivity = new Intent(this,StationSetting.class);
			startActivity(stationActivity);
		}
		else if(preference.getKey().compareToIgnoreCase("find_bus") == 0){
			Intent busActivity = new Intent(this,FindBusActivity.class);
			startActivity(busActivity);
		}
		else if (preference.getKey().compareToIgnoreCase("db_version") == 0)
		{
				//BaseCheckTask task = new BaseCheckTask(this);
				//task.execute();
			if (!mPrefs.getBoolean("db_complete", false))
			{
				DBDownloadTask task = new DBDownloadTask(this);
				task.execute();
			}
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	//출발지와 도착지 간 직선거리.
	public void calc_distance(Double sp_lat, Double sp_lng, Double dest_lat, Double dest_lng){

		Location locationA = new Location("point A");
 
		locationA.setLatitude(sp_lat);
		locationA.setLongitude(sp_lng);		

		Location locationB = new Location("point B");

		locationB.setLatitude(dest_lat);
		locationB.setLongitude(dest_lng);


		distance = locationA.distanceTo(locationB);

		if(distance > 1000){

			distance = distance / 1000;
			msg = new java.text.DecimalFormat("#.##").format(distance) + "km" ;
		}
		else{
			msg = new java.text.DecimalFormat("#.##").format(distance) + "m" ;
		}
	}
	
	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			boolean validDb = mPrefs.getBoolean("db_complete", false);
			
			if (validDb)
			{
					String ver = mPrefs.getString("db_version", "-1");
					show_version.setSummary("버전: " + ver);
			}
			else
				show_version.setSummary("DB가 존재하지 않습니다. 다운로드하시려면 클릭하세요.");
			
			Log.d("SmartTimeTable", "DB Download Completed.");
		}
	};
}
