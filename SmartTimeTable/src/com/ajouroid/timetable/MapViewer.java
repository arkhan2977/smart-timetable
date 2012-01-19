package com.ajouroid.timetable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MapViewer extends MapActivity implements LocationListener {

	private static final int MENU_SET = Menu.FIRST+1;
	private static final int MENU_SEARCH = Menu.FIRST+2;
	LocationManager location = null;
	boolean bGetteringGPS = false; //GPS 제어
	private LocationManager locationManager = null;

	private MapView mapView = null;
	private MapController mapController;
	private GeoPoint centerGP = null;
	private GeoPoint point = null;	

	private double currentLat = 0;
	private double currentLng = 0;


	private Context context = null;
	//private EditText editAddress = null;
	List<Address> addr;	
	AlertDialog.Builder alert;
	private String address;
	private SharedPreferences mPrefs;
	private int SEARCH_TYPE;  //출발지 검색인지 도착지 검색인지 여부 확인 

	Resources r;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.context = this;

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Iterator<String> providers = locationManager.getAllProviders().iterator();
		

		// GPS 정보를 얻기위한 프로바이더 검색
		while(providers.hasNext()) {
			Log.d("MapViewer", "provider : " + providers.next());
		}

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.NO_REQUIREMENT);
		criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

		String best = locationManager.getBestProvider(criteria, true);
		if(best != null){
			locationManager.requestLocationUpdates(best, 0, 0, this);	
		}
		else{
			Toast toast = Toast.makeText(this, r.getString(R.string.err_cannotFindLocation), Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.TOP, 0, 50 );
			toast.show();
		}
		setContentView(R.layout.map);
		
	}	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Intent intent = getIntent();
		SEARCH_TYPE = intent.getIntExtra("TYPE", 0);
		
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	
		mapView = (MapView) findViewById(R.id.mapview);
		mapController = mapView.getController();		
		
	}

	//메뉴 구현부
	public boolean onCreateOptionsMenu(Menu menu){
		if(SEARCH_TYPE == 0){
			menu.add(0, MENU_SET, 0, R.string.loc_setStart).setIcon(R.drawable.icon_loc_check);
			menu.add(0, MENU_SEARCH, 0, R.string.loc_searchStart).setIcon(R.drawable.icon_loc_search);
		}
		else{
			menu.add(0, MENU_SET, 0, R.string.loc_setDest).setIcon(R.drawable.icon_loc_check);
			menu.add(0, MENU_SEARCH, 0, R.string.loc_searchDest).setIcon(R.drawable.icon_loc_search);
		}
		return(super.onCreateOptionsMenu(menu));
	}

	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case MENU_SET:
			menu_set();
			return(true);

		case MENU_SEARCH:
			menu_search();
			return(true);
		}

		return(super.onOptionsItemSelected(item));
	}
	//설정 선택시 시작----
	private void menu_set(){	
		
		if(SEARCH_TYPE == 0){
			if(addr == null){
				Toast.makeText(getApplicationContext(), "출발지를 먼저 클릭하셔야합니다!", Toast.LENGTH_SHORT).show();
			}
			else{
				address = addr.get(0).getAddressLine(0).toString();			
				
				alert = new AlertDialog.Builder(MapViewer.this);
				alert.setTitle( "출발지 설정" )
				.setMessage( address + "\n 현 위치를 출발지로 설정 하시겠습니까?" )
				.setPositiveButton( "확인", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						String sp_lat = Double.toString(currentLat);
						String sp_lng = Double.toString(currentLng);
						SharedPreferences.Editor ed = mPrefs.edit();
						ed.putString("START_ADDRESS", address);
						ed.putString("SP_LAT", sp_lat);
						ed.putString("SP_LNG", sp_lng);
						ed.commit();
						finish();
					}
				}).setNegativeButton("취소", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				})
				.show();
			}
		}
		else{
			if(addr == null){
				Toast.makeText(getApplicationContext(), "도착지를 먼저 클릭하셔야합니다!", Toast.LENGTH_SHORT).show();
			}
			else{
				address = addr.get(0).getAddressLine(0).toString();
				
				alert = new AlertDialog.Builder(MapViewer.this);
				alert.setTitle( "도착지 설정" )
				.setMessage( address + "\n 현 위치를 도착지로 설정 하시겠습니까?" )
				.setPositiveButton( "확인", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						String dest_lat = Double.toString(currentLat);
						String dest_lng = Double.toString(currentLng);
						SharedPreferences.Editor ed = mPrefs.edit();
						ed.putString("DEST_ADDRESS", address);
						ed.putString("DEST_LAT", dest_lat);
						ed.putString("DEST_LNG", dest_lng);
						ed.commit();
						finish();
					}
				}).setNegativeButton("취소", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				})
				.show();
			}
		}
	}
	//---------------끝.
	//출발지 검색 시작 ----
	private void menu_search(){		
		
		Intent i = new Intent(this,SearchAddr.class); // SearchAddr 액티비티를 다이얼로그로 불러옴.
		startActivityForResult(i, 1);
		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);

		if(resultCode==RESULT_OK) // 액티비티가 정상적으로 종료되었을 경우
		{
			if(requestCode==1) // InformationInput에서 호출한 경우에만 처리합니다.
			{				
				Geocoder geocoder = new Geocoder(context);
				mapView.getOverlays().remove(0);
				currentLat = Double.parseDouble(data.getStringExtra("LAT"));
				currentLng = Double.parseDouble(data.getStringExtra("LON"));
				
				try {
					addr = geocoder.getFromLocation(currentLat, currentLng, 1);
					point = new GeoPoint((int)(currentLat * 1E6),(int)(currentLng * 1E6));
					placeMarker((int)(currentLat * 1E6), (int)(currentLng * 1E6));
					mapController.animateTo(point);
					mapController.setZoom(15);									

				} catch (IOException e) {
					// TODO Auto-generated catch block
					Toast.makeText(getApplicationContext(), "유효하지 않은 주소입니다.", Toast.LENGTH_SHORT).show();
				}

				
			}
		}
	}	
	//---------------끝.


	//지도 노출 
	private void setMap() {

		centerGP = new GeoPoint((int) (currentLat * 1E6),(int) (currentLng * 1E6));

		mapView.setBuiltInZoomControls(true);
		mapView.setClickable(true);
		mapController.setZoom(15);
		mapController.setCenter(centerGP);
		mapView.invalidate();

		placeMarker((int)(currentLat * 1E6), (int)(currentLng * 1E6));

		if(SEARCH_TYPE == 0){
			Toast toast = Toast.makeText(getApplicationContext(), "출발지를 지도에서 찾아 클릭하세요.", Toast.LENGTH_SHORT);
			toast.show();
		}
		else{
			Toast toast = Toast.makeText(getApplicationContext(), "도착지를 지도에서 찾아 클릭하세요.", Toast.LENGTH_SHORT);
			toast.show();
		}

	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	//위치 변경시 location 다시 잡아주는 부분.
	public void onLocationChanged(Location location) {

		if(bGetteringGPS == false) {

			currentLat = location.getLatitude();
			currentLng = location.getLongitude();

			Log.d("MapViewer", "Current Location : " + currentLat + " " + currentLng);
			locationManager.removeUpdates(this);			
			bGetteringGPS = true;

			setMap();

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
	
	//마커를 지도에 위치시키는 부분
	private void placeMarker(int markerLatitude, int markerLongitude)
	{
		Drawable marker = getResources().getDrawable(R.drawable.icon_here);

		//Drawable marker=getResources().getDrawable(R.drawable.marker);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(),marker.getIntrinsicHeight());

		mapView.getOverlays().add(new InterestingLocations(marker,markerLatitude, markerLongitude));
	}
	
	//설정된 좌표를 센터로 이동시키는 부분
	private void CenterLocation(GeoPoint centerGeoPoint)
	{
		mapController.animateTo(centerGeoPoint);

		placeMarker(centerGeoPoint.getLatitudeE6(), centerGeoPoint.getLongitudeE6());

		currentLat = (double)centerGeoPoint.getLatitudeE6()/1000000;
		currentLng = (double)centerGeoPoint.getLongitudeE6()/1000000;		

	};


	//지도 위에 레이어올리는 부분 (오버레이)
	class InterestingLocations extends ItemizedOverlay<OverlayItem>{

		private List<OverlayItem> locations = new ArrayList<OverlayItem>();
		private Drawable marker;
		private OverlayItem myOverlayItem;

		public InterestingLocations(Drawable defaultMarker, 
				int LatitudeE6, int LongitudeE6) {
			super(defaultMarker);

			// TODO Auto-generated constructor stub
			this.marker=defaultMarker;
			// create locations of interest
			GeoPoint myPlace = new GeoPoint(LatitudeE6,LongitudeE6);
			myOverlayItem = new OverlayItem(myPlace, "My Place", "My Place");
			locations.add(myOverlayItem);

			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			// TODO Auto-generated method stub
			return locations.get(i);
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return locations.size();
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, 
				boolean shadow) {
			// TODO Auto-generated method stub
			super.draw(canvas, mapView, shadow);

			boundCenterBottom(marker);
		}

		@Override
		//지도 클릭시 이벤트
		public boolean onTap(GeoPoint p, MapView mapView) {
			// TODO Auto-generated method stub

			Geocoder geocoder = new Geocoder(context);
			mapView.getOverlays().remove(0);

			CenterLocation(p);
			try {

				addr = geocoder.getFromLocation(currentLat, currentLng, 1);
			//	calc_distance();
				Toast toast = Toast.makeText(getApplicationContext(), addr.get(0).getAddressLine(0).toString() 
						+ "\n위도 : " + currentLat + ", 경도 : " + currentLng, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.TOP, 0, 50 );

				toast.show();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(getApplicationContext(), "유효하지 않은 주소입니다.", Toast.LENGTH_SHORT).show();
			}			
			return true;
		}			
	}
	
}