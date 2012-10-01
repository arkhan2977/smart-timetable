package com.ajouroid.timetable;

import java.util.Date;
import java.util.Locale;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class AddTaskDialog extends Activity implements View.OnClickListener, CheckBox.OnCheckedChangeListener {

	Button addBtn;
	Spinner type;
	EditText title;
	EditText desc;
	DatePicker dateP;
	String subject;
	CheckBox useTime;
	TimePicker timeP;
	
	boolean b_useTime = false;
	
	boolean editingMode = false;
	
	int _id=-1;
	
	Resources r;
	
	String[] spin;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.addtask);
		
		r = getResources();
		
		spin = r.getStringArray(R.array.tasks);
		
		addBtn = (Button)findViewById(R.id.addtask_addBtn);
		
		type = (Spinner)findViewById(R.id.addtask_type);
		
		title = (EditText)findViewById(R.id.addtask_title);
		desc = (EditText)findViewById(R.id.addtask_desc);
		dateP = (DatePicker)findViewById(R.id.addtask_datePicker);
		
		useTime = (CheckBox)findViewById(R.id.useTime);
		
		timeP = (TimePicker)findViewById(R.id.addtask_timePicker);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		addBtn.setOnClickListener(this);
		
		type.setAdapter(new ArrayAdapter<String>(this, R.layout.simple_spinner_row, spin));
		Intent intent = getIntent();
		subject = intent.getStringExtra("subject");
		
		if (intent.hasExtra("id"))
		{
			_id = intent.getIntExtra("id", -1);
			DBAdapter dbA = new DBAdapter(this);
			dbA.open();
			
			Cursor c = dbA.getTask(_id);
			
			int iSubject = c.getColumnIndex("subject");
			int iType = c.getColumnIndex("type");
			int iTitle = c.getColumnIndex("title");
			int iDesc = c.getColumnIndex("desc");
			int iDate = c.getColumnIndex("taskdate");
			int iUseTime = c.getColumnIndex("usetime");
			
			if (c.getString(iSubject).compareToIgnoreCase(subject) == 0)
			{

				type.setSelection(c.getInt(iType));
				title.setText(c.getString(iTitle));
				desc.setText(c.getString(iDesc));
				
				SimpleDateFormat form = new SimpleDateFormat(getResources().getString(R.string.dateformat), Locale.US);
				try {
					Date d = form.parse(c.getString(iDate));

					dateP.updateDate(d.getYear()+1900, d.getMonth(), d.getDate());
					timeP.setCurrentHour(d.getHours());
					timeP.setCurrentMinute(d.getMinutes());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (c.getInt(iUseTime) > 0)
				{
					useTime.setChecked(true);
					b_useTime = true;
					timeP.setVisibility(View.VISIBLE);
				}
				else
					useTime.setChecked(false);
			}
			c.close();
			dbA.close();
		}
		
		useTime.setOnCheckedChangeListener(this);
	}



	public void onClick(View v) {
		// TODO Auto-generated method stub
		dateP.clearFocus();
		timeP.clearFocus();
		if (!b_useTime)
		{
			timeP.setCurrentHour(23);
			timeP.setCurrentMinute(59);
		}
		
		Calendar datetime = Calendar.getInstance();
		datetime.set(dateP.getYear(), dateP.getMonth(), dateP.getDayOfMonth(), timeP.getCurrentHour(), timeP.getCurrentMinute(), 0);
		
		DBAdapter dbA = new DBAdapter(this);
		dbA.open();
		if (_id < 0)
			dbA.addTask(subject, type.getSelectedItemPosition(), title.getText().toString(), desc.getText().toString(), datetime.getTimeInMillis(), b_useTime);
		else
			dbA.updateTask(_id, subject, type.getSelectedItemPosition(), title.getText().toString(), desc.getText().toString(), datetime.getTimeInMillis(), b_useTime);
		dbA.close();
		finish();
	}

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (isChecked)
		{
			b_useTime = true;
			timeP.setVisibility(View.VISIBLE);
		}
		else
		{
			b_useTime = false;
			timeP.setVisibility(View.GONE);
		}
	}

	
}
