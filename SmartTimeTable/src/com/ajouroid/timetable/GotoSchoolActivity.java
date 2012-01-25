package com.ajouroid.timetable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

public class GotoSchoolActivity extends Activity {
	/** Called when the activity is first created. */

	private SharedPreferences mPrefs;
	ListView buslist;
	ListView buslist_2;
	TextView trafficinfo;

	Button setupBtn;
	/*
	 * TextView start_point; TextView destination; TextView sp_bus_stop;
	 * TextView dest_bus_stop;
	 */
	ArrayList<BusInfo>[] businfo;
	// ArrayList<BusInfo> businfo_2;

	Resources r;

	BusAdapter adapter;
	BusAdapter adapter_2;

	String url = "http://openapi.gbis.go.kr/ws/rest/busarrivalservice/station";
	URL requestURL;
	RequestBusInfoTask requestTask;
	RequestBusInfoTask requestTask_2;

	AlertDialog alert_dialog;
	ProgressBar prog_bar;
	TextView update_btn;

	ProgressBar prog_bar_2;
	TextView update_btn_2;

	DBAdapterBus dbA;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gotoschool);
		trafficinfo = (TextView) findViewById(R.id.trafficinfo);
		buslist = (ListView) findViewById(R.id.buslist);
		buslist.setSelector(R.drawable.stroke_bus);

		buslist_2 = (ListView) findViewById(R.id.buslist_2);
		buslist_2.setSelector(R.drawable.stroke_bus);

		r = getResources();
		/*
		 * start_point = (TextView)findViewById(R.id.start_point); destination =
		 * (TextView)findViewById(R.id.destination); sp_bus_stop =
		 * (TextView)findViewById(R.id.sp_bus_stop); dest_bus_stop =
		 * (TextView)findViewById(R.id.dest_bus_stop);
		 */

		prog_bar = (ProgressBar) findViewById(R.id.bus_progress);
		update_btn = (TextView) findViewById(R.id.bus_update);

		prog_bar_2 = (ProgressBar) findViewById(R.id.bus_progress_2);
		update_btn_2 = (TextView) findViewById(R.id.bus_update_2);

		setupBtn = (Button) findViewById(R.id.bus_setup_station);

		dbA = new DBAdapterBus(GotoSchoolActivity.this);
		dbA.open();

		// Regist_bus();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		
		// asynctask 실행.
		businfo = new ArrayList[2];

		businfo[0] = new ArrayList<BusInfo>();
		adapter = new BusAdapter(businfo[0]);
		buslist.setAdapter(adapter);

		businfo[1] = new ArrayList<BusInfo>();
		adapter_2 = new BusAdapter(businfo[1]);
		buslist_2.setAdapter(adapter_2);

		update_btn.setVisibility(View.INVISIBLE);
		prog_bar.setVisibility(ProgressBar.VISIBLE);
		requestTask = new RequestBusInfoTask(TO_SCHOOL);
		requestTask.execute();

		update_btn_2.setVisibility(View.INVISIBLE);
		prog_bar_2.setVisibility(ProgressBar.VISIBLE);
		requestTask_2 = new RequestBusInfoTask(FROM_SCHOOL);
		requestTask_2.execute();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (!dbA.isOpen())
			dbA.open();
		
		update_btn.setOnClickListener(new UpdateClickListener());
		update_btn_2.setOnClickListener(new UpdateClickListener());

		buslist.setOnItemClickListener(new Bus1ClickListener());
		buslist_2.setOnItemClickListener(new Bus2ClickListener());

		setupBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(GotoSchoolActivity.this,
						StationSetting.class);
				startActivity(intent);
			}

		});

	}

	private void setAdapter(int type) {
		switch (type) {
		case TO_SCHOOL:
			adapter = new BusAdapter(businfo[0]);
			buslist.setAdapter(adapter);
			break;
		case FROM_SCHOOL:
			adapter_2 = new BusAdapter(businfo[1]);
			buslist_2.setAdapter(adapter_2);
			break;
		}
	}

	@Override
	protected void onPause() {
		Log.d("GotoSchoolActivity", "onPause()");
		
		super.onPause();
	}

	@Override
	protected void onStop() {
		Log.d("GotoSchoolActivity", "onStop()");
		
		if (requestTask != null)
			requestTask.cancel(true);
		if (requestTask_2 != null)
			requestTask_2.cancel(true);
		dbA.close();
		super.onStop();
	}

	final int TO_SCHOOL = 0;
	final int FROM_SCHOOL = 1;

	class RequestBusInfoTask extends
			AsyncTask<Void, ArrayList<BusInfo>, Boolean> {
		ProgressDialog dialog;
		int ERROR_CODE = 0;
		int type;

		public RequestBusInfoTask(int _type) {
			this.type = _type;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (!result) {
				ErrorDialog();
			}

			switch (this.type) {
			case 0:
				prog_bar.setVisibility(View.INVISIBLE);
				update_btn.setVisibility(View.VISIBLE);
				break;
			case 1:
				prog_bar_2.setVisibility(View.INVISIBLE);
				update_btn_2.setVisibility(View.VISIBLE);
				break;
			}
		}
		
		

		@Override
		protected void onCancelled() {
			switch (this.type) {
			case 0:
				prog_bar.setVisibility(View.INVISIBLE);
				update_btn.setVisibility(View.VISIBLE);
				break;
			case 1:
				prog_bar_2.setVisibility(View.INVISIBLE);
				update_btn_2.setVisibility(View.VISIBLE);
				break;
			}
			super.onCancelled();
		}

		@Override
		protected void onPreExecute() {
			switch (this.type) {
			case TO_SCHOOL:
				update_btn.setVisibility(View.INVISIBLE);
				prog_bar.setVisibility(ProgressBar.VISIBLE);
				break;
			case FROM_SCHOOL:
				update_btn_2.setVisibility(View.INVISIBLE);
				prog_bar_2.setVisibility(ProgressBar.VISIBLE);
				break;
			}

			super.onPreExecute();
		}

		@SuppressWarnings("unchecked")
		@Override
		protected synchronized Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			int statusCode = 0;

			String sp_stationID = "NULL";
			String dest_stationID = "NULL";

			if (this.type == 0) {
				sp_stationID = mPrefs.getString("START_STOP", "NULL");
				dest_stationID = mPrefs.getString("DEST_STOP", "NULL");
			} else if (this.type == 1) {
				sp_stationID = mPrefs.getString("START_STOP_2", "NULL");
				dest_stationID = mPrefs.getString("DEST_STOP_2", "NULL");
			}

			if (sp_stationID.compareToIgnoreCase("NULL") == 0
					|| dest_stationID.compareToIgnoreCase("NULL") == 0) {

				statusCode = 17;
				return false;
			}

			if (!mPrefs.getBoolean("db_complete", false)) {
				statusCode = -1;
				return false;
			}

			try {

				ArrayList<String> validBus = dbA.findBuses(sp_stationID,
						dest_stationID);

				String key = URLEncoder.encode(Keyring.BUS_KEY, "UTF-8");
				// key = URLEncoder.encode(KEY, "UTF-8");
				XmlPullParserFactory baseparser = XmlPullParserFactory
						.newInstance();
				baseparser.setNamespaceAware(true);
				XmlPullParser xpp = baseparser.newPullParser();

				String urlStr = url + "?serviceKey=" + key + "&stationId="
						+ sp_stationID;

				Log.d("SmartTimeTable", "Requesting Bus Arrival Information...");
				Log.d("SmartTimeTable", "URL: " + urlStr);
				requestURL = new URL(urlStr);
				InputStream input = requestURL.openStream();
				xpp.setInput(input, "UTF-8");

				int parserEvent = xpp.getEventType();
				parserEvent = xpp.next();// 파싱한 자료에서 다음 라인으로 이동
				boolean check = true;

				// businfo[type].clear();
				ArrayList<BusInfo> temp = new ArrayList<BusInfo>();
				BusInfo bus = null;
				boolean skip = false;

				while (parserEvent != XmlPullParser.END_DOCUMENT) {
					if (!check) {
						break;
					}

					switch (parserEvent) {
					case XmlPullParser.END_TAG: // xml의 </> 이부분을 만나면 실행되게 됩니다.
						break;
					case XmlPullParser.START_TAG: // xml의 <> 부분을 만나게 되면 실행되게
													// 됩니다.
						if (xpp.getName().compareToIgnoreCase("returnCode") == 0) // <returnCode>
																					// 인
																					// 경우.
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
									if (tag.compareTo("busArrivalList") == 0) {
										bus = new BusInfo();
									} else if (tag.compareTo("routeId") == 0) {
										xpp.next();
										String id = xpp.getText();

										if (validBus.contains(id)) {
											bus.setBus_id(id);
											BusInfo info = dbA.getBusInfo(id);
											bus.setBus_id(id);
											bus.setBus_number(info
													.getBus_number());
										} else {
											skip = true;
										}
									} else if (tag.compareTo("predictTime1") == 0) {
										xpp.next();
										bus.setArrive_time(xpp.getText());
									}
								} else if (parserEvent == XmlPullParser.END_TAG) {
									if (xpp.getName().compareTo(
											"busArrivalList") == 0) {
										if (!skip) {
											temp.add(bus);
											Log.d("SmartTimeTable",
													type
															+ ") "
															+ bus.getBus_number()
															+ " ("
															+ bus.getArrive_time()
															+ ") added.");
										}
										skip = false;
									} else if (xpp.getName().compareTo(
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

				/*
				 * 필요없는 버스 지우기 int index = temp.size()-1;
				 * 
				 * while(true){ if(index<0){ break; }
				 * if(!validBus.contains(temp.get(index).getBus_number())){
				 * Log.d("SmartTimeTable", type + ") " +
				 * temp.get(index).getBus_number() + " removed."); try {
				 * temp.remove(index); } catch(IndexOutOfBoundsException e) {
				 * Log.d("SmartTimeTable", "IndexOutOfBoundException");
				 * e.printStackTrace(); } } index--; }
				 */
				publishProgress(temp);
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				Log.d("sibal", "xml exception");
			} catch (IOException e) {
				Log.d("sibal", "io exception");
			}

			/*
			 * switch(type) { case TO_SCHOOL: businfo = temp; break; case
			 * FROM_SCHOOL: businfo_2 = temp; break; }
			 */

			if (checkXml(statusCode)) {
				return true;
			} else {
				ERROR_CODE = statusCode;
				return false;
			}

		}

		public boolean checkXml(int statusCode) {

			if (statusCode != 0) {
				// xml 에러의 경우
				return false;
			} else {
				return true;
			}

		}

		public void ErrorDialog() {
			if (ERROR_CODE != 0) {
				switch (ERROR_CODE) {

				case -1:
					Toast.makeText(
							GotoSchoolActivity.this,
							getResources()
									.getString(R.string.dbdown_noDatabase),
							Toast.LENGTH_SHORT).show();
					return;

				case 1:
					break;
				case 2:
					break;
				case 3:
					break;
				case 4:
					break;
				case 5:
					break;
				case 6:
					break;
				case 7:
					break;
				case 8:
					break;

				case 17:
					ERROR_CODE = 18;
					break;

				case 20:
					ERROR_CODE = 9;
					break;
				case 21:
					ERROR_CODE = 10;
					break;
				case 22:
					ERROR_CODE = 11;
					break;
				case 23:
					ERROR_CODE = 12;
					break;
				case 30:
					ERROR_CODE = 13;
					break;
				case 31:
					ERROR_CODE = 14;
					break;
				case 32:
					ERROR_CODE = 15;
					break;
				case 99:
					ERROR_CODE = 16;
					break;

				default:
					ERROR_CODE = 17;
					break;
				}

				Log.d("RequestBusInfoTask", "Error : " + ERROR_CODE);
				String addition_msg = "";
				// error code에 해당하는 메시지를 띄운다.
				if (ERROR_CODE == 4) {
					addition_msg += "\n"
							+ getResources().getString(R.string.bus_noBus);
				}
				alert_dialog = new AlertDialog.Builder(GotoSchoolActivity.this)
						.setTitle("Error!!")
						.setMessage(
								getResources()
										.getStringArray(R.array.errorCode)[ERROR_CODE]
										+ addition_msg)
						.setPositiveButton(
								getResources().getString(R.string.ok),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								}).show();
			}
		}

		@Override
		protected void onProgressUpdate(ArrayList<BusInfo>... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			businfo[type] = values[0];
			setAdapter(type);
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, getResources().getString(R.string.loc_setLocation))
				.setIcon(R.drawable.icon_loc_setting);
		menu.add(0, 2, 0, getResources().getString(R.string.refresh)).setIcon(
				R.drawable.icon_refresh);
		return (super.onCreateOptionsMenu(menu));
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			Intent intent = new Intent(this, StationSetting.class);
			startActivity(intent);
			return (true);
		case 2:
			requestTask = new RequestBusInfoTask(TO_SCHOOL);
			requestTask_2 = new RequestBusInfoTask(FROM_SCHOOL);
			requestTask.execute();
			requestTask_2.execute();
			return (true);
		}
		return (super.onOptionsItemSelected(item));
	}

	class BusAdapter extends ArrayAdapter<BusInfo> {
		ArrayList<BusInfo> info;

		BusAdapter(ArrayList<BusInfo> arr) {
			super(GotoSchoolActivity.this, R.layout.row, R.id.bus_number, arr);

			info = arr;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater inflater = getLayoutInflater();

			View row = inflater.inflate(R.layout.row, parent, false);

			TextView bus_number = (TextView) row.findViewById(R.id.bus_number);
			TextView arrive_time = (TextView) row
					.findViewById(R.id.arrive_time);

			bus_number.setText(info.get(position).getBus_number());
			arrive_time.setText(info.get(position).getArrive_time()
					+ r.getString(R.string.bus_later));
			// arrive_time.setTextColor(0xFFFFFF);

			return row;

		}
	}

	class UpdateClickListener implements OnClickListener {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.bus_update:

				requestTask = new RequestBusInfoTask(0);
				requestTask.execute();
				break;
			case R.id.bus_update_2:

				requestTask_2 = new RequestBusInfoTask(1);
				requestTask_2.execute();
				break;
			}
		}

	}

	class Bus1ClickListener implements ListView.OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

			Intent i = new Intent(GotoSchoolActivity.this, RouteViewer.class);

			i.putExtra("id", businfo[0].get(arg2).getBus_id());

			startActivity(i);
		}

	}

	class Bus2ClickListener implements ListView.OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

			Intent i = new Intent(GotoSchoolActivity.this, RouteViewer.class);

			i.putExtra("id", businfo[1].get(arg2).getBus_id());

			startActivity(i);
		}

	}

}