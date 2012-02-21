package com.ajouroid.timetable.bus;

import com.ajouroid.timetable.MainActivity;
import com.ajouroid.timetable.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class AddFavorite extends Activity {
	
	ListView lv_start;
	ListView lv_dest;
	Button btn_addStation;
	Button btn_commit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		setContentView(R.layout.addfavorite);
		
		lv_start = (ListView)findViewById(R.id.fav_add_startList);
		lv_dest = (ListView)findViewById(R.id.fav_add_destList);
		btn_addStation = (Button)findViewById(R.id.fav_add_addStation);
		btn_commit = (Button)findViewById(R.id.fav_add_commitBtn);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		btn_addStation.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent i = new Intent(AddFavorite.this, StationSetting.class);
				startActivity(i);
			}
			
		});
		
		btn_commit.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();
			}
			
		});
	}

}
