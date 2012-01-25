package com.ajouroid.timetable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import android.widget.TextView;

public class StationInfoAlert extends MapActivity implements OnClickListener {
	static final int PROGRESS_DIALOG = 0;
	Button btn_set_start1;
	Button btn_set_dest1;
	Button btn_set_start2;
	Button btn_set_dest2;
	TextView sia_station_name;
	TextView sia_station_num;
	
	SharedPreferences sPref;
	BusStopInfo info;
	
	Double lat, lng;
	private MapView mapView = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		setContentView(R.layout.station_info_alert);	
		btn_set_start1 = (Button) findViewById(R.id.btn_set_start1);
		btn_set_dest1 = (Button) findViewById(R.id.btn_set_dest1);
		btn_set_start2 = (Button) findViewById(R.id.btn_set_start2);
		btn_set_dest2 = (Button) findViewById(R.id.btn_set_dest1);
		sia_station_name = (TextView) findViewById(R.id.sta_station_name);
		sia_station_num = (TextView) findViewById(R.id.sta_station_num);
		
		Intent i = getIntent();
		DBAdapterBus dbA = new DBAdapterBus(this);
		dbA.open();
		
		info = dbA.getBusStopInfoById(i.getStringExtra("id"));
		
		dbA.close();
		
		lat = info.getLatitude();
		lng = info.getLongitude();
		
		sia_station_name.setText(info.getStop_name());
		sia_station_num.setText(info.getNumber() + "");
		
		btn_set_start1.setOnClickListener(this);
		btn_set_dest1.setOnClickListener(this);
		btn_set_start2.setOnClickListener(this);
		btn_set_dest2.setOnClickListener(this);
		
	}

	@Override
	protected void onResume() {
		sPref = PreferenceManager.getDefaultSharedPreferences(this);
		super.onResume();
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	public void onClick(View v) {

		SharedPreferences.Editor ed = sPref.edit();
		Resources r = getResources();
		
		switch(v.getId())
		{
		case R.id.btn_set_start1:
			ed.putString("START_STOP",info.getStop_id()); 
			ed.putString("START_STOP_NAME", info.getStop_name());
			Toast.makeText(this, r.getString(R.string.bus_start1) + r.getString(R.string.isSet), Toast.LENGTH_SHORT).show();
			break;
		case R.id.btn_set_dest1:
			ed.putString("DEST_STOP",info.getStop_id()); 
			ed.putString("DEST_STOP_NAME", info.getStop_name());
			Toast.makeText(this, r.getString(R.string.bus_dest1) + r.getString(R.string.isSet), Toast.LENGTH_SHORT).show();
			break;
		case R.id.btn_set_start2:
			ed.putString("START_STOP_2",info.getStop_id()); 
			ed.putString("START_STOP_NAME_2", info.getStop_name());
			Toast.makeText(this, r.getString(R.string.bus_start2) + r.getString(R.string.isSet), Toast.LENGTH_SHORT).show();
			break;
		case R.id.btn_set_dest2:
			ed.putString("DEST_STOP_2",info.getStop_id()); 
			ed.putString("DEST_STOP_NAME_2", info.getStop_name());
			Toast.makeText(this, r.getString(R.string.bus_dest2) + r.getString(R.string.isSet), Toast.LENGTH_SHORT).show();
			break;
		}
		
		ed.commit();
		
	}		
}

