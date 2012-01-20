package com.ajouroid.timetable;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

public class EditTime extends Activity implements OnClickListener {

	Button[] dayBtn;
	TimePicker startPicker;
	TimePicker endPicker;
	Button btnSave;

	String subject;
	int day;
	
	Resources r;

	ArrayList<ClassTime> timeList;
	int position;


	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		this.setContentView(R.layout.edittime);

		r = getResources();

		dayBtn = new Button[7];
		dayBtn[0] = (Button) findViewById(R.id.edit_mon);
		dayBtn[1] = (Button) findViewById(R.id.edit_tue);
		dayBtn[2] = (Button) findViewById(R.id.edit_wed);
		dayBtn[3] = (Button) findViewById(R.id.edit_thu);
		dayBtn[4] = (Button) findViewById(R.id.edit_fri);
		dayBtn[5] = (Button) findViewById(R.id.edit_sat);
		dayBtn[6] = (Button) findViewById(R.id.edit_sun);

		for (int i = 0; i < 7; i++)
			dayBtn[i].setOnClickListener(this);

		startPicker = (TimePicker) findViewById(R.id.edit_startPicker);
		endPicker = (TimePicker) findViewById(R.id.edit_endPicker);

		btnSave = (Button) findViewById(R.id.edit_save);
		btnSave.setOnClickListener(this);

		timeList = (ArrayList<ClassTime>) getIntent().getSerializableExtra(
				"timeList");
		position = getIntent().getIntExtra("position", 0);

		ClassTime curTime = timeList.get(position);

		Time start = curTime.getStartTime();
		Time end = curTime.getEndTime();

		
		startPicker.setCurrentHour(start.getHour());
		startPicker.setCurrentMinute(start.getMinute());
		endPicker.setCurrentHour(end.getHour());
		endPicker.setCurrentMinute(end.getMinute());

		subject = curTime.getSubject();

		day = curTime.getDay();
		dayBtn[day].setBackgroundResource(R.drawable.day_on);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.edit_mon:
			for (int i = 0; i < 7; i++)
				dayBtn[i].setBackgroundResource(R.drawable.day_off);
			dayBtn[0].setBackgroundResource(R.drawable.day_on);
			day = 0;
			break;
		case R.id.edit_tue:
			for (int i = 0; i < 7; i++)
				dayBtn[i].setBackgroundResource(R.drawable.day_off);
			dayBtn[1].setBackgroundResource(R.drawable.day_on);
			day = 1;
			break;
		case R.id.edit_wed:
			for (int i = 0; i < 7; i++)
				dayBtn[i].setBackgroundResource(R.drawable.day_off);
			dayBtn[2].setBackgroundResource(R.drawable.day_on);
			day = 2;
			break;
		case R.id.edit_thu:
			for (int i = 0; i < 7; i++)
				dayBtn[i].setBackgroundResource(R.drawable.day_off);
			dayBtn[3].setBackgroundResource(R.drawable.day_on);
			day = 3;
			break;
		case R.id.edit_fri:
			for (int i = 0; i < 7; i++)
				dayBtn[i].setBackgroundResource(R.drawable.day_off);
			dayBtn[4].setBackgroundResource(R.drawable.day_on);
			day = 4;
			break;
		case R.id.edit_sat:
			for (int i = 0; i < 7; i++)
				dayBtn[i].setBackgroundResource(R.drawable.day_off);
			dayBtn[5].setBackgroundResource(R.drawable.day_on);
			day = 5;
			break;
		case R.id.edit_sun:
			for (int i = 0; i < 7; i++)
				dayBtn[i].setBackgroundResource(R.drawable.day_off);
			dayBtn[6].setBackgroundResource(R.drawable.day_on);
			day = 6;
			break;
		case R.id.edit_save:
			startPicker.clearFocus();
			endPicker.clearFocus();
			Time startTime = new Time(startPicker.getCurrentHour(),
					startPicker.getCurrentMinute());
			Time endTime = new Time(endPicker.getCurrentHour(),
					endPicker.getCurrentMinute());

			if (endTime.before(startTime)) {
				Toast.makeText(this, r.getString(R.string.add_beforeStartTime),
						Toast.LENGTH_SHORT).show();
				return;
			}

			ClassTime modifiedTime = new ClassTime(day, startTime, endTime);
			modifiedTime.setSubject(subject);

			DBAdapter dbA = new DBAdapter(this);
			dbA.open();
			if (dbA.isValid(modifiedTime)) {
				dbA.close();
				for (int pos = 0; pos < timeList.size(); pos++) {
					if (pos == position)
						continue;
					else if (modifiedTime.isDuplicatedWith(timeList.get(pos))) {
						Toast.makeText(
								this,
								getResources().getString(
										R.string.err_duplicateTime),
								Toast.LENGTH_SHORT).show();
						return;
					}
				}
				timeList.set(position, modifiedTime);

				Intent i = new Intent();
				i.putExtra("timeList", timeList);
				setResult(RESULT_OK, i);
				finish();
			} else
				Toast.makeText(this,
						getResources().getString(R.string.err_duplicateTime),
						Toast.LENGTH_SHORT).show();
			break;
		}
	}
}
