package com.ajouroid.timetable.bus;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.ajouroid.timetable.Keyring;
import com.ajouroid.timetable.R;
import com.ajouroid.timetable.R.id;
import com.ajouroid.timetable.R.layout;
import com.ajouroid.timetable.R.menu;
import com.ajouroid.timetable.R.string;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class RouteViewer extends Activity {
	private static final int From_StationSetting = 0;
	Bundle extra;
	Intent favorite_intent;
	TextView upfirst_time;
	TextView uplast_time;
	TextView downfirst_time;
	TextView downlast_time;
	TextView bus_term_peek;
	TextView bus_term_npeek;
	TextView bus_region;
	TextView bus_name;
	ListView stationList;
	ArrayList<BusStopInfo> stationArr;
	DBAdapterBus dbA;
	
	ArrayList<String> locationList;
	 
	SharedPreferences sPrefs;
	
	Resources r;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.routeviewer);
		stationList = (ListView)findViewById(R.id.findstation_list);
		
		upfirst_time = (TextView)findViewById(R.id.findbus_upfirst);
		uplast_time = (TextView)findViewById(R.id.findbus_uplast);
		downfirst_time = (TextView)findViewById(R.id.findbus_downfirst);
		downlast_time = (TextView)findViewById(R.id.findbus_downlast);
		bus_term_peek = (TextView)findViewById(R.id.findbus_term_peek);
		bus_term_npeek = (TextView)findViewById(R.id.findbus_term_npeek);
		bus_region = (TextView)findViewById(R.id.findbus_region);
		bus_name = (TextView)findViewById(R.id.findbus_name);
		
		r = getResources();
		registerForContextMenu(stationList);
		
		locationList = new ArrayList<String>();
		
		dbA = new DBAdapterBus(this);
		extra = new Bundle();
		favorite_intent = new Intent();
		sPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		String busId = getIntent().getStringExtra("id");
		
		FindRouteTask task = new FindRouteTask();
		task.execute(busId);
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		stationList.setOnItemClickListener(new List_ClickEvent());		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.stopsettiongmenu, menu);		
		
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId())
		{
		case R.id.cmenu_start:
			setStartStop(stationArr.get(info.position).getStop_id(),stationArr.get(info.position).getStop_name());
			break;
		case R.id.cmenu_end:
			setDestStop(stationArr.get(info.position).getStop_id(),stationArr.get(info.position).getStop_name());
			break;
		case R.id.cmenu_start_2:
			setStartStop_2(stationArr.get(info.position).getStop_id(),stationArr.get(info.position).getStop_name());
			break;
		case R.id.cmenu_end_2:
			setDestStop_2(stationArr.get(info.position).getStop_id(),stationArr.get(info.position).getStop_name());
			break;
		}

		adapter.notifyDataSetChanged();
		
		return super.onContextItemSelected(item);
	}
	BusStopAdapter adapter;
	
	class List_ClickEvent implements ListView.OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View v, int position,
				long arg3) {

			//v.showContextMenu();
			
			Intent i = new Intent(RouteViewer.this, StationInfoAlert.class);
			BusStopInfo info = stationArr.get(position);
			
			i.putExtra("id", info.getStop_id());
			
			startActivityForResult(i,From_StationSetting);

		}
	}	
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode){
		case From_StationSetting: // requestCode가 B_ACTIVITY인 케이스
			if(resultCode == RESULT_OK){ //B_ACTIVITY에서 넘겨진 resultCode가 OK일때만 실행
				extra.putAll(data.getExtras());
				favorite_intent.putExtras(extra);
				this.setResult(RESULT_OK, favorite_intent); // 성공했다는 결과값을 보내면서 데이터 꾸러미를 지고 있는 intent를 함께 전달한다.
				this.finish();
			}
		}
	}

	public void setStartStop(String id,String name)
	{
		SharedPreferences.Editor ed = sPrefs.edit();
		ed.putString("START_STOP",id); 
		ed.putString("START_STOP_NAME", name);
		Toast.makeText(this, r.getString(R.string.bus_start1) + r.getString(R.string.isSet), Toast.LENGTH_SHORT).show();
		ed.commit();
	}
	
	public void setDestStop(String id,String name)
	{
		SharedPreferences.Editor ed = sPrefs.edit();
		ed.putString("DEST_STOP",id); 
		ed.putString("DEST_STOP_NAME", name);
		Toast.makeText(this, r.getString(R.string.bus_dest1) + r.getString(R.string.isSet), Toast.LENGTH_SHORT).show();
		ed.commit();
	}
	
	public void setStartStop_2(String id,String name)
	{
		SharedPreferences.Editor ed = sPrefs.edit();
		ed.putString("START_STOP_2",id); 
		ed.putString("START_STOP_NAME_2", name);
		Toast.makeText(this, r.getString(R.string.bus_start2) + r.getString(R.string.isSet), Toast.LENGTH_SHORT).show();
		ed.commit();
	}
	
	public void setDestStop_2(String id,String name)
	{
		SharedPreferences.Editor ed = sPrefs.edit();
		ed.putString("DEST_STOP_2",id); 
		ed.putString("DEST_STOP_NAME_2", name);
		Toast.makeText(this, r.getString(R.string.bus_dest2) + r.getString(R.string.isSet), Toast.LENGTH_SHORT).show();
		ed.commit();
	}
	
	class FindRouteTask extends AsyncTask<String, Void, Void>
	{
		ProgressDialog dialog;
		
		BusInfo info;
		
		int statusCode;
		
		String url = "http://openapi.gbis.go.kr/ws/rest/buslocationservice";
		
		@Override
		protected void onPostExecute(Void result) {
			dbA.close();
			dialog.dismiss();

			adapter = new BusStopAdapter();
			stationList.setAdapter(adapter);
			
			bus_name.setText(info.getBus_number());
			bus_region.setText(info.getRegion());
			upfirst_time.setText(info.getUpFirstTime());
			uplast_time.setText(info.getUpLastTime());
			downfirst_time.setText(info.getDownFirstTime());
			downlast_time.setText(info.getDownLastTime());
			bus_term_peek.setText(info.getPeek_term());
			bus_term_npeek.setText(info.getNpeek_term());
			
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			dbA.open();
			dialog = new ProgressDialog(RouteViewer.this);
			dialog.setTitle(R.string.bus_findingRoute);
			dialog.setMessage(r.getString(R.string.bus_findingRouteMsg));
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			dialog.show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(String... params) {
			stationArr = dbA.getBusStopOfBus(params[0]);
			info = dbA.getBusInfo(params[0]);
			
			try {
				String key = URLEncoder.encode(Keyring.BUS_KEY, "UTF-8");
				// key = URLEncoder.encode(KEY, "UTF-8");
				XmlPullParserFactory baseparser = XmlPullParserFactory
						.newInstance();
				baseparser.setNamespaceAware(true);
				XmlPullParser xpp = baseparser.newPullParser();

				String urlStr = url + "?serviceKey=" + key + "&routeId=" + params[0];

				Log.d("SmartTimeTable", "Requesting Bus Location Information...");
				Log.d("SmartTimeTable", "URL: " + urlStr);
				URL requestURL = new URL(urlStr);
				InputStream input = requestURL.openStream();
				xpp.setInput(input, "UTF-8");

				int parserEvent = xpp.getEventType();
				parserEvent = xpp.next();// 파싱한 자료에서 다음 라인으로 이동
				boolean check = true;

				locationList = new ArrayList<String>();

				while (parserEvent != XmlPullParser.END_DOCUMENT) {
					if (!check) {
						break;
					}

					switch (parserEvent) {
					case XmlPullParser.END_TAG: 
						break;
					case XmlPullParser.START_TAG: 
						if (xpp.getName().compareToIgnoreCase("returnCode") == 0) 
						{
							xpp.next();
							String tempCode = xpp.getText();
							statusCode = Integer.parseInt(tempCode);
							xpp.next();
							check = false;
						} else if (xpp.getName().compareToIgnoreCase(
								"resultCode") == 0) // <returnCode> 인 경우.
						{
							xpp.next();
							String tempCode = xpp.getText();
							statusCode = Integer.parseInt(tempCode);
							xpp.next();
							
						} else if (xpp.getName().compareTo("msgBody") == 0) {
							parserEvent = xpp.next();
							while (true) {
								if (parserEvent == XmlPullParser.START_TAG) {
									String tag = xpp.getName();
									if (tag.compareTo("stationId") == 0) {
										xpp.next();
										locationList.add(xpp.getText());
									}
								} else if (parserEvent == XmlPullParser.END_TAG) {
									if (xpp.getName().compareTo(
											"msgBody") == 0) {
										break;
									}
								}

								parserEvent = xpp.next();
							}
						}
					}
					parserEvent = xpp.next(); // 다음 태그를 읽어 들입니다.
				}
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				Log.d("sibal", "xml exception");
			} catch (IOException e) {
				Log.d("sibal", "io exception");
			}
			return null;
		}
		
	}

	class BusStopAdapter extends ArrayAdapter<BusStopInfo>{
		
		BusStopAdapter(){
			super(RouteViewer.this,R.layout.route_row,R.id.route_name, stationArr);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){

			LayoutInflater inflater = getLayoutInflater();
			
			View row = inflater.inflate(R.layout.route_row, parent, false);
			

			TextView stop_updown = (TextView)row.findViewById(R.id.route_updown);		
			String updown = stationArr.get(position).getUpdown();
			if (updown.compareTo("정") == 0)
				updown = r.getString(R.string.bus_up);
			else
				updown = r.getString(R.string.bus_down); 
			stop_updown.setText(updown);
			
			TextView stop_name = (TextView)row.findViewById(R.id.route_name);
			stop_name.setText(stationArr.get(position).getStop_name());
			
			
			String id = stationArr.get(position).getStop_id();
			if (locationList.contains(id))
			{
				TextView current = (TextView)row.findViewById(R.id.route_current);
				current.setVisibility(View.VISIBLE);
			}
			
			String spId = sPrefs.getString("START_STOP", "");
			String destId = sPrefs.getString("DEST_STOP", "");
			
			String spId_2 = sPrefs.getString("START_STOP_2", "");
			String destId_2 = sPrefs.getString("DEST_STOP_2", "");
			
			TextView state = (TextView)row.findViewById(R.id.route_state);
			
			String curId = stationArr.get(position).getStop_id();
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

			
			return row;

		}
	}
}
