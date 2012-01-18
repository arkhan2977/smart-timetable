package com.ajouroid.timetable;

import java.util.ArrayList;

import com.ajouroid.timetable.StationSetting.BUSLIST_ClickEvent;
import com.ajouroid.timetable.StationSetting.BusStopAdapter;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
	
	SharedPreferences sPrefs;
	
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
		
		registerForContextMenu(stationList);
		
		dbA = new DBAdapterBus(this);
		
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
		bus_name.setText(getIntent().getStringExtra("number"));
		bus_region.setText(getIntent().getStringExtra("region"));
		upfirst_time.setText(getIntent().getStringExtra("upfirst"));
		uplast_time.setText(getIntent().getStringExtra("uplast"));
		downfirst_time.setText(getIntent().getStringExtra("downfirst"));
		downlast_time.setText(getIntent().getStringExtra("downlast"));
		bus_term_peek.setText(getIntent().getStringExtra("term_peek"));
		bus_term_npeek.setText(getIntent().getStringExtra("term_npeek"));
		
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

			v.showContextMenu();

		}
	}	
	
	public void setStartStop(String id,String name)
	{
		SharedPreferences.Editor ed = sPrefs.edit();
		ed.putString("START_STOP",id); 
		ed.putString("START_STOP_NAME", name);
		Toast.makeText(this, "등교 출발 정류장이 설정되었습니다.", Toast.LENGTH_SHORT).show();
		ed.commit();
	}
	
	public void setDestStop(String id,String name)
	{
		SharedPreferences.Editor ed = sPrefs.edit();
		ed.putString("DEST_STOP",id); 
		ed.putString("DEST_STOP_NAME", name);
		Toast.makeText(this, "등교 도착 정류장이 설정되었습니다.", Toast.LENGTH_SHORT).show();
		ed.commit();
	}
	
	public void setStartStop_2(String id,String name)
	{
		SharedPreferences.Editor ed = sPrefs.edit();
		ed.putString("START_STOP_2",id); 
		ed.putString("START_STOP_NAME_2", name);
		Toast.makeText(this, "하교 출발 정류장이 설정되었습니다.", Toast.LENGTH_SHORT).show();
		ed.commit();
	}
	
	public void setDestStop_2(String id,String name)
	{
		SharedPreferences.Editor ed = sPrefs.edit();
		ed.putString("DEST_STOP_2",id); 
		ed.putString("DEST_STOP_NAME_2", name);
		Toast.makeText(this, "하교 도착 정류장이 설정되었습니다.", Toast.LENGTH_SHORT).show();
		ed.commit();
	}
	
	class FindRouteTask extends AsyncTask<String, Void, Void>
	{
		ProgressDialog dialog;
		@Override
		protected void onPostExecute(Void result) {
			dbA.close();
			dialog.dismiss();
			//adapter = new BusStopAdapter();
			//RouteViewer.this.setListAdapter(adapter);
			adapter = new BusStopAdapter();
			stationList.setAdapter(adapter);
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			dbA.open();
			dialog = new ProgressDialog(RouteViewer.this);
			dialog.setTitle("노선 정보 검색중");
			dialog.setMessage("노선 정보를 검색중 입니다. \n잠시만 기다려 주세요.");
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			dialog.show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(String... params) {
			stationArr = dbA.getBusStopOfBus(params[0]);
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
				updown = "상행";
			else
				updown = "하행"; 
			stop_updown.setText(updown);
			
			TextView stop_name = (TextView)row.findViewById(R.id.route_name);
			stop_name.setText(stationArr.get(position).getStop_name());
			
			
			String spId = sPrefs.getString("START_STOP", "");
			String destId = sPrefs.getString("DEST_STOP", "");
			
			String spId_2 = sPrefs.getString("START_STOP_2", "");
			String destId_2 = sPrefs.getString("DEST_STOP_2", "");
			
			TextView state = (TextView)row.findViewById(R.id.route_state);
			
			String curId = stationArr.get(position).getStop_id();
			if (spId.compareTo(curId) == 0)
			{
				state.setText("등교\n출발");
				stop_name.setTextColor(0xFFDAA520);
				state.setVisibility(View.VISIBLE);
			}
			else if (destId.compareTo(curId) == 0){
				state.setText("등교\n도착");
				stop_name.setTextColor(0xFFDAA520);
				state.setVisibility(View.VISIBLE);
			}
			
			else if (spId_2.compareTo(curId) == 0)
			{
				state.setText("하교\n출발");
				stop_name.setTextColor(0xFFFF0000);
				state.setVisibility(View.VISIBLE);
			}
			else if(destId_2.compareTo(curId) == 0)
			{
				state.setText("하교\n도착");
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
