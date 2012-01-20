package com.ajouroid.timetable;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

public class MainActivity extends Activity {

	// 데이터베이스
	DBAdapter dbA;
	SubjectDBAdapter adapter;
	Cursor c;
	
	// 위젯
	TimeTable timeTable;
	ListView listview_subject;
	Button addBtn;
	SlidingDrawer drawer; 


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
		addBtn = (Button) findViewById(R.id.btn_addSubject);
		drawer = (SlidingDrawer) findViewById(R.id.subjectDrawer);
	}

	@Override
	public void onResume() {
		super.onResume();
		initWidgets();
		initDB();
		
		registerReceiver(addReceiver, new IntentFilter("com.ajouroid.timetable.ADD_TIME"));

		timeTable.init();
		
		SmartTimeTable app = (SmartTimeTable)getApplication();
		if (app.morningCallService != null)
		{
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
				drawer.animateClose();
				Intent addDialog = new Intent(MainActivity.this,
						AddDialog.class);
				startActivityForResult(addDialog, 0);
			}

		});
		SubjectListClickListener listener = new SubjectListClickListener();
		listview_subject.setOnItemClickListener(listener);
		listview_subject.setOnItemLongClickListener(listener);
	}

	public void initDB() {

		dbA = new DBAdapter(this);

		dbA.open();

		c = dbA.getSubjectCursor();

		adapter = new SubjectDBAdapter(c);
		listview_subject.setAdapter(adapter);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
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
			if (path != null)
			{
				Toast.makeText(
						this,
						getResources().getString(R.string.exportcomplete) + "\n" + path,
						Toast.LENGTH_SHORT).show();
			}
			else
			{
				Toast.makeText(this, R.string.exportfail, Toast.LENGTH_SHORT).show();
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

		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == INFOLIST_ACTIVITY || requestCode == SELECTLIST_ACTIVITY)
		{
			if (resultCode == RESULT_OK)
			{
				String subject = data.getStringExtra("subject");
				timeTable.selectAdder(subject);
			}
		}

	}

	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		c.close();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

		Intent intent = new Intent("com.ajouroid.timetable.WIDGET_UPDATE");
		sendBroadcast(intent);

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unregisterReceiver(addReceiver);
		c.close();
		dbA.close();
	}

	// 백 버튼을 눌렀을 때
	@Override
	public void onBackPressed() {
		// 드로워가 열려있으면 닫음
		if (drawer.isOpened())
			drawer.animateClose();
		
		// 시간 추가모드일경우 취소
		else if (timeTable.isAddingMode()) {
			timeTable.endAddingMode();
		} 
		
		else
			super.onBackPressed();
	}

	class SubjectListClickListener
		implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
		// 과목을 클릭했을 때
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			drawer.animateClose();
			
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
			//과목 추가모드를 시작한다.
			timeTable.selectAdder(c.getString(iName));
			drawer.animateClose();

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
			ImageView subjectColor = (ImageView) view.findViewById(R.id.list_subjectColor);
			TextView subjectTitle = (TextView) view.findViewById(R.id.list_subjectTitle);
			TextView subjectClass = (TextView) view.findViewById(R.id.list_className);

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

			SimpleDateFormat format = new SimpleDateFormat(getResources().getString(R.string.dateformat), Locale.US);

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
					// TODO Auto-generated catch block
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
					remainTv.setText(remain + getResources().getString(R.string.daylater));
					remainTv.setTextColor(Color.BLACK);
				}
				//시간을 지정했다면 시간 단위 표시
				else if (useTime) {
					// 시간 단위 (1시간 : 3600초)
					if (remain > 3600) {
						remain = (remain + 1800) / (3600);
						remainTv.setText(remain + getResources().getString(R.string.hourlater));
						remainTv.setTextColor(0xFFFF6000); //오렌지색
						
					}
					//분 단위 (1분 : 60초)
					else if (remain > 60) {
						remain = (remain + 30)  / 60;
						remainTv.setTextColor(Color.RED);
						remainTv.setText(remain + getResources().getString(R.string.minlater));
					}
					//초 단위
					else {
						remainTv.setText(remain + getResources().getString(R.string.seclater));
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
			// TODO Auto-generated method stub
			return View.inflate(context, R.layout.subjectlist, null);
		}

	}
	
	private BroadcastReceiver addReceiver = new BroadcastReceiver()
	{

		@Override
		public void onReceive(Context context, Intent intent) {
			
			ArrayList<String> list = intent.getStringArrayListExtra("subject");
			
			if (list.size()==1)
			{
				Intent addIntent = new Intent(MainActivity.this, InfoList.class);
				int id = dbA.getIdFromTitle(list.get(0));
				addIntent.putExtra("id", id);
				MainActivity.this.startActivityForResult(addIntent, INFOLIST_ACTIVITY);
			}
			else if (list.size()>1)
			{
				Intent listIntent = new Intent(MainActivity.this, SubjectSelector.class);
				listIntent.putExtra("subject", list);
				MainActivity.this.startActivityForResult(listIntent, SELECTLIST_ACTIVITY);
			}
		}
	};
}