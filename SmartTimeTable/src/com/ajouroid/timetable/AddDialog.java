package com.ajouroid.timetable;

import java.util.ArrayList;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class AddDialog extends Activity implements OnClickListener {
	EditText subjectName;
	EditText classRoom;
	EditText professorName;
	EditText proEmail;

	
	Button[] dayBtn;
	
	LinearLayout bg1;
	LinearLayout bg2;
	LinearLayout bg3;
	LinearLayout bg4;
	LinearLayout bg5;
	LinearLayout bg6;
	LinearLayout bg7;
	LinearLayout bg8;
	LinearLayout bg9;
	LinearLayout bg10;
	LinearLayout bg11;
	LinearLayout bg12;
	
	
	ImageView color1;
	ImageView color2;
	ImageView color3;
	ImageView color4;
	ImageView color5;
	ImageView color6;
	ImageView color7;
	ImageView color8;
	ImageView color9;
	ImageView color10;
	ImageView color11;
	ImageView color12;

	HorizontalScrollView scrollView;
	
	int color;

	int dayset;

	Button addTimeBtn;
	Button storeBtn;
	Button cancelBtn;
	Button startTimebtn;
	Button endTimebtn;

	ListView timeListView;
	TimeListAdapter adapter;

	TimePickerDialog startPicker;
	TimePickerDialog endPicker;

	Time startTime;
	Time endTime;

	ArrayList<ClassTime> timeList;

	DBAdapter dbHelper;

	boolean editMode = false;
	String originalName = "";

	String[] dayString;
	
	int _id=-1;
	
	final static int ID_EDIT_TIME = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		setContentView(R.layout.adddialog);

		timeListView = (ListView) findViewById(R.id.add_timelist);

		LayoutInflater li = getLayoutInflater();
		LinearLayout head = (LinearLayout) li.inflate(R.layout.add_head, null);
		timeListView.addHeaderView(head);
		//LinearLayout foot = (LinearLayout) li.inflate(R.layout.add_foot, null);
		//timeListView.addFooterView(foot);

		timeList = new ArrayList<ClassTime>();

		classRoom = (EditText) findViewById(R.id.dlgClassname);
		professorName = (EditText) findViewById(R.id.dlgProfessorname);
		subjectName = (EditText) findViewById(R.id.dlgsubject);
		proEmail = (EditText) findViewById(R.id.dlgproEmail);
		
		dayBtn = new Button[7];
		dayBtn[0] = (Button) findViewById(R.id.add_mon);
		dayBtn[1] = (Button) findViewById(R.id.add_tue);
		dayBtn[2] = (Button) findViewById(R.id.add_wed);
		dayBtn[3] = (Button) findViewById(R.id.add_thu);
		dayBtn[4] = (Button) findViewById(R.id.add_fri);
		dayBtn[5] = (Button) findViewById(R.id.add_sat);
		dayBtn[6] = (Button) findViewById(R.id.add_sun);

		
		bg1 = (LinearLayout) findViewById(R.id.colorBg1);
		bg2 = (LinearLayout) findViewById(R.id.colorBg2);
		bg3 = (LinearLayout) findViewById(R.id.colorBg3);
		bg4 = (LinearLayout) findViewById(R.id.colorBg4);
		bg5 = (LinearLayout) findViewById(R.id.colorBg5);
		bg6 = (LinearLayout) findViewById(R.id.colorBg6);
		bg7 = (LinearLayout) findViewById(R.id.colorBg7);
		bg8 = (LinearLayout) findViewById(R.id.colorBg8);
		bg9 = (LinearLayout) findViewById(R.id.colorBg9);
		bg10 = (LinearLayout) findViewById(R.id.colorBg10);
		bg11 = (LinearLayout) findViewById(R.id.colorBg11);
		bg12 = (LinearLayout) findViewById(R.id.colorBg12);
		
		
		color1 = (ImageView) findViewById(R.id.color1);
		color2 = (ImageView) findViewById(R.id.color2);
		color3 = (ImageView) findViewById(R.id.color3);
		color4 = (ImageView) findViewById(R.id.color4);
		color5 = (ImageView) findViewById(R.id.color5);
		color6 = (ImageView) findViewById(R.id.color6);
		color7 = (ImageView) findViewById(R.id.color7);
		color8 = (ImageView) findViewById(R.id.color8);
		color9 = (ImageView) findViewById(R.id.color9);
		color10 = (ImageView) findViewById(R.id.color10);
		color11 = (ImageView) findViewById(R.id.color11);
		color12 = (ImageView) findViewById(R.id.color12);
		
		scrollView = (HorizontalScrollView)findViewById(R.id.add_scrollview);

		startTimebtn = (Button) findViewById(R.id.starttime);
		endTimebtn = (Button) findViewById(R.id.endtime);

		storeBtn = (Button) findViewById(R.id.storeButton);
		cancelBtn = (Button) findViewById(R.id.cancelButton);

		addTimeBtn = (Button) findViewById(R.id.add_addtime);

		color = getResources().getColor(R.color.color1);

		dbHelper = new DBAdapter(this);
		dbHelper.open();

		Intent intent = getIntent();

		if (intent.getExtras() != null) {
			int id = intent.getIntExtra("id", -1);
			if (id>-1)
			{
				Cursor c = dbHelper.getSubjectCursor(id);
				
				int iName = c.getColumnIndex("name");
				int iClass = c.getColumnIndex("classroom");
				int iProf = c.getColumnIndex("professor");
				int iEmail = c.getColumnIndex("email");
				int iColor = c.getColumnIndex("color");
				
				_id = c.getInt(0);
				subjectName.setText(c.getString(iName));
				classRoom.setText(c.getString(iClass));
				professorName.setText(c.getString(iProf));
				proEmail.setText(c.getString(iEmail));
				color = c.getInt(iColor);
				
				
				ClassTime[] timeArr = dbHelper.getTimes(c.getString(iName));
				if (timeArr != null)
				{
					for (int i=0; i<timeArr.length; i++)
					{
						timeList.add(timeArr[i]);
					}
				}
	
				editMode = true;
				originalName = c.getString(iName);
				c.close();
			}
			else
			{
				Toast.makeText(this, "과목 정보를 읽어오는 중 오류가 발생했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
				finish();
			}
		}
		
		dbHelper.close();
		
		findSetColor();
	}

	@Override
	protected void onResume() {
		
		dayString = getResources().getStringArray(R.array.days);
		startPicker = new TimePickerDialog(this, new StartTimeListener(), 9, 0,
				false);
		endPicker = new TimePickerDialog(this, new EndTimeListener(), 10, 30,
				false);

		startTime = new Time(9, 0);
		endTime = new Time(10, 30);

		dayBtn[0].setOnClickListener(this);
		dayBtn[1].setOnClickListener(this);
		dayBtn[2].setOnClickListener(this);
		dayBtn[3].setOnClickListener(this);
		dayBtn[4].setOnClickListener(this);
		dayBtn[5].setOnClickListener(this);
		dayBtn[6].setOnClickListener(this);

		ColorClickListener colorListener = new ColorClickListener();
		color1.setOnClickListener(colorListener);
		color2.setOnClickListener(colorListener);
		color3.setOnClickListener(colorListener);
		color4.setOnClickListener(colorListener);
		color5.setOnClickListener(colorListener);
		color6.setOnClickListener(colorListener);
		color7.setOnClickListener(colorListener);
		color8.setOnClickListener(colorListener);
		color9.setOnClickListener(colorListener);
		color10.setOnClickListener(colorListener);
		color11.setOnClickListener(colorListener);
		color12.setOnClickListener(colorListener);

		startTimebtn.setOnClickListener(this);
		endTimebtn.setOnClickListener(this);
		addTimeBtn.setOnClickListener(this);

		storeBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);

		adapter = new TimeListAdapter();
		timeListView.setAdapter(adapter);

		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		dbHelper.close();
	}

	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.add_mon:
			if ((dayset & 0x1) == 0)
				dayBtn[0].setBackgroundResource(R.drawable.day_on);
			else
				dayBtn[0].setBackgroundResource(R.drawable.day_off);

			dayset = dayset ^ 0x1;
			break;
		case R.id.add_tue:
			if ((dayset & 0x2) == 0)
				dayBtn[1].setBackgroundResource(R.drawable.day_on);
			else
				dayBtn[1].setBackgroundResource(R.drawable.day_off);

			dayset = dayset ^ 0x2;
			break;
			
		case R.id.add_wed:
			if ((dayset & 0x4) == 0)
				dayBtn[2].setBackgroundResource(R.drawable.day_on);
			else
				dayBtn[2].setBackgroundResource(R.drawable.day_off);

			dayset = dayset ^ 0x4;
			break;
			
		case R.id.add_thu:
			if ((dayset & 0x8) == 0)
				dayBtn[3].setBackgroundResource(R.drawable.day_on);
			else
				dayBtn[3].setBackgroundResource(R.drawable.day_off);

			dayset = dayset ^ 0x8;
			break;
			
		case R.id.add_fri:
			if ((dayset & 0x10) == 0)
				dayBtn[4].setBackgroundResource(R.drawable.day_on);
			else
				dayBtn[4].setBackgroundResource(R.drawable.day_off);

			dayset = dayset ^ 0x10;
			break;


		case R.id.add_sat:
			if ((dayset & 0x20) == 0)
				dayBtn[5].setBackgroundResource(R.drawable.day_on);
			else
				dayBtn[5].setBackgroundResource(R.drawable.day_off);

			dayset = dayset ^ 0x20;
			break;
		case R.id.add_sun:
			if ((dayset & 0x40) == 0)
				dayBtn[6].setBackgroundResource(R.drawable.day_on);
			else
				dayBtn[6].setBackgroundResource(R.drawable.day_off);

			dayset = dayset ^ 0x40;
			break;


		case R.id.starttime:
			startPicker.updateTime(startTime.getHour(), startTime.getMinute());
			startPicker.show();
			break;

		case R.id.endtime:
			endPicker.updateTime(endTime.getHour(), endTime.getMinute());
			endPicker.show();
			break;

		case R.id.add_addtime:
			if (dayset == 0) {
				Toast.makeText(this,
						getResources().getString(R.string.err_nullDays),
						Toast.LENGTH_SHORT).show();
				break;
			}
			
			dbHelper.open();
			
			for (int i=0; i<7; i++)
			{
				if (((dayset >> i) & 1) == 1) {
					dayBtn[i].setBackgroundResource(R.drawable.day_off);
					dayset = dayset ^ (1 << i);
					ClassTime time = new ClassTime(i, startTime, endTime);
					time.setSubject(originalName);
					if (dbHelper.isValid(time)) {
						for (int pos = 0; pos < timeList.size(); pos++) {
							if (time.isDuplicatedWith(timeList.get(pos))) {
								
								Toast.makeText(
										this,
										getResources().getString(
												R.string.err_duplicateTime),
										Toast.LENGTH_SHORT).show();
								return;
							}
						}
						timeList.add(time);
						Log.d("SmartTimeTable",
								"New time added: " + dayString[i] + ", "
										+ startTime.toString() + "~"
										+ endTime.toString());
						
					} else {
						Toast.makeText(
								this,
								getResources().getString(
										R.string.err_duplicateTime),
								Toast.LENGTH_SHORT).show();
						break;
					}
				}
			}
			dbHelper.close();
			adapter.notifyDataSetChanged();
			break;

		case R.id.storeButton:
			if (TextUtils.isEmpty(subjectName.getText())) {
				Toast.makeText(this,
						getResources().getString(R.string.err_nullSubjectName),
						Toast.LENGTH_SHORT).show();
				break;
			} else if (TextUtils.isEmpty(classRoom.getText())) {
				Toast.makeText(this,
						getResources().getString(R.string.err_nullClassRoom),
						Toast.LENGTH_SHORT).show();
				break;
			} else {
				String newName = subjectName.getText().toString();
				String newRoom = classRoom.getText().toString();
				String newProf = professorName.getText().toString();
				String newEmail = proEmail.getText().toString();

				Subject newSubject = new Subject(newName, newRoom, newProf,
						newEmail, color);
				
				for (int j=0; j<timeList.size(); j++)
				{
					newSubject.addTime(timeList.get(j));
				}

				dbHelper.open();
				if (_id > 0) {
					if (!dbHelper.updateSubject(_id, newSubject, originalName))
					{
						dbHelper.close();
						Toast.makeText(this,
								"과목 이름이 중복됩니다.",
								Toast.LENGTH_SHORT).show();
					}
					else
					{
						dbHelper.close();
						finish();
					}
				}

				else if (dbHelper.addSubject(newSubject)) {
					for (int index = 0; index < timeList.size(); index++) {
						ClassTime time = timeList.get(index);
						if (dbHelper.isValid(time)) {
							newSubject.addTime(time);
							dbHelper.addTime(newName, time);
						}
					}
					dbHelper.close();
					finish();
				}
				else
				{
					dbHelper.close();
					Toast.makeText(this,
							"과목 이름이 중복됩니다.",
							Toast.LENGTH_SHORT).show();
				}
				break;
			}

		case R.id.cancelButton:
			finish();
			break;
		}
	}
	
	

	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ID_EDIT_TIME)
		{
			if (resultCode == RESULT_OK)
			{
				timeList.clear();
				timeList = (ArrayList<ClassTime>)data.getSerializableExtra("timeList");
				adapter.notifyDataSetChanged();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}



	class TimeListAdapter extends ArrayAdapter<ClassTime> {
		ListDeleteListener deleteListener;
		ListEditListener editListener;

		public TimeListAdapter() {
			super(AddDialog.this, R.layout.listbox_time, R.id.timelist_day,
					timeList);
			deleteListener = new ListDeleteListener();
			editListener = new ListEditListener();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;

			if (row == null) {
				row = getLayoutInflater().inflate(R.layout.listbox_time,
						parent, false);
			}

			TextView day = (TextView) row.findViewById(R.id.timelist_day);
			TextView start = (TextView) row.findViewById(R.id.timelist_start);
			TextView end = (TextView) row.findViewById(R.id.timelist_end);
			ImageView edit = (ImageView) row.findViewById(R.id.timelist_edit);
			ImageView delete = (ImageView) row.findViewById(R.id.timelist_delete);

			ClassTime time = timeList.get(position);
			day.setText(dayString[time.getDay()]);
			start.setText(time.getStartTime().toString());
			end.setText(time.getEndTime().toString());

			edit.setOnClickListener(editListener);
			edit.setTag(position);
			delete.setOnClickListener(deleteListener);
			delete.setTag(position);

			return row;
		}
	}

	public void findSetColor() {
		
		Resources r = getResources();
		int color1 = r.getColor(R.color.color1);
		int color2 = r.getColor(R.color.color2);
		int color3 = r.getColor(R.color.color3);
		int color4 = r.getColor(R.color.color4);
		int color5 = r.getColor(R.color.color5);
		int color6 = r.getColor(R.color.color6);
		int color7 = r.getColor(R.color.color7);
		int color8 = r.getColor(R.color.color8);
		int color9 = r.getColor(R.color.color9);
		int color10 = r.getColor(R.color.color10);
		int color11 = r.getColor(R.color.color11);
		int color12 = r.getColor(R.color.color12);

		bg1.setBackgroundColor(Color.TRANSPARENT);
		bg2.setBackgroundColor(Color.TRANSPARENT);
		bg3.setBackgroundColor(Color.TRANSPARENT);
		bg4.setBackgroundColor(Color.TRANSPARENT);
		bg5.setBackgroundColor(Color.TRANSPARENT);
		bg6.setBackgroundColor(Color.TRANSPARENT);
		bg7.setBackgroundColor(Color.TRANSPARENT);
		bg8.setBackgroundColor(Color.TRANSPARENT);
		bg9.setBackgroundColor(Color.TRANSPARENT);
		bg10.setBackgroundColor(Color.TRANSPARENT);
		bg11.setBackgroundColor(Color.TRANSPARENT);
		bg12.setBackgroundColor(Color.TRANSPARENT);

		if (color == color1)
			bg1.setBackgroundColor(Color.WHITE);
		
		else if (color == color2)
			bg2.setBackgroundColor(Color.WHITE);

		else if (color == color3)
			bg3.setBackgroundColor(Color.WHITE);

		else if (color == color4)
			bg4.setBackgroundColor(Color.WHITE);

		else if (color == color5)
			bg5.setBackgroundColor(Color.WHITE);

		else if (color == color6)
			bg6.setBackgroundColor(Color.WHITE);

		else if (color == color7)
			bg7.setBackgroundColor(Color.WHITE);

		else if (color == color8)
			bg8.setBackgroundColor(Color.WHITE);
		
		else if (color == color9)
			bg9.setBackgroundColor(Color.WHITE);
		
		else if (color == color10)
			bg10.setBackgroundColor(Color.WHITE);
		
		else if (color == color11)
			bg11.setBackgroundColor(Color.WHITE);
		
		else if (color == color12)
			bg12.setBackgroundColor(Color.WHITE);
	}

	class StartTimeListener implements OnTimeSetListener {

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// TODO Auto-generated method stub
			startTime = new Time(hourOfDay, minute);

			int result = endTime.compare(startTime);

			if (result != Time.BIGGER) {
				endTime = new Time(startTime.toString());
				endTime.addTime(new Time(1, 30));
				endTimebtn.setText(endTime.to12Hour());
			}

			startTimebtn.setText(startTime.to12Hour());
		}
	}

	class EndTimeListener implements OnTimeSetListener {

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// TODO Auto-generated method stub
			Time time = new Time(hourOfDay, minute);

			int result = startTime.compare(time);

			if (result != Time.LESS) {
				endTime = new Time(startTime.toString());
				endTime.addTime(new Time(1, 30));

			} else {
				endTime = time;
			}
			endTimebtn.setText(endTime.to12Hour());
		}
	}
	
	class ListEditListener implements View.OnClickListener {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			int position = (Integer) v.getTag();

			Intent i = new Intent(AddDialog.this, EditTime.class);
			i.putExtra("timeList", timeList);
			i.putExtra("position", position);
			startActivityForResult(i, ID_EDIT_TIME);
		}
		
	}

	class ListDeleteListener implements View.OnClickListener {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			int position = (Integer) v.getTag();

			timeList.remove(position);
			adapter.notifyDataSetChanged();
		}
	}

	class ColorClickListener implements View.OnClickListener {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			Resources r = getResources();

			switch (id) {
			// �� ����
			case R.id.color1:
				color = r.getColor(R.color.color1);
			
				break;
			case R.id.color2:
				color = r.getColor(R.color.color2);
			
				break;
			case R.id.color3:
				color = r.getColor(R.color.color3);
			
				break;
			case R.id.color4:
				color = r.getColor(R.color.color4);
		
				break;
			case R.id.color5:
				color = r.getColor(R.color.color5);
			
				break;
			case R.id.color6:
				color = r.getColor(R.color.color6);
		
				break;
			case R.id.color7:
				color = r.getColor(R.color.color7);
			
				break;
			case R.id.color8:
				color = r.getColor(R.color.color8);
		
				break;
			case R.id.color9:
				color = r.getColor(R.color.color9);
		
				break;
			case R.id.color10:
				color = r.getColor(R.color.color10);
		
				break;
			case R.id.color11:
				color = r.getColor(R.color.color11);
		
				break;
			case R.id.color12:
				color = r.getColor(R.color.color12);
		
				break;
			}
			
			findSetColor();

		}

	}
}
