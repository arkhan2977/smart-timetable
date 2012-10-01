package com.ajouroid.timetable;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView;

public class SubjectSelector extends Activity implements AdapterView.OnItemClickListener {

	ListView selectorList;
	ArrayList<String> subjectList;
	ArrayAdapter<String> adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.subjectselector);
		
		selectorList = (ListView)findViewById(R.id.selector_list);
	}	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		subjectList = new ArrayList<String>();
		
		Intent intent = getIntent();
		
		if (intent.hasExtra("subject"))
		{
			subjectList = intent.getStringArrayListExtra("subject");
			adapter = new ArrayAdapter<String>(this, R.layout.simple_list_row, subjectList);
			selectorList.setAdapter(adapter);
		}
		else finish();
		
		selectorList.setOnItemClickListener(this);
	}



	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		String subject = subjectList.get(arg2);
		DBAdapter dbA = new DBAdapter(this);
		dbA.open();
		int id = dbA.getIdFromTitle(subject);
		dbA.close();
		
		Intent intent = new Intent(this, InfoList.class);
		
		if (id>-1)
			intent.putExtra("id", id);
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == 0)
		{
			if (resultCode == RESULT_OK)
			{
				setResult(RESULT_OK, data);
			}
		}
		finish();
	}
}
