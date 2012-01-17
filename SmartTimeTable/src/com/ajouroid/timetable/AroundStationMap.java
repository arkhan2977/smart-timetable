package com.ajouroid.timetable;

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Toast;


public class AroundStationMap extends MapActivity {

	private List<Overlay> mapOverlays;
	private Drawable drawable;
	private Drawable marker;
	private AroundStation around_station;
	private MapView mapView;
	private MapController mc;

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stationsetting);
		mapView = (MapView)findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setSatellite(true);

		mc = mapView.getController();
		//mc.animateTo(getPoint(current_lat, current_lon)); //현재위치로 설정.
		mc.setZoom(17);

		mapOverlays = mapView.getOverlays();
		drawable = this.getResources().getDrawable(R.drawable.icon_here);
		marker = this.getResources().getDrawable(R.drawable.icon_busstopmarker);
		around_station = new AroundStation(marker, this);

		OverlayItem overlayitem1 = 

				new OverlayItem(getPoint(37517180,127041268), "ㅋ","강남구청");

		OverlayItem overlayitem2 = 

				new OverlayItem(getPoint(37517180,127035000), "ㅋㅋ","Apod");


		around_station.addOverlay(overlayitem1);
		around_station.addOverlay(overlayitem2);
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
			(mContext, item.getSnippet(), Toast.LENGTH_LONG).show();
			return true;
		}

	}

}



