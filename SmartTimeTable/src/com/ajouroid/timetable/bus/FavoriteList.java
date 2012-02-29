package com.ajouroid.timetable.bus;

import com.ajouroid.timetable.DBAdapter;
import com.ajouroid.timetable.R;
import com.ajouroid.timetable.R.id;
import com.ajouroid.timetable.R.layout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FavoriteList extends Activity implements OnItemClickListener {

	private SharedPreferences mPrefs;
	DBAdapter dbA;
	Cursor c;
	FavoriteAdapter adapter;
	private static final int From_StationSetting = 0;
	Button btn_Add;

	ListView favorite_list;
	Resources r;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		dbA = new DBAdapter(FavoriteList.this);
		dbA.open();	
		super.onCreate(savedInstanceState);
		// Regist_bus();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		setContentView(R.layout.favoritelist);
		boolean v = mPrefs.getBoolean("db_complete", false);
		r = getResources();
		if (!v) {
			VersionCheckTask down_task = new VersionCheckTask(
					FavoriteList.this);
			down_task.execute();
		}
		favorite_list = (ListView)findViewById(R.id.fav_list);
		btn_Add = (Button)findViewById(R.id.fav_add);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		c = dbA.getFavoriteCursor();
		adapter = new FavoriteAdapter();
		favorite_list.setAdapter(adapter);
		favorite_list.setOnItemClickListener(this);
		
		btn_Add.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent i = new Intent(FavoriteList.this, StationSetting.class);
				startActivityForResult(i, From_StationSetting);
				Toast.makeText(FavoriteList.this, "출발정류장을 설정해 주세요.", Toast.LENGTH_LONG).show();
			}			
		});
		
		
		
	}

	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		dbA.close();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		switch(requestCode){
		case From_StationSetting: // requestCode가 B_ACTIVITY인 케이스
			if(resultCode == RESULT_OK){ //B_ACTIVITY에서 넘겨진 resultCode가 OK일때만 실행
				data.getExtras().getString("START_STOP_ID");
				data.getExtras().getString("START_STOP_NAME");
				data.getExtras().getString("DEST_STOP_ID");
				data.getExtras().getString("DEST_STOP_NAME");
				dbA.addFavoriteInfo(data.getExtras().getString("START_STOP_ID"), 
						data.getExtras().getString("START_STOP_NAME"),
						data.getExtras().getString("DEST_STOP_ID"), 
						data.getExtras().getString("DEST_STOP_NAME"));
				
				adapter.getCursor().requery();
			}
		}
	}
		
	

	
	private class FavoriteAdapter extends CursorAdapter
	{
		int iSTART_ID;
		int iSTART_NM;
		int iDEST_ID;
		int iDEST_NM;
		
		public FavoriteAdapter()
		{			
			super(FavoriteList.this, c,true);
			
			iSTART_ID = c.getColumnIndex("START_ID");
			iSTART_NM = c.getColumnIndex("START_NM");
			iDEST_ID = c.getColumnIndex("DEST_ID");	
			iDEST_NM = c.getColumnIndex("DEST_NM");	
		}

		@Override
		public void bindView(View v, Context context, Cursor cursor) {
			
			TextView tv_start = (TextView)v.findViewById(R.id.fav_row_start);
			TextView tv_dest = (TextView)v.findViewById(R.id.fav_row_dest);
			tv_start.setText(cursor.getString(iSTART_NM));
			tv_dest.setText(cursor.getString(iDEST_NM));
			ImageView btn_Delete = (ImageView)v.findViewById(R.id.fav_row_delete);
			int id = cursor.getInt(0);
			btn_Delete.setTag(id);
			btn_Delete.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					// TODO Auto-generated method stub
					dbA.deleteFavoriteInfo((Integer)v.getTag());
				}
			});
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
 
			return View.inflate(context, R.layout.favorite_row, null);
		}
	}




	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
		Editor ed = sPref.edit();
		
		c.moveToPosition(arg2);
		
		int iSTART_ID = c.getColumnIndex("START_ID");
		int iSTART_NM = c.getColumnIndex("START_NM");
		int iDEST_ID = c.getColumnIndex("DEST_ID");	
		int iDEST_NM = c.getColumnIndex("DEST_NM");	
		
		@SuppressWarnings("unused")
		int id = c.getInt(0);
		String startId = c.getString(iSTART_ID);
		
		ed.putInt("current_route", c.getInt(0));
		ed.putString("current_start_id", c.getString(iSTART_ID));
		ed.putString("current_start_name", c.getString(iSTART_NM));
		ed.putString("current_dest_id", c.getString(iDEST_ID));
		ed.putString("current_dest_name", c.getString(iDEST_NM));
		ed.commit();
		setResult(RESULT_OK);
		finish();
	}
}
