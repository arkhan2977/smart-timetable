package com.ajouroid.timetable.bus;
import java.util.ArrayList;
import java.util.List;

import com.ajouroid.timetable.R;
import com.ajouroid.timetable.R.drawable;
import com.ajouroid.timetable.R.id;
import com.ajouroid.timetable.R.layout;
import com.ajouroid.timetable.R.string;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import android.widget.TextView;

public class StationInfoAlert extends MapActivity implements OnClickListener {
	static final int PROGRESS_DIALOG = 0;
	Button btn_set_start;
	Button btn_set_dest;	
	TextView sia_station_name;
	TextView sia_station_num;
	ImageButton btn_close;
	
	SharedPreferences sPref;
	BusStopInfo info;
	Bundle extra;
	Intent favorite_intent;
	private MapView mapView = null;
	private MapController mc;
	private List<Overlay> mapOverlays;
	private Drawable marker;
	private AroundStation around_station;
	double station_lat = 37.2824052;
	double station_lng = 127.0453329; //기본값 아주대학교
	private String tempSP;
	private String tempDEST;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		setContentView(R.layout.station_info_alert);	
		btn_set_start = (Button) findViewById(R.id.btn_set_start);
		btn_set_dest = (Button) findViewById(R.id.btn_set_dest);
		//btn_set_start2 = (Button) findViewById(R.id.btn_set_start2);
		//btn_set_dest2 = (Button) findViewById(R.id.btn_set_dest2);
		sia_station_name = (TextView) findViewById(R.id.sia_station_name);
		sia_station_num = (TextView) findViewById(R.id.sia_station_num);
		btn_close = (ImageButton) findViewById(R.id.close_img_button);
		
		Intent i = getIntent();
		extra = new Bundle();
		favorite_intent = new Intent();
		
		DBAdapterBus dbA = new DBAdapterBus(this);
		dbA.open();
		
		info = dbA.getBusStopInfoById(i.getStringExtra("id"));
				
		dbA.close();
		
		station_lat = info.getLatitude();
		station_lng = info.getLongitude();
		
		sia_station_name.setText(info.getStop_name());
		sia_station_num.setText(info.getNumber() + "");
		
		btn_set_start.setOnClickListener(this);
		btn_set_dest.setOnClickListener(this);
		btn_close.setOnClickListener(this);
		
		setMap();
		
	}

	@Override
	protected void onResume() {
		sPref = PreferenceManager.getDefaultSharedPreferences(this);
		checkSelected();
		super.onResume();
	}
	
	private void checkSelected()
	{
		String id_start1 = sPref.getString("START_STOP", "-1");
		String id_dest1 = sPref.getString("DEST_STOP", "-1");
		
		if (id_start1.compareTo(info.getStop_id()) == 0)
		{
			btn_set_start.setTextColor(Color.RED);
		}
		else btn_set_start.setTextColor(Color.BLACK);
			
		if (id_dest1.compareTo(info.getStop_id()) == 0)
		{
			btn_set_dest.setTextColor(Color.RED);
		}
		else btn_set_dest.setTextColor(Color.BLACK);
		
		
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void setMap(){

		Log.d("MiniMapActivity"," setting map");
		mapView = (MapView)findViewById(R.id.mapview_alert);
		mapView.setBuiltInZoomControls(true);
		mapView.setSatellite(false);

		mc = mapView.getController();
		mc.animateTo(getPoint(station_lat, station_lng)); //현재위치로 설정.
		mc.setZoom(18);

		mapOverlays = mapView.getOverlays();
		marker = this.getResources().getDrawable(R.drawable.icon_busstopmarker);
		around_station = new AroundStation(marker, this);

		OverlayItem overlayitem = 
				new OverlayItem(getPoint(station_lat, station_lng), info.getStop_name(), info.getNumber() + "");

		around_station.addOverlay(overlayitem);
		mapOverlays.add(around_station);		
	}
	
	private GeoPoint getPoint(double lat, double lon) {
		return (new GeoPoint((int) (lat * 1000000.0), (int) (lon * 1000000.0)));
	}
	
	private class AroundStation extends ItemizedOverlay<OverlayItem> {

		private Context mContext;
		private ArrayList<OverlayItem> mOverlays 
		= new ArrayList<OverlayItem>();

		public AroundStation(Drawable defaultMarker, Context context) {
			super(boundCenterBottom(defaultMarker));
			// TODO Auto-generated constructor stub
			mContext = context;
		}

		@Override
		protected OverlayItem createItem(int i) {
			// TODO Auto-generated method stub
			return mOverlays.get(i);
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return mOverlays.size();
		}

		public void addOverlay(OverlayItem overlay)
		{
			mOverlays.add(overlay);
			populate();
		}		

		@Override
		protected boolean onTap(int index) {
			OverlayItem item = mOverlays.get(index);
			Toast.makeText
			(mContext, item.getSnippet() + ", " + item.getTitle(), Toast.LENGTH_SHORT).show();
			return true;
		}

	}

	public void onClick(View v) {

		SharedPreferences.Editor ed = sPref.edit();
		Resources r = getResources();
		
		switch(v.getId())
		{
		case R.id.btn_set_start:
			ed.putString("START_STOP",info.getStop_id());
			ed.putString("START_STOP_NAME", info.getStop_name());
			extra.putString("START_STOP_ID", info.getStop_id());
			extra.putString("START_STOP_NAME", info.getStop_name());
			tempSP = info.getStop_name();
			Log.d("sakdhas",tempSP);
			Toast.makeText(this, r.getString(R.string.bus_dest) + r.getString(R.string.rqSet), Toast.LENGTH_SHORT).show();
			break;
		case R.id.btn_set_dest:
			ed.putString("DEST_STOP",info.getStop_id()); 
			ed.putString("DEST_STOP_NAME", info.getStop_name());
			extra.putString("DEST_STOP_ID", info.getStop_id());
			extra.putString("DEST_STOP_NAME", info.getStop_name());
			tempDEST = info.getStop_name();
			
			//alert dialog 띄우기.
			if(tempSP == null){
				AlertDialog dlg = new AlertDialog.Builder(this)
				.setTitle(r.getString(R.string.error))		// title 지정
				.setMessage(r.getString(R.string.bus_start) + r.getString(R.string.isnotSet) + "\n" +
						r.getString(R.string.bus_start) + r.getString(R.string.rqSet))
				.setPositiveButton(r.getString(R.string.ok), new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
			}
			else{
				AlertDialog dlg = new AlertDialog.Builder(this)
				.setTitle(r.getString(R.string.currentRoute))		// title 지정
				.setMessage(tempSP + " ▶ " + tempDEST + "\n" + r.getString(R.string.rqConfirm))	
				.setPositiveButton(r.getString(R.string.ok), new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						favorite_intent.putExtras(extra);
						StationInfoAlert.this.setResult(RESULT_OK, favorite_intent); // 성공했다는 결과값을 보내면서 데이터 꾸러미를 지고 있는 intent를 함께 전달한다.
						StationInfoAlert.this.finish();//finish
					}
				}).setNegativeButton(r.getString(R.string.cancel), new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
			}			
			break;		
		case R.id.close_img_button:
			finish();
			break;
		}
		
		ed.commit();
		
		checkSelected();
	}		
}

