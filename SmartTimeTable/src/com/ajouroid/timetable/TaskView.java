package com.ajouroid.timetable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class TaskView extends Activity {

	int _id;

	DBAdapter dbA;

	TextView tv_title;
	TextView tv_type;
	TextView tv_desc;
	TextView tv_date;

	Button btn_close;

	Task task;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		this.setContentView(R.layout.taskview);

		tv_type = (TextView) findViewById(R.id.taskview_type);
		tv_title = (TextView) findViewById(R.id.taskview_title);
		tv_desc = (TextView) findViewById(R.id.taskview_desc);
		tv_date = (TextView) findViewById(R.id.taskview_date);

		btn_close = (Button) findViewById(R.id.taskview_close);

		Intent intent = getIntent();
		if (intent.hasExtra("id")) {
			_id = intent.getIntExtra("id", -1);
			dbA = new DBAdapter(this);
			dbA.open();

			Cursor c = dbA.getTask(_id);

			int iSubject = c.getColumnIndex("subject");
			int iType = c.getColumnIndex("type");
			int iTitle = c.getColumnIndex("title");
			int iDesc = c.getColumnIndex("desc");
			int iDate = c.getColumnIndex("taskdate");
			int iUseTime = c.getColumnIndex("usetime");

			tv_type.setText(getResources().getStringArray(R.array.tasks)[c
					.getInt(iType)]);
			tv_title.setText(c.getString(iTitle));
			tv_desc.setText(c.getString(iDesc));

			SimpleDateFormat form = new SimpleDateFormat(getResources()
					.getString(R.string.dateformat), Locale.US);
			Date d = null;
			String dateStr;
			try {
				d = form.parse(c.getString(iDate));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			dateStr = (1900 + d.getYear()) + "/" + (d.getMonth()+1) + "/" + d.getDate();

			if (c.getInt(iUseTime) > 0) {
				dateStr += " " + new Time(d.getHours(), d.getMinutes()).to12Hour();
			}
			
			tv_date.setText(dateStr);
			c.close();
			dbA.close();
		}

	}
}
