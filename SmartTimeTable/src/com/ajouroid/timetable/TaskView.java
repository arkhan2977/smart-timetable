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
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class TaskView extends Activity implements OnClickListener{

	int _id;

	DBAdapter dbA;

	TextView tv_title;
	TextView tv_type;
	TextView tv_desc;
	TextView tv_date;

	Button btn_close;
	Button btn_edit;

	Task task;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.taskview);

		tv_type = (TextView) findViewById(R.id.taskview_type);
		tv_title = (TextView) findViewById(R.id.taskview_title);
		tv_desc = (TextView) findViewById(R.id.taskview_desc);
		tv_date = (TextView) findViewById(R.id.taskview_date);

		btn_close = (Button) findViewById(R.id.taskview_close);
		btn_edit = (Button)findViewById(R.id.taskview_edit);
	}
	

	@Override
	protected void onResume() {
		btn_close.setOnClickListener(this);
		btn_edit.setOnClickListener(this);
		
		tv_title.setSelected(true);
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
			
			task = new Task(c.getString(iSubject), c.getString(iTitle), c.getLong(iDate), c.getInt(iType));
			task.setId(_id);

			tv_type.setText(getResources().getStringArray(R.array.tasks)[task.getType()]);
			tv_title.setText(task.getName());
			tv_desc.setText(c.getString(iDesc));


			Date d = new Date(c.getLong(iDate));
			SimpleDateFormat format;
			
			if (c.getInt(iUseTime) > 0)
			{
				format = new SimpleDateFormat(this.getResources().getString(R.string.dateformat), Locale.US);
			}
			else
				format = new SimpleDateFormat(this.getResources().getString(R.string.onlydateformat), Locale.US);
			
			String dateStr = format.format(d);
			
			tv_date.setText(dateStr);
			c.close();
			dbA.close();
		}
		super.onResume();
	}


	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.taskview_edit:
			Intent intent = new Intent(this, AddTaskDialog.class);
			intent.putExtra("subject", task.getSubject());
			intent.putExtra("id", task.getId());
			startActivity(intent);
			break;
		case R.id.taskview_close:
			finish();
		}
	}
}
