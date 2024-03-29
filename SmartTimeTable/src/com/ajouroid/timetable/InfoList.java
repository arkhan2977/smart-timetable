package com.ajouroid.timetable;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.Toast;

public class InfoList extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {
	
	ListView infoList;
	Button addBtn;
	DBAdapter dbA;
	//Cursor c;
	//TaskAdapter adapter;
	TaskArrayAdapter arrAdapter;
	
	Button addTime;
	Button editSubject;
	Button sendEmail;
	Button deleteSubject;
	
	Subject subject;
	String classRoom;
	int color;
	int id;
	
	Resources r;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.infolist);
		
		infoList = (ListView)findViewById(R.id.infolistbox);

		addTime = (Button)findViewById(R.id.info_addtime);
		addBtn = (Button)findViewById(R.id.info_addtask);
		editSubject = (Button)findViewById(R.id.info_editSubject);
		deleteSubject = (Button)findViewById(R.id.info_deleteSubject);
		sendEmail = (Button)findViewById(R.id.info_sendEmail);

		r = getResources();
	}
	
	
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		dbA = new DBAdapter(this);
		dbA.open();
		
		Intent i = getIntent();
		id = i.getIntExtra("id", -1);
		
		
		if (id>-1)
		{
			subject = dbA.getSubject(id);
		}
		else
		{
			Toast.makeText(this, r.getString(R.string.add_subjectError), Toast.LENGTH_SHORT).show();
			finish();
		}
		((LinearLayout)findViewById(R.id.info_title)).setBackgroundColor(subject.getColor());
		((TextView)findViewById(R.id.info_subjectName)).setText(subject.getName());
		((TextView)findViewById(R.id.info_classroom)).setText(subject.getClassRoom());
		
		if (subject.getProfessor().length() > 0)
		{
			String prof = subject.getProfessor();
			if (subject.getEmail().length() > 0)
				prof += " (" + subject.getEmail() + ")";
			((TextView)findViewById(R.id.info_prof)).setText(prof);
		}
		else
			((TextView)findViewById(R.id.info_prof)).setVisibility(View.GONE);
		
		
		//c = dbA.getTaskCursor(subject.getName());
		taskList = dbA.getTask(subject.getName());
		arrAdapter = new TaskArrayAdapter();
		
		//adapter = new TaskAdapter();
		infoList.setAdapter(arrAdapter);
		infoList.setOnItemClickListener(this);
		
		addBtn.setOnClickListener(new AddTaskListener());
		editSubject.setOnClickListener(this);
		deleteSubject.setOnClickListener(this);
		addTime.setOnClickListener(this);
		sendEmail.setOnClickListener(this);
		
	}


	@Override
	protected void onPause()
	{
		super.onPause();
		
		//c.close();
		dbA.close();
	}


	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		//c.close();
	}

/*
	class TaskAdapter extends CursorAdapter
	{
		int iSubject;
		int iType;
		int iTitle;
		int iDate;
		int iUseTime;
		Date now;
		
		DeleteListener listener;
		
		public TaskAdapter()
		{
			super(InfoList.this, c);
			iSubject = c.getColumnIndex("subject");
			iType = c.getColumnIndex("type");
			iTitle = c.getColumnIndex("title");
			iDate = c.getColumnIndex("taskdate");
			iUseTime = c.getColumnIndex("usetime");
			now = new Date(System.currentTimeMillis());
			
			listener = new DeleteListener();
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			TextView type = (TextView)view.findViewById(R.id.info_type);
			TextView title = (TextView)view.findViewById(R.id.info_title);
			TextView datetime = (TextView)view.findViewById(R.id.info_datetime);
			TextView remain = (TextView)view.findViewById(R.id.info_remain);
			ImageView delete = (ImageView)view.findViewById(R.id.info_delete);
			
			String typeStr=new String();
			switch(cursor.getInt(iType))
			{
			case DBAdapter.TYPE_ASSIGNMENT:
				typeStr = getResources().getString(R.string.info_assign);
				break;
			case DBAdapter.TYPE_TEST:
				typeStr = getResources().getString(R.string.info_test);
				break;
			case DBAdapter.TYPE_EXTRA:
				typeStr = getResources().getString(R.string.info_test);
				break;
			case DBAdapter.TYPE_ETC:
				typeStr = getResources().getString(R.string.info_etc);
				break;
			}
			type.setText(typeStr);
			title.setText(cursor.getString(iTitle));
			
			boolean useTime;
			
			if (cursor.getInt(iUseTime) == 1)
				useTime = true;
			else
				useTime = false;
			
			try {
				Date taskTime = (new SimpleDateFormat(getResources().getString(R.string.dateformat), Locale.US)).parse(cursor.getString(iDate));
				long dist =  DBAdapter.distance(taskTime, now);
				{
					if (useTime)
					{
						datetime.setText(cursor.getString(iDate));
					}
					else
					{
						String date = cursor.getString(iDate).split(" ")[0];
						datetime.setText(date);
					}
				}
				
				if (dist<0)
				{
					title.setTextColor(Color.LTGRAY);
					remain.setTextColor(Color.LTGRAY);
					remain.setText("지난 일정");
				}
				else
				{
					dist = dist/1000;
					if (dist > 86400) {
						// 남은 일수를 계산
						dist = dist / 86400;
						remain.setText(dist + getResources().getString(R.string.daylater));
					}
					//시간을 지정했다면 시간 단위 표시
					else if (useTime) {
						// 시간 단위 (1시간 : 3600초)
						if (dist > 3600) {
							dist = (dist + 1800) / (3600);
							remain.setText(dist + getResources().getString(R.string.hourlater));
							remain.setTextColor(0xFFFF6000); //오렌지색
						}
						//분 단위 (1분 : 60초)
						else if (dist > 60) {
							dist = (dist + 30)  / 60;
							remain.setTextColor(Color.RED);
							remain.setText(dist + getResources().getString(R.string.minlater));
						}
						//초 단위
						else {
							remain.setText(dist + getResources().getString(R.string.seclater));
							remain.setTextColor(Color.RED);
						}
					}
					else {
						remain.setText(getResources().getString(R.string.today));
						remain.setTextColor(Color.RED);
					}
				}
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			delete.setOnClickListener(listener);
			delete.setTag(cursor.getInt(0));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			return View.inflate(context, R.layout.infolist_row, null);
		}
	}
	*/
	ArrayList<Task> taskList;
	
	class TaskArrayAdapter extends ArrayAdapter<Task>
	{
		Date now;
		
		DeleteListener listener;

		public TaskArrayAdapter()
		{
			super(InfoList.this, R.id.info_title, taskList);

			now = new Date(System.currentTimeMillis());
			
			listener = new DeleteListener();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			
			if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.infolist_row, null);
            }
			
			try {
			
				TextView type = (TextView)v.findViewById(R.id.info_type);
				TextView title = (TextView)v.findViewById(R.id.info_title);
				TextView datetime = (TextView)v.findViewById(R.id.info_datetime);
				TextView remain = (TextView)v.findViewById(R.id.info_remain);
				ImageView delete = (ImageView)v.findViewById(R.id.info_delete);
				
				Task task = taskList.get(position);
				
				String typeStr=new String();
				switch(task.getType())
				{
				case DBAdapter.TYPE_ASSIGNMENT:
					typeStr = getResources().getString(R.string.info_assign);
					break;
				case DBAdapter.TYPE_TEST:
					typeStr = getResources().getString(R.string.info_test);
					break;
				case DBAdapter.TYPE_EXTRA:
					typeStr = getResources().getString(R.string.info_test);
					break;
				case DBAdapter.TYPE_ETC:
					typeStr = getResources().getString(R.string.info_etc);
					break;
				}
				type.setText(typeStr);
				title.setText(task.getName());
				
				boolean useTime = task.isUsetime();
				
				long dist=0;
				Date taskTime = new Date(task.getTaskDate());
				{
					SimpleDateFormat format;
					if (useTime)
					{
						format = new SimpleDateFormat(r.getString(R.string.dateformat), Locale.US);
						datetime.setText(format.format(taskTime));
					}
					else
					{
						format = new SimpleDateFormat(r.getString(R.string.onlydateformat), Locale.US);
						String date = format.format(taskTime);
						datetime.setText(date);
					}
				}
				
				dist = taskTime.getTime() - (new Date().getTime());
				Log.d("SmartTimeTable", "[" + task.getName() + "] " + dist + "ms");
				
				if (dist<0)
				{
					remain.setText("지난 일정");
					title.setTextColor(Color.LTGRAY);
					remain.setTextColor(Color.LTGRAY);
					datetime.setTextColor(Color.LTGRAY);
				}
				else
				{
					title.setTextColor(Color.BLACK);
					remain.setTextColor(Color.BLACK);
					datetime.setTextColor(Color.BLACK);
					dist = dist/1000;
					if (dist > 86400) {
						// 남은 일수를 계산
						dist = dist / 86400;
						remain.setText(dist + getResources().getString(R.string.daylater));
					}
					//시간을 지정했다면 시간 단위 표시
					else if (useTime) {
						// 시간 단위 (1시간 : 3600초)
						if (dist > 3600) {
							dist = (dist + 1800) / (3600);
							remain.setText(dist + getResources().getString(R.string.hourlater));
							remain.setTextColor(0xFFFF6000); //오렌지색
						}
						//분 단위 (1분 : 60초)
						else if (dist > 60) {
							dist = (dist + 30)  / 60;
							remain.setTextColor(Color.RED);
							remain.setText(dist + getResources().getString(R.string.minlater));
						}
						//초 단위
						else {
							remain.setText(dist + getResources().getString(R.string.seclater));
							remain.setTextColor(Color.RED);
						}
					}
					else {
						remain.setText(getResources().getString(R.string.today));
						remain.setTextColor(Color.RED);
					}
				}
				delete.setOnClickListener(listener);
				delete.setTag(task.getId());
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IndexOutOfBoundsException e)
			{
				return null;
			}
			return v;
		}

	}
	
	class DeleteListener implements View.OnClickListener
	{
		int id;
		public void onClick(View v) {
			// TODO Auto-generated method stub
			id = (Integer)v.getTag();
			Resources r = getResources();
			AlertDialog.Builder aDialog = new AlertDialog.Builder(InfoList.this);
			aDialog.setTitle(r.getString(R.string.warning))
			.setMessage(r.getString(R.string.deleteMsg))
			.setPositiveButton(r.getString(R.string.ok), new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dbA.deleteTask(id);
					
					taskList = dbA.getTask(subject.getName());
					arrAdapter = new TaskArrayAdapter();
					infoList.setAdapter(arrAdapter);
				}
			})
			.setNegativeButton(r.getString(R.string.cancel), new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			}); 
			
			(aDialog.create()).show();
			
		}
		
	}
	
	class AddTaskListener implements View.OnClickListener
	{

		public void onClick(View v) {
			Intent intent = new Intent(InfoList.this, AddTaskDialog.class);
			intent.putExtra("subject", subject.getName());
			startActivity(intent);
		}
		
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId())
		{
		case R.id.info_addtime:
			Intent addIntent = new Intent();
			addIntent.putExtra("subject", subject.getName());
			this.setResult(RESULT_OK, addIntent);
			finish();
			break;
		case R.id.info_sendEmail:
			String address = subject.getEmail();
			Uri uri = Uri.parse("mailto:" + address);
			Intent emailIntent = new Intent(Intent.ACTION_SENDTO, uri);
			startActivity(emailIntent);
			break;
			
		case R.id.info_editSubject:
			Intent editIntent = new Intent(this, AddDialog.class);
			editIntent.putExtra("subject", subject);
			editIntent.putExtra("id", id);
			startActivity(editIntent);
			break;
			
		case R.id.info_deleteSubject:
			Resources r = getResources();
			AlertDialog.Builder aDialog = new AlertDialog.Builder(InfoList.this);
			aDialog.setTitle(r.getString(R.string.warning))
			.setMessage(r.getString(R.string.deleteMsg))
			.setPositiveButton(r.getString(R.string.ok), new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dbA.deleteSubject(subject.getName());

					finish();
				}
			})
			.setNegativeButton(r.getString(R.string.cancel), new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
			
			(aDialog.create()).show();
			
			break;
		}
	}




	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Intent intent = new Intent(InfoList.this, TaskView.class);
		intent.putExtra("subject", subject.getName());
		//c.moveToPosition(arg2);
		intent.putExtra("id", taskList.get(arg2).getId());
		startActivity(intent);
	}
}
