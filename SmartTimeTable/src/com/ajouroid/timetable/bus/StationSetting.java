package com.ajouroid.timetable.bus;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.ajouroid.timetable.DBAdapter;
import com.ajouroid.timetable.R;
import com.ajouroid.timetable.R.drawable;
import com.ajouroid.timetable.R.id;
import com.ajouroid.timetable.R.layout;
import com.ajouroid.timetable.R.menu;
import com.ajouroid.timetable.R.string;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class StationSetting extends MapActivity implements LocationListener, View.OnClickListener, OnTabChangeListener {

	private static final int From_StationSetting = 0;
	Bundle extra;
	Intent favorite_intent;
	
	ListView sp_stop_list;
	ListView dest_stop_list;
	ListView current_station_list;
	ListView search_station_list;	
	ListView search_bus_List;

	TextView myloc_sp;
	TextView myloc_dest;
	TextView myloc_current;
	TextView myloc_current1;
	EditText et_stationNo;
	EditText et_busNumber;
	Button btn_search_station;	
	Button btn_search_bus;
	Button btn_setup_start;
	Button btn_setup_dest;
	SharedPreferences sPrefs;
	
	TextView tv_start;
	TextView tv_dest;
 
	AlertDialog error_dialog;
	AlertDialog alert_dialog;
	ArrayList<BusStopInfo> sp_stop_arrlist;
	ArrayList<BusStopInfo> dest_stop_arrlist;
	ArrayList<BusStopInfo> current_stop_arrlist;	
	ArrayList<BusStopInfo> stopList;
	ArrayList<BusInfo> busList;
	
	
	Resources r;

	private LocationManager locManager;
	boolean bGetteringGPS = false;
	Geocoder geoCoder;
	double current_lat = 37.2824052;
	double current_lng = 127.0453329; //기본값 아주대학교

	/*Base info 관련*/

	DBAdapterBus dbA;
	Cursor cursor;


	double SP_LAT;
	double SP_LNG;
	double DEST_LAT;
	double DEST_LNG;
	boolean checkRunning = false;
	boolean downRunning = false;

	TabHost tabHost;

	private List<Overlay> mapOverlays;
	private Drawable drawable;
	private Drawable marker;
	private AroundStation around_station;
	private MapView mapView;
	private MapController mc;

	public void setLocation(){
		SetLocation setTask = new SetLocation();
		
		setTask.execute();
	}
	
	class SetLocation extends AsyncTask<Void, Void, Void>
	{
		
		String best = null;
		
		@Override
		protected void onPreExecute() {
			locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			
			Iterator<String> providers = locManager.getAllProviders().iterator();

			// GPS 정보를 얻기위한 프로바이더 검색
			while(providers.hasNext()) {
				Log.d("StationSetting", "Provider : " + providers.next());
			}

			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.NO_REQUIREMENT);
			criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

			best = locManager.getBestProvider(criteria, true);

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (best == null){
				Toast toast = Toast.makeText(StationSetting.this, R.string.err_cannotFindLocation, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.TOP, 0, 50 );
				toast.show();		
			}			
			else
			{
				locManager.requestLocationUpdates(best, 0, 0, StationSetting.this);
				// 주소를 확인하기 위한 Geocoder KOREA 와 KOREAN 둘다 가능
				geoCoder = new Geocoder(StationSetting.this, Locale.KOREAN); 
			}
			super.onPostExecute(result);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		dbA = new DBAdapterBus(StationSetting.this);
		dbA.open();	
		

		setContentView(R.layout.stationsetting);
		
		extra = new Bundle();
		favorite_intent = new Intent();



		r = getResources();
		tabHost = (TabHost)findViewById(R.id.tabhost);

		current_station_list = (ListView)findViewById(R.id.roh_current_stop_list);
		search_station_list = (ListView)findViewById(R.id.roh_search_station_list);
		et_stationNo = (EditText)findViewById(R.id.roh_input_search_station);

		et_busNumber = (EditText)findViewById(R.id.roh_input_search_bus);
		btn_search_bus = (Button)findViewById(R.id.roh_btn_search_bus);
		search_bus_List = (ListView)findViewById(R.id.roh_search_bus_list);

		myloc_current = (TextView) findViewById(R.id.roh_my_location_current); //주변정류장 내위치 표시

		btn_search_station = (Button) findViewById(R.id.roh_btn_search_station);

		myloc_current1 = (TextView) findViewById(R.id.roh_my_location_current1); //주변지도 내위치표시
		
		tv_start = (TextView)findViewById(R.id.station_startValue);
		tv_dest = (TextView)findViewById(R.id.station_destValue);


		tabHost.setup();


		registTab("주변정류장", R.drawable.tab_myloc, R.id.tab_view1);		
		registTab("주변지도", R.drawable.tab_map, R.id.tab_view2);
		registTab("정류장검색", R.drawable.tab_search, R.id.tab_view3);
		registTab("버스검색", R.drawable.tab_search, R.id.tab_view4);

		tabHost.setOnTabChangedListener(this);

		tabHost.setCurrentTab(0);


		sPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		this.registerReceiver(receiver, new IntentFilter("com.ajouroid.timetable.DOWNLOAD_COMPLETE"));

		if (!sPrefs.getBoolean("db_complete", false)){
			VersionCheckTask down_task = new VersionCheckTask(StationSetting.this);
			down_task.execute();
		}
		
		setLocation();
		
		r = getResources();
	}
	
	public void registTab(String labelId, int drawableId, int id)
	{
		TabHost.TabSpec spec = tabHost.newTabSpec("tab" + labelId);

		View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, tabHost.getTabWidget(), false);

		TextView title = (TextView) tabIndicator.findViewById(R.id.roh_tab_title);
		title.setText(labelId);
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.roh_tab_icon);

		icon.setImageResource(drawableId);		
		spec.setIndicator(tabIndicator);
		spec.setContent(id);
		tabHost.addTab(spec);
	}

	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		if(tabId.compareToIgnoreCase("tab주변지도") == 0){
			Log.d("StationSetting", tabId + " find and map start");
			//setMap();
			
			Toast toast = Toast.makeText(getApplicationContext(), R.string.loc_noExact, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.BOTTOM, 0, 150 );
			toast.show();
		}
	}

	boolean startEnable;
	boolean destEnable;
	@Override
	protected void onResume() {
		
		super.onResume();
		
		current_station_list.setOnItemClickListener(new CURR_ClickEvent());
		search_station_list.setOnItemClickListener(new SEARCH_ClickEvent());
		search_bus_List.setOnItemClickListener(new BUSLIST_ClickEvent());
		btn_search_station.setOnClickListener(this);
		btn_search_bus.setOnClickListener(this);
		
		

		registerForContextMenu(current_station_list);
		registerForContextMenu(search_station_list);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(receiver);
		dbA.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.stationmenu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.stopsettiongmenu, menu);		

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
		case R.id.menu_station_update:
			VersionCheckTask ver_task = new VersionCheckTask(StationSetting.this);
			ver_task.execute();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		switch(tabHost.getCurrentTab())
		{
		case 0:
			switch(item.getItemId())
			{
			case R.id.cmenu_start:
				setStartStop(sp_stop_arrlist.get(info.position).getStop_id(),sp_stop_arrlist.get(info.position).getStop_name());
				break;
			case R.id.cmenu_end:
				setDestStop(sp_stop_arrlist.get(info.position).getStop_id(),sp_stop_arrlist.get(info.position).getStop_name());
				break;

			}
			break;

		case 1:
			switch(item.getItemId())
			{
			case R.id.cmenu_start:
				setStartStop(dest_stop_arrlist.get(info.position).getStop_id(),dest_stop_arrlist.get(info.position).getStop_name());
				break;
			case R.id.cmenu_end:
				setDestStop(dest_stop_arrlist.get(info.position).getStop_id(),dest_stop_arrlist.get(info.position).getStop_name());
				break;

			}
			break;			
		case 2:
			switch(item.getItemId())
			{
			case R.id.cmenu_start:
				setStartStop(current_stop_arrlist.get(info.position).getStop_id(),current_stop_arrlist.get(info.position).getStop_name());
				break;
			case R.id.cmenu_end:
				setDestStop(current_stop_arrlist.get(info.position).getStop_id(),current_stop_arrlist.get(info.position).getStop_name());
				break;

			}
			break;

		case 3:
			switch(item.getItemId())
			{
			case R.id.cmenu_start:

				setStartStop(stopList.get(info.position).getStop_id(),stopList.get(info.position).getStop_name());
				break;
			case R.id.cmenu_end:
				setDestStop(stopList.get(info.position).getStop_id(),stopList.get(info.position).getStop_name());
				break;

			}
			break;			
		}
		return super.onContextItemSelected(item);
	}
	public void onLocationChanged(final Location location) {
		// TODO Auto-generated method stub
		Log.d("StationSetting", "Location changed.");
		
		LocationChangeTask lctask = new LocationChangeTask();
		lctask.execute(location);
	}
	
	class LocationChangeTask extends AsyncTask<Location, Void, Void>
	{
		ProgressDialog dialog;
		String myloc = null;
		@Override
		protected void onPostExecute(Void result) {
			if (dbA.isOpen())
			{
				current_stop_arrlist = dbA.findNearStops(current_lat, current_lng);
				curr_adapter = new BusStopAdapter(current_stop_arrlist);
				current_station_list.setAdapter(curr_adapter);
				myloc_current.setText(myloc);
				myloc_current1.setText(myloc);
				myloc_current.setSelected(true);
				myloc_current1.setSelected(true);
				locManager.removeUpdates(StationSetting.this);	
				bGetteringGPS = true;
				setMap();
				Log.d("StationSetting"," call setMap()");
			}	
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Location... params) {
			if(bGetteringGPS == false) {

				current_lat = params[0].getLatitude();
				current_lng = params[0].getLongitude();

				Log.d("StationSetting", "Current Location : " + current_lat + ", " + current_lng);					
				try {
					// 위도,경도를 이용하여 현재 위치의 주소를 가져온다. 
					List<Address> addresses = null;
					addresses = geoCoder.getFromLocation(current_lat, current_lng, 1);
					myloc = addresses.get(0).getAddressLine(0).toString();

				} catch (IOException e) {
					myloc = current_lat + ", " + current_lng ;
				}
			}
			return null;
		}
		
	}

	public void setMap(){
		
		Log.d("StationSetting"," setting map");
		mapView = (MapView)findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setSatellite(false);

		mc = mapView.getController();
		mc.animateTo(getPoint(current_lat, current_lng)); //현재위치로 설정.
		mc.setZoom(16);

		mapOverlays = mapView.getOverlays();
		drawable = this.getResources().getDrawable(R.drawable.icon_here);
		marker = this.getResources().getDrawable(R.drawable.icon_busstopmarker);
		around_station = new AroundStation(marker, this);

		OverlayItem overlayitem = 
				new OverlayItem(getPoint(current_lat, current_lng), "내 위치", "내 현재위치");

		around_station.addOverlay(overlayitem,drawable);

		for(int i=0; i<current_stop_arrlist.size(); i++){
			OverlayItem overlayitem1 =
					new OverlayItem(getPoint(current_stop_arrlist.get(i).getLatitude(),
							current_stop_arrlist.get(i).getLongitude()),
							current_stop_arrlist.get(i).getStop_name(),
							current_stop_arrlist.get(i).getStop_id()+"");

			around_station.addOverlay(overlayitem1);
		}
		
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
		public void addOverlay(OverlayItem overlay, Drawable drawable)
		{
			if(drawable!=null)
				overlay.setMarker(boundCenterBottom(drawable));
			addOverlay(overlay);
		}

		@Override
		protected boolean onTap(int index) {
			OverlayItem item = mOverlays.get(index);
			
			//Toast.makeText(mContext, item.getSnippet() + ", " + item.getTitle(), Toast.LENGTH_SHORT).show();
			
			Intent i = new Intent(StationSetting.this, StationInfoAlert.class);
			i.putExtra("id", item.getSnippet());
			startActivityForResult(i,From_StationSetting); // 요기!!!!!!!!!!!!!
			return true;
		}

	}


	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}



	BusStopAdapter curr_adapter;
	BusStopAdapter find_adapter;

	public void updateAdapter()
	{
		if (curr_adapter != null)
		{
			curr_adapter.notifyDataSetChanged();
		}

		if (find_adapter != null)
		{
			find_adapter.notifyDataSetChanged();
		}
	}

	//DB가 완료되면 주변 정류장을 찾아 리스트를 뿌려줘야함.
	class BusStopAdapter extends ArrayAdapter<BusStopInfo>{
		ArrayList<BusStopInfo> arrlist;

		BusStopAdapter(ArrayList<BusStopInfo> _list){
			super(StationSetting.this,R.layout.busstop_row,R.id.stop_name, _list);
			arrlist = _list;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){

			LayoutInflater inflater = getLayoutInflater();

			View row = inflater.inflate(R.layout.busstop_row, parent, false);


			TextView stop_name = (TextView)row.findViewById(R.id.stop_name);		
			stop_name.setText(arrlist.get(position).getStop_name());
			

			TextView stop_num = (TextView)row.findViewById(R.id.stop_num);
			stop_num.setText(arrlist.get(position).getNumber() + "");

			TextView distance = (TextView)row.findViewById(R.id.distance);
			if (arrlist.get(position).getDistance() > -1)
			{
				distance.setText(arrlist.get(position).getDistance() + "m");
			}
			else
				distance.setText("");

			return row;

		}
	}

	class CURR_ClickEvent implements ListView.OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View v, int position,
				long arg3) {
			
			Intent i = new Intent(StationSetting.this, StationInfoAlert.class);
			BusStopInfo info = current_stop_arrlist.get(position);

			i.putExtra("id", info.getStop_id());
			
			startActivityForResult(i,From_StationSetting);// 요기!!!!!!!!!!!!!
		}
	}
	
	class SEARCH_ClickEvent implements ListView.OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Intent i = new Intent(StationSetting.this, StationInfoAlert.class);
			BusStopInfo info = stopList.get(arg2);
			
			i.putExtra("id", info.getStop_id());
			
			startActivityForResult(i,From_StationSetting);// 요기!!!!!!!!!!!!!
		}
	}
	class BUSLIST_ClickEvent implements ListView.OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View v, int position,
				long arg3) {
			BusInfo bus = busList.get(position);

			Intent i = new Intent(StationSetting.this, RouteViewer.class);
			i.putExtra("number", bus.getBus_number());
			i.putExtra("id", bus.getBus_id());
			i.putExtra("upfirst", bus.getUpFirstTime());
			i.putExtra("uplast", bus.getUpLastTime());
			i.putExtra("downfirst", bus.getDownFirstTime());
			i.putExtra("downlast", bus.getDownLastTime());
			i.putExtra("region", bus.getRegion());
			i.putExtra("term_peek", bus.getPeek_term());
			i.putExtra("term_npeek", bus.getNpeek_term());			
			startActivityForResult(i,From_StationSetting); // 요기!!!!!!!!!!!!!
		}

	}


	public void setStartStop(String id,String name)
	{
		start_id = id;
		start_name = name;
		tv_start.setText(name);
		checkSetting();
	}

	public void setDestStop(String id,String name)
	{
		dest_id = id;
		dest_name = name;
		tv_dest.setText(name);
		checkSetting();
	}

	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.roh_btn_search_station:

			String station = et_stationNo.getText().toString();

			FindStationTask findTask = new FindStationTask();

			findTask.execute(station);
			break;

		case R.id.roh_btn_search_bus:

			busList = dbA.getBusInfoByNumber(et_busNumber.getText().toString());
			search_bus_List.setAdapter(new FindBusAdapter());
			break;	
		}
	}

	class FindStationTask extends AsyncTask<String, Void, Void>
	{
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(StationSetting.this);
			dialog.setTitle(R.string.bus_findingStation);
			dialog.setMessage(r.getString(R.string.bus_findingStationMsg));
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			dialog.show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(String... params) {
			try {
				int number = Integer.parseInt(params[0]);
				stopList = dbA.getBusStopInfo(number);
			} catch (NumberFormatException e)
			{
				stopList = dbA.getBusStopInfo(params[0]);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			dialog.dismiss();
			find_adapter = new BusStopAdapter(stopList);
			search_station_list.setAdapter(find_adapter);
			super.onPostExecute(result);
		}
	}
	class FindBusAdapter extends ArrayAdapter<BusInfo>{

		FindBusAdapter(){
			super(StationSetting.this, R.layout.bus_row ,R.id.row_busnumber, busList);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){

			LayoutInflater inflater = getLayoutInflater();

			View row = inflater.inflate(R.layout.bus_row, parent, false);


			TextView number = (TextView)row.findViewById(R.id.row_busnumber);
			number.setText(busList.get(position).getBus_number());

			TextView region = (TextView)row.findViewById(R.id.row_region);
			region.setText(busList.get(position).getRegion());

			return row;

		}
	}
	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			updateAdapter();

			Log.d("SmartTimeTable", "DB Download Completed.");
		}
	};

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	String start_id;
	String start_name;
	String dest_id;
	String dest_name;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode){
		case From_StationSetting: // requestCode가 B_ACTIVITY인 케이스
			if(resultCode == RESULT_OK){ //B_ACTIVITY에서 넘겨진 resultCode가 OK일때만 실행
				if (data.hasExtra("start_id")) {
					start_name = data.getStringExtra("start_name");
					start_id = data.getStringExtra("start_id");
					tv_start.setText(start_name);
				}	
				
				if (data.hasExtra("dest_id")) {
					dest_name = data.getStringExtra("dest_name");
					dest_id = data.getStringExtra("dest_id");
					tv_dest.setText(dest_name);
				}
					
				checkSetting();

			}
		}
	}
	
	public void checkSetting()
 {
		if (start_id != null && dest_id != null) {
			AlertDialog dlg = new AlertDialog.Builder(this)
					.setTitle(r.getString(R.string.currentRoute))
					// title 지정
					.setMessage(
							start_name + " ▶ " + dest_name + "\n"
									+ r.getString(R.string.rqConfirm))
					.setPositiveButton(r.getString(R.string.ok),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									DBAdapter dbA = new DBAdapter(
											StationSetting.this);
									dbA.open();
									dbA.addFavoriteInfo(start_id, start_name,
											dest_id, dest_name);
									dbA.close();
									finish();
								}
							})
					.setNegativeButton(r.getString(R.string.cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).show();
		}
	}
	
	
}

