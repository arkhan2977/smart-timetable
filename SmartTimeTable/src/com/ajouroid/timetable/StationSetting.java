package com.ajouroid.timetable;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.ajouroid.timetable.RouteViewer.BusStopAdapter;
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


		tabHost = (TabHost)findViewById(R.id.tabhost);

		/* 에러방지용 임시 주석
		sp_stop_list = (ListView)findViewById(R.id.roh_sp_stop_list);
		dest_stop_list = (ListView)findViewById(R.id.roh_dest_stop_list);
		 */
		current_station_list = (ListView)findViewById(R.id.roh_current_stop_list);
		search_station_list = (ListView)findViewById(R.id.roh_search_station_list);
		et_stationNo = (EditText)findViewById(R.id.roh_input_search_station);

		et_busNumber = (EditText)findViewById(R.id.roh_input_search_bus);
		btn_search_bus = (Button)findViewById(R.id.roh_btn_search_bus);
		search_bus_List = (ListView)findViewById(R.id.roh_search_bus_list);

		/* 에러방지용 임시 주석
		myloc_sp = (TextView) findViewById(R.id.roh_my_location_sp);
		myloc_dest = (TextView) findViewById(R.id.roh_my_location_dest);
		 */
		myloc_current = (TextView) findViewById(R.id.roh_my_location_current); //주변정류장 내위치 표시

		btn_search_station = (Button) findViewById(R.id.roh_btn_search_station);

		myloc_current1 = (TextView) findViewById(R.id.roh_my_location_current1); //주변지도 내위치표시

		/* 에러방지용 임시 주석
		btn_setup_start = (Button)findViewById(R.id.setup_start);
		btn_setup_dest = (Button)findViewById(R.id.setup_dest);
		 */

		tabHost.setup();


		registTab("주변정류장", R.drawable.tab_myloc, R.id.tab_view1);		
		registTab("주변지도", R.drawable.tab_map, R.id.tab_view2);
		registTab("정류장검색", R.drawable.tab_search, R.id.tab_view3);
		registTab("버스검색", R.drawable.tab_search, R.id.tab_view4);
		//registTab("주변지도", R.drawable.tab_dest, R.id.tab_view2); //에러방지용 임시 주석
		//registTab("버스검색", R.drawable.tab_search, R.id.tab_view4); //에러방지용 임시 주석
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
			Toast.makeText(getApplicationContext(), R.string.loc_noExact, Toast.LENGTH_SHORT).show();
		}
	}

	boolean startEnable;
	boolean destEnable;
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		/*myloc_sp.setText(sPrefs.getString("START_ADDRESS", "출발지 미설정"));
		startEnable = sPrefs.contains("START_ADDRESS");
		myloc_sp.setSelected(true);
		SP_LAT = Double.parseDouble(sPrefs.getString("SP_LAT", "-1"));
		SP_LNG = Double.parseDouble(sPrefs.getString("SP_LNG", "-1"));	

		myloc_dest.setText(sPrefs.getString("DEST_ADDRESS", "도착지 미설정"));
		destEnable = sPrefs.contains("DEST_ADDRESS");
		myloc_dest.setSelected(true);
		DEST_LAT = Double.parseDouble(sPrefs.getString("DEST_LAT", "-1"));
		DEST_LNG = Double.parseDouble(sPrefs.getString("DEST_LNG", "-1"));*/

		//sp_stop_list.setOnItemClickListener(clickListener);
		//dest_stop_list.setOnItemClickListener(clickListener);	
		current_station_list.setOnItemClickListener(new CURR_ClickEvent());
		search_station_list.setOnItemClickListener(new SEARCH_ClickEvent());
		search_bus_List.setOnItemClickListener(new BUSLIST_ClickEvent());
		btn_search_station.setOnClickListener(this);
		btn_search_bus.setOnClickListener(this);


		//btn_setup_start.setOnClickListener(this);
		//btn_setup_dest.setOnClickListener(this);

		//registerForContextMenu(sp_stop_list);
		//registerForContextMenu(dest_stop_list);
		registerForContextMenu(current_station_list);
		registerForContextMenu(search_station_list);

		setAdapter();
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

			case R.id.cmenu_start_2:
				setStartStop_2(sp_stop_arrlist.get(info.position).getStop_id(),sp_stop_arrlist.get(info.position).getStop_name());
				break;
			case R.id.cmenu_end_2:
				setDestStop_2(sp_stop_arrlist.get(info.position).getStop_id(),sp_stop_arrlist.get(info.position).getStop_name());
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

			case R.id.cmenu_start_2:
				setStartStop_2(dest_stop_arrlist.get(info.position).getStop_id(),dest_stop_arrlist.get(info.position).getStop_name());
				break;
			case R.id.cmenu_end_2:
				setDestStop_2(dest_stop_arrlist.get(info.position).getStop_id(),dest_stop_arrlist.get(info.position).getStop_name());
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

			case R.id.cmenu_start_2:
				setStartStop_2(current_stop_arrlist.get(info.position).getStop_id(),current_stop_arrlist.get(info.position).getStop_name());
				break;
			case R.id.cmenu_end_2:
				setDestStop_2(current_stop_arrlist.get(info.position).getStop_id(),current_stop_arrlist.get(info.position).getStop_name());
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

			case R.id.cmenu_start_2:
				setStartStop_2(stopList.get(info.position).getStop_id(),stopList.get(info.position).getStop_name());
				break;
			case R.id.cmenu_end_2:
				setDestStop_2(stopList.get(info.position).getStop_id(),stopList.get(info.position).getStop_name());
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
		

		/*if(bGetteringGPS == false) {

			current_lat = location.getLatitude();
			current_lng = location.getLongitude();

			Log.d("StationSetting", "Current Location : " + current_lat + ", " + current_lng);			
			String myloc = null;
			try {
				// 위도,경도를 이용하여 현재 위치의 주소를 가져온다. 
				List<Address> addresses = null;
				addresses = geoCoder.getFromLocation(current_lat, current_lng, 1);
				myloc = addresses.get(0).getAddressLine(0).toString();

			} catch (IOException e) {
				myloc = current_lat + ", " + current_lng ;
			}
			finally{
				if (dbA.isOpen())
				{
					current_stop_arrlist = dbA.findNearStops(current_lat, current_lng);
					curr_adapter = new BusStopAdapter(current_stop_arrlist);
					current_station_list.setAdapter(curr_adapter);
					myloc_current.setText(myloc);
					myloc_current1.setText(myloc);
					myloc_current.setSelected(true);
					myloc_current1.setSelected(true);
					locManager.removeUpdates(this);	
					bGetteringGPS = true;
					setMap();
					Log.d("StationSetting"," call setMap()");
				}
			}
		}*/
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
			dialog.dismiss();				
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(StationSetting.this);
			dialog.setTitle("현재위치 탐색중...");
			dialog.setMessage("현재위치를 탐색중입니다. GPS나 3g 신호가 약할 경우 수신이 되지 않을 수 있습니다.");
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			dialog.show();
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
							current_stop_arrlist.get(i).getNumber()+"");

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
			Toast.makeText
			(mContext, item.getSnippet() + ", " + item.getTitle(), Toast.LENGTH_SHORT).show();
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




	//	BusStopAdapter sp_adapter;
	//	BusStopAdapter dest_adapter;
	BusStopAdapter curr_adapter;
	BusStopAdapter find_adapter;

	public void setAdapter(){
		/*if(SP_LAT != -1 && SP_LNG != -1){
			sp_stop_arrlist = dbA.findNearStops(SP_LAT, SP_LNG);
			if (sp_stop_arrlist != null)
			{
			sp_adapter = new BusStopAdapter(sp_stop_arrlist);
			sp_stop_list.setAdapter(sp_adapter);
			}
		}
		else{
			Toast.makeText(StationSetting.this, "출발지가 설정되어있지 않습니다.", Toast.LENGTH_SHORT).show();
		}
		if(DEST_LAT != -1 && DEST_LNG != -1){
			dest_stop_arrlist = dbA.findNearStops(DEST_LAT, DEST_LNG);
			if (dest_stop_arrlist != null)
			{
				dest_adapter = new BusStopAdapter(dest_stop_arrlist);
				dest_stop_list.setAdapter(dest_adapter);
			}
		}
		else{
			Toast.makeText(StationSetting.this, "도착지가 설정되어있지 않습니다.", Toast.LENGTH_SHORT).show();
		}*/
	}

	public void updateAdapter()
	{
		/*if (sp_adapter != null)
		{
			sp_adapter.notifyDataSetChanged();
		}
		if (dest_adapter != null)
		{
			dest_adapter.notifyDataSetChanged();
		}*/
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

			String spId = sPrefs.getString("START_STOP", "");
			String destId = sPrefs.getString("DEST_STOP", "");

			String spId_2 = sPrefs.getString("START_STOP_2", "");
			String destId_2 = sPrefs.getString("DEST_STOP_2", "");

			TextView state = (TextView)row.findViewById(R.id.stop_state);

			String curId = arrlist.get(position).getStop_id();
			if (spId.compareTo(curId) == 0)
			{
				state.setText(R.string.bus_st1);
				stop_name.setTextColor(0xFFDAA520);
				state.setVisibility(View.VISIBLE);
			}
			else if (destId.compareTo(curId) == 0){
				state.setText(R.string.bus_ds1);
				stop_name.setTextColor(0xFFDAA520);
				state.setVisibility(View.VISIBLE);
			}

			else if (spId_2.compareTo(curId) == 0)
			{
				state.setText(R.string.bus_st2);
				stop_name.setTextColor(0xFFFF0000);
				state.setVisibility(View.VISIBLE);
			}
			else if(destId_2.compareTo(curId) == 0)
			{
				state.setText(R.string.bus_ds2);
				stop_name.setTextColor(0xFFFF0000);
				state.setVisibility(View.VISIBLE);
			}
			else
			{
				state.setVisibility(View.INVISIBLE);
			}

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

	/*class SP_ClickEvent implements ListView.OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View v, int position,
				long arg3) {

			setStartStop(sp_stop_arrlist.get(position).getStop_id(),sp_stop_arrlist.get(position).getStop_name());
			//v.showContextMenu();

		}

	}
	class DEST_ClickEvent implements ListView.OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View v, int position,
				long arg3) {
			setDestStop(dest_stop_arrlist.get(position).getStop_id(),dest_stop_arrlist.get(position).getStop_name());
			//v.showContextMenu();		
		}

	}*/
	class CURR_ClickEvent implements ListView.OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View v, int position,
				long arg3) {

			//v.showContextMenu();
			
			Intent i = new Intent(StationSetting.this, StationInfoAlert.class);
			BusStopInfo info = current_stop_arrlist.get(position);

			i.putExtra("id", info.getStop_id());
			
			startActivity(i);
		}
	}
	
	class SEARCH_ClickEvent implements ListView.OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Intent i = new Intent(StationSetting.this, StationInfoAlert.class);
			BusStopInfo info = stopList.get(arg2);
			
			i.putExtra("id", info.getStop_id());
			
			startActivity(i);
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
			startActivity(i);		
		}

	}


	public void setStartStop(String id,String name)
	{
		SharedPreferences.Editor ed = sPrefs.edit();
		ed.putString("START_STOP",id); 
		ed.putString("START_STOP_NAME", name);
		Toast.makeText(this, r.getString(R.string.bus_start1) + r.getString(R.string.isSet), Toast.LENGTH_SHORT).show();
		ed.commit();
		updateAdapter();
	}

	public void setDestStop(String id,String name)
	{
		SharedPreferences.Editor ed = sPrefs.edit();
		ed.putString("DEST_STOP",id); 
		ed.putString("DEST_STOP_NAME", name);
		Toast.makeText(this, r.getString(R.string.bus_dest1) + r.getString(R.string.isSet), Toast.LENGTH_SHORT).show();
		ed.commit();	
		updateAdapter();
	}

	public void setStartStop_2(String id,String name)
	{
		SharedPreferences.Editor ed = sPrefs.edit();
		ed.putString("START_STOP_2",id); 
		ed.putString("START_STOP_NAME_2", name);
		Toast.makeText(this, r.getString(R.string.bus_start2) + r.getString(R.string.isSet), Toast.LENGTH_SHORT).show();
		ed.commit();
		updateAdapter();
	}

	public void setDestStop_2(String id,String name)
	{
		SharedPreferences.Editor ed = sPrefs.edit();
		ed.putString("DEST_STOP_2",id); 
		ed.putString("DEST_STOP_NAME_2", name);
		Toast.makeText(this, r.getString(R.string.bus_dest2) + r.getString(R.string.isSet), Toast.LENGTH_SHORT).show();
		ed.commit();	
		updateAdapter();
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
}

