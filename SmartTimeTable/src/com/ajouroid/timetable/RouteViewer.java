package com.ajouroid.timetable;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
