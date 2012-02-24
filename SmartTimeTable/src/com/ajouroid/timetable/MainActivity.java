package com.ajouroid.timetable;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.ajouroid.timetable.bus.BusInfo;
import com.ajouroid.timetable.bus.DBAdapterBus;
import com.ajouroid.timetable.bus.FavoriteList;
import com.ajouroid.timetable.bus.GotoSchoolActivity;
import com.ajouroid.timetable.bus.StationSetting;
import com.ajouroid.timetable.interpolator.BounceInterpolator;
import com.ajouroid.timetable.interpolator.EasingType.Type;
import com.ajouroid.timetable.widget.Panel;
import com.ajouroid.timetable.widget.Panel.OnPanelListener;

import android.app.Activity;
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
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class MainActivity extends Activity {

	// 데이터베이스
	DBAdapter dbA;
	SubjectDBAdapter adapter;
	TaskDBAdapter taskAdapter;
	Cursor c;
	Cursor taskC;

	// 위젯
	TimeTable timeTable;
	ListView listview_subject;
	ListView lv_task;
	Button addBtn;
	Panel drawer;
	Panel busDrawer;

	Button drawerButton;
	Button busDrawerButton;

	TextView tv_start;
	TextView tv_dest;
	Button btn_favorite;
	ListView lv_busList;
	
	ProgressBar busProgress;
	Button busUpdate;

	BusArrivalManager busManager;

	LinearLayout topdown;

	SharedPreferences sPref;
	Resources r;

	public final static int OPTION_ACTIVITY = 0;
	public final static int GOTO_ACTIVITY = 1;
	public final static int SUBJECT_ACTIVITY = 2;

	public final static int INFOLIST_ACTIVITY = 3;
	public final static int SELECTLIST_ACTIVITY = 4;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(Window.FEATURE_NO_TITLE, Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		timeTable = (TimeTable) findViewById(R.id.timetable);
		listview_subject = (ListView) findViewById(R.id.subjectList);
		lv_task = (ListView) findViewById(R.id.taskList);
		addBtn = (Button) findViewById(R.id.btn_addSubject);
		drawer = (Panel) findViewById(R.id.topPanel);
		drawer.setInterpolator(new BounceInterpolator(Type.OUT));
		busDrawer = (Panel) findViewById(R.id.busPanel);
		busDrawer.setInterpolator(new BounceInterpolator(Type.OUT));

		tv_start = (TextView) findViewById(R.id.main_bus_start);
		tv_dest = (TextView) findViewById(R.id.main_bus_dest);
		
		lv_busList = (ListView)findViewById(R.id.main_bus_list);

		btn_favorite = (Button) findViewById(R.id.main_bus_favotite_list);

		drawerButton = (Button) findViewById(R.id.panelHandle);
		busDrawerButton = (Button) findViewById(R.id.busHandle);
		
		busProgress = (ProgressBar)findViewById(R.id.main_bus_progress);
		busUpdate = (Button)findViewById(R.id.main_bus_update);

		r = getResources();
		sPref = PreferenceManager.getDefaultSharedPreferences(this);

		busManager = new BusArrivalManager(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		initDB();
		initWidgets();

		registerReceiver(addReceiver, new IntentFilter(
				"com.ajouroid.timetable.ADD_TIME"));

		timeTable.init();

		SmartTimeTable app = (SmartTimeTable) getApplication();
		if (app.morningCallService != null) {
			app.morningCallService.setMorningCall();
			app.morningCallService.setNextClassStartCall();
			app.morningCallService.setClassSilentCall();
			app.morningCallService.setTaskTimeCall();
			app.morningCallService.setDayTaskCall();
		}
	}

	public void initWidgets() {
		addBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				drawer.setOpen(false, false);
				Intent addDialog = new Intent(MainActivity.this,
						AddDialog.class);
				startActivityForResult(addDialog, 0);
			}

		});
		lv_task.setOnItemClickListener(new TaskClickListener());
		
		btn_favorite.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, FavoriteList.class);
				startActivity(i);
			}
			
		});

		SubjectListClickListener listener = new SubjectListClickListener();
		listview_subject.setOnItemClickListener(listener);
		listview_subject.setOnItemLongClickListener(listener);

		drawer.setOnPanelListener(new OnPanelListener() {

			public void onPanelClosed(Panel panel) {
				drawerButton.setText("▼ Subjects / Tasks");
			}

			public void onPanelOpened(Panel panel) {
				drawerButton.setText("▲ Subjects / Tasks");
			}

		});

		busDrawer.setOnPanelListener(new OnPanelListener() {

			public void onPanelClosed(Panel panel) {
				busDrawerButton.setText("Bus Arrival ▼");
			}

			public void onPanelOpened(Panel panel) {
				busDrawerButton.setText("Bus Arrival ▲");
				
				//busManager.setRoute(sPref.getInt("current_route", -1));
				busManager.setRoute(99); // input test value
				if (!busManager.isUpdating())
					busManager.update();
			}

		});
		
		busUpdate.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				busManager.update();
			}
			
		});

	}

	public void initDB() {

		dbA = new DBAdapter(this);

		if (!dbA.isOpen())
			dbA.open();

		c = dbA.getSubjectCursor();
		taskC = dbA.getValidTaskCursor();

		adapter = new SubjectDBAdapter(c);
		listview_subject.setAdapter(adapter);

		taskAdapter = new TaskDBAdapter(taskC);
		lv_task.setAdapter(taskAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.mainmenu, menu);

		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_alarm:
			dbA.close();
			Intent gotoschool = new Intent(this, GotoSchoolActivity.class);
			startActivityForResult(gotoschool, GOTO_ACTIVITY);
			break;
		case R.id.menu_option:
			dbA.close();
			Intent intent = new Intent(this, OptionActivity.class);
			startActivityForResult(intent, OPTION_ACTIVITY);
			break;
		case R.id.menu_share:
			if (timeTable.share()) {
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.setType("image/*");
				sendIntent.addCategory("android.intent.category.DEFAULT");

				sendIntent.putExtra("exit_on_sent", true);
				sendIntent.putExtra("subject", "시간표");

				Uri dataUri = Uri
						.parse("file:///sdcard/SmartTimeTable/share.png");
				if (dataUri != null) { // 성공이면
					sendIntent.putExtra(Intent.EXTRA_STREAM, dataUri);
				}
				startActivity(sendIntent);
			}
			break;
		case R.id.menu_toImage:
			String path = timeTable.toBitmap();
			if (path != null) {
				Toast.makeText(
						this,
						getResources().getString(R.string.exportcomplete)
								+ "\n" + path, Toast.LENGTH_LONG).show();

				sendBroadcast(new Intent("android.intent.action.MEDIA_MOUNTED",
						Uri.parse((new StringBuilder("file://")).append(
								Environment.getExternalStorageDirectory())
								.toString())));
			} else {
				Toast.makeText(this, R.string.exportfail, Toast.LENGTH_SHORT)
						.show();
			}
			break;

		case R.id.menu_add:
			dbA.close();
			Intent addDialog = new Intent(MainActivity.this, AddDialog.class);
			startActivityForResult(addDialog, 0);
			break;

		case R.id.menu_exit:
			finish();
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == INFOLIST_ACTIVITY
				|| requestCode == SELECTLIST_ACTIVITY) {
			if (resultCode == RESULT_OK) {
				String subject = data.getStringExtra("subject");
				timeTable.selectAdder(subject);
			}
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		c.close();
	}

	@Override
	protected void onStop() {
		super.onStop();

		Intent intent = new Intent("com.ajouroid.timetable.WIDGET_UPDATE");
		sendBroadcast(intent);

	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(addReceiver);
		c.close();
		dbA.close();
	}

	// 백 버튼을 눌렀을 때
	@Override
	public void onBackPressed() {
		// 드로워가 열려있으면 닫음
		if (drawer.isOpen()) {
			drawer.setOpen(false, true);
		}

		else if (busDrawer.isOpen()) {
			busDrawer.setOpen(false, true);
		}

		// 시간 추가모드일경우 취소
		else if (timeTable.isAddingMode()) {
			timeTable.endAddingMode();
		}

		else
			super.onBackPressed();
	}

	class TaskClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

			drawer.setOpen(false, false);

			busDrawer.setOpen(false, false);

			taskC.moveToPosition(arg2);

			int _id = taskC.getInt(0);

			Intent i = new Intent(MainActivity.this, TaskView.class);
			i.putExtra("id", _id);
			startActivity(i);
		}

	}

	class SubjectListClickListener implements OnItemClickListener,
			OnItemLongClickListener {
		// 과목을 클릭했을 때
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			drawer.setOpen(false, false);
			busDrawer.setOpen(false, false);

			// 과목 정보 액티비티를 실행한다.
			c.moveToPosition(arg2);
			int iName = c.getColumnIndex("name");
			int iId = c.getColumnIndex("_id");
			String name = c.getString(iName);
			int id = c.getInt(iId);

			Intent intent = new Intent(MainActivity.this, InfoList.class);
			intent.putExtra("subject", name);
			intent.putExtra("id", id);
			startActivityForResult(intent, INFOLIST_ACTIVITY);
		}

		// 과목을 길게 클릭했을 때
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {

			int iName = c.getColumnIndex("name");
			c.moveToPosition(arg2);
			// 과목 추가모드를 시작한다.
			timeTable.selectAdder(c.getString(iName));
			drawer.setOpen(false, true);

			return true;
		}
	}

	class SubjectDBAdapter extends CursorAdapter {
		int iName;
		int iClass;
		int iColor;

		public SubjectDBAdapter(Cursor cursor) {
			super(MainActivity.this, cursor);

			iName = cursor.getColumnIndex("name");
			iClass = cursor.getColumnIndex("classroom");
			iColor = cursor.getColumnIndex("color");
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ImageView subjectColor = (ImageView) view
					.findViewById(R.id.list_subjectColor);
			TextView subjectTitle = (TextView) view
					.findViewById(R.id.list_subjectTitle);
			TextView subjectClass = (TextView) view
					.findViewById(R.id.list_className);

			// 리스트뷰 설정
			subjectColor.setBackgroundColor(cursor.getInt(iColor));
			subjectTitle.setText(cursor.getString(iName));
			subjectClass.setText(cursor.getString(iClass));

			// 해당과목의 작업을 불러옴
			Cursor taskCursor = dbA.getTaskCursor(cursor.getString(iName));
			long remain = 0;

			// 현재 시간
			Date now = new Date(System.currentTimeMillis());

			int iDate = taskCursor.getColumnIndex("taskdate");
			int iUseTime = taskCursor.getColumnIndex("usetime");

			boolean useTime = false;

			SimpleDateFormat format = new SimpleDateFormat(getResources()
					.getString(R.string.dateformat), Locale.US);

			while (taskCursor.moveToNext()) {
				String dateStr = taskCursor.getString(iDate);
				try {
					// DB에서 가져온 날짜를 Date로 parse
					Date selectedDate = format.parse(dateStr);
					if (selectedDate.before(now))
						continue;

					long between = DBAdapter.distance(selectedDate, now);

					// 처음 나왔거나, 기존의 일정보다 먼저 오는 일정인 경우
					if (between < remain || remain == 0) {
						remain = between;
						// 시간 사용 여부
						if (taskCursor.getInt(iUseTime) > 0) {
							useTime = true;
						} else
							useTime = false;
					}

				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			taskCursor.close();

			// 초 단위로 변경
			remain = remain / 1000;

			TextView remainTv = (TextView) view.findViewById(R.id.list_alert);

			// 다가오는 일정이 있다면
			if (remain > 0) {

				// 일 단위 (하루 : 86400초)
				if (remain > 86400) {
					// 남은 일수를 계산
					remain = remain / 86400;
					remainTv.setText(remain
							+ getResources().getString(R.string.daylater));
					remainTv.setTextColor(Color.GRAY);
				}
				// 시간을 지정했다면 시간 단위 표시
				else if (useTime) {
					// 시간 단위 (1시간 : 3600초)
					if (remain > 3600) {
						remain = (remain + 1800) / (3600);
						remainTv.setText(remain
								+ getResources().getString(R.string.hourlater));
						remainTv.setTextColor(0xFFFF6000); // 오렌지색

					}
					// 분 단위 (1분 : 60초)
					else if (remain > 60) {
						remain = (remain + 30) / 60;
						remainTv.setTextColor(Color.RED);
						remainTv.setText(remain
								+ getResources().getString(R.string.minlater));
					}
					// 초 단위
					else {
						remainTv.setText(remain
								+ getResources().getString(R.string.seclater));
						remainTv.setTextColor(Color.RED);
					}
				}
				// 시간을 지정하지 않았다면 오늘로 표시
				else {
					remainTv.setText(getResources().getString(R.string.today));
					remainTv.setTextColor(Color.RED);

				}
			}
			// 표시할 것이 없다면 빈칸
			else
				remainTv.setText("");
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return View.inflate(context, R.layout.subjectlist, null);
		}

	}

	class TaskDBAdapter extends CursorAdapter {
		int iType;
		int iSubject;
		int iDatetime;
		int iTitle;
		int iUsetime;

		public TaskDBAdapter(Cursor cursor) {
			super(MainActivity.this, cursor);

			iType = cursor.getColumnIndex("type");
			iSubject = cursor.getColumnIndex("subject");
			iDatetime = cursor.getColumnIndex("taskdate");
			iTitle = cursor.getColumnIndex("title");
			iUsetime = cursor.getColumnIndex("usetime");
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView type = (TextView) view.findViewById(R.id.task_type);
			TextView subject = (TextView) view.findViewById(R.id.task_subject);
			TextView datetime = (TextView) view
					.findViewById(R.id.task_datetime);
			TextView title = (TextView) view.findViewById(R.id.task_title);

			type.setText(r.getStringArray(R.array.tasks)[cursor.getInt(iType)]);
			subject.setText(cursor.getString(iSubject));
			title.setText(cursor.getString(iTitle));

			String date = cursor.getString(iDatetime);
			int usetime = cursor.getInt(iUsetime);

			if (usetime == 1)
				datetime.setText(date);
			else
				datetime.setText(date.split(" ")[0]);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return View.inflate(context, R.layout.task_row, null);
		}

	}

	private BroadcastReceiver addReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			ArrayList<String> list = intent.getStringArrayListExtra("subject");

			if (list.size() == 1) {
				Intent addIntent = new Intent(MainActivity.this, InfoList.class);
				if (!dbA.isOpen())
					dbA.open();
				int id = dbA.getIdFromTitle(list.get(0));
				addIntent.putExtra("id", id);
				MainActivity.this.startActivityForResult(addIntent,
						INFOLIST_ACTIVITY);
			} else if (list.size() > 1) {
				Intent listIntent = new Intent(MainActivity.this,
						SubjectSelector.class);
				listIntent.putExtra("subject", list);
				MainActivity.this.startActivityForResult(listIntent,
						SELECTLIST_ACTIVITY);
			}
		}
	};

	/**
	 * Bus Arrival Information Class
	 */

	class BusArrivalManager {
		Context context;
		boolean updating = false;
		int route_id;
		
		String start_id = "NULL";
		String dest_id = "NULL";
		
		
		RequestBusInfoTask task;

		public BusArrivalManager(Context ctx) {
			context = ctx;
		}

		// 업데이트
		public void update() {
			if (!updating && route_id > -1)
			{
				task = new RequestBusInfoTask(context, this);
				task.execute(start_id, dest_id);
			}
		}
		
		public void startUpdate()
		{
			updating = true;
			busProgress.setVisibility(View.VISIBLE);
			busUpdate.setEnabled(false);
		}
		
		public void finishUpdate()
		{
			updating = false;
			busProgress.setVisibility(View.INVISIBLE);
			busUpdate.setEnabled(true);
		}

		// 노선 선택
		public void setRoute(int _id) {
			route_id = _id;
			if (_id == -1) {
				tv_start.setText("미설정");
				tv_dest.setText("미설정");
				start_id = "NULL";
				dest_id="NULL";
			}
			
			
			else if (_id == 99) // Test Value
			{
				tv_start.setText("대원터널4거리.전주회관.우리은행");
				tv_dest.setText("아주대학교.아주대병원입구.아주대삼거리");
				start_id = "205000156";
				dest_id = "202000061";
			}
			
			// TODO: 데이터베이스로부터 노선 정보를 가져와 설정

		}

		public boolean isUpdating() {
			return updating;
		}
	}
	
	class BusAdapter extends ArrayAdapter<BusInfo> {
		ArrayList<BusInfo> info;

		BusAdapter(ArrayList<BusInfo> arr) {
			super(MainActivity.this, R.layout.row, R.id.bus_number, arr);

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

	class RequestBusInfoTask extends AsyncTask<String, ArrayList<BusInfo>, Boolean> {
		ProgressDialog dialog;
		int ERROR_CODE = 0;
		Context context;
		DBAdapterBus busDb;
		String url = "http://openapi.gbis.go.kr/ws/rest/busarrivalservice/station";
		
		BusArrivalManager parent;

		public RequestBusInfoTask(Context ctx, BusArrivalManager manager) {
			context = ctx;
			parent = manager;
		}
		
		@Override
		protected void onPreExecute() {
			parent.startUpdate();
			busDb = new DBAdapterBus(context);
			busDb.open();
			super.onPreExecute();
		}

		@Override
		protected void onCancelled() {
			parent.finishUpdate();
			busDb.close();
			super.onCancelled();
		}

		

		@SuppressWarnings("unchecked")
		@Override
		protected synchronized Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			int statusCode = 0;

			String sp_stationID = "NULL";
			String dest_stationID = "NULL";

			sp_stationID = params[0];
			dest_stationID = params[1];


			if (sp_stationID.compareToIgnoreCase("NULL") == 0
					|| dest_stationID.compareToIgnoreCase("NULL") == 0) {

				statusCode = 17;
				return false;
			}

			if (!sPref.getBoolean("db_complete", false)) {
				statusCode = -1;
				return false;
			}

			try {
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
				URL requestURL = new URL(urlStr);
				InputStream input = requestURL.openStream();
				xpp.setInput(input, "UTF-8");

				int parserEvent = xpp.getEventType();
				parserEvent = xpp.next();// 파싱한 자료에서 다음 라인으로 이동
				boolean check = true;

				// businfo[type].clear();
				ArrayList<BusInfo> temp = new ArrayList<BusInfo>();
				BusInfo bus = null;

				ArrayList<String> validBus = busDb.findBuses(sp_stationID,
						dest_stationID);
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
											BusInfo info = busDb.getBusInfo(id);
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
											Log.d("SmartTimeTable",bus.getBus_number()
															+ " ("
															+ bus.getArrive_time()
															+ "min) added.");
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
							context,
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
				AlertDialog alert_dialog = new AlertDialog.Builder(context)
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
			super.onProgressUpdate(values);
			BusAdapter adapter = new BusAdapter(values[0]);
			lv_busList.setAdapter(adapter);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			parent.finishUpdate();
			busDb.close();
			super.onPostExecute(result);
			if (!result) {
				ErrorDialog();
			}
		}
	}
}