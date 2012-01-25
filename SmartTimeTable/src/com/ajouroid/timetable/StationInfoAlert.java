package com.ajouroid.timetable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import android.widget.TextView;

public class StationInfoAlert extends MapActivity {
	static final int PROGRESS_DIALOG = 0;
	Button btn_set_start1;
	Button btn_set_dest1;
	Button btn_set_start2;
	Button btn_set_dest2;
	TextView sia_station_name;
	TextView sia_station_num;
	private MapView mapView = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		setContentView(R.layout.station_info_alert);	
		btn_set_start1 = (Button) findViewById(R.id.btn_confirm);
		btn_set_dest1 = (Button) findViewById(R.id.btn_confirm);
		btn_set_start2 = (Button) findViewById(R.id.btn_confirm);
		btn_set_dest2 = (Button) findViewById(R.id.btn_confirm);
		sia_station_name = (TextView) findViewById(R.id.sia_station_name);
		sia_station_num = (TextView) findViewById(R.id.sia_station_num);
		
	
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();


	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}		
}

