package com.ajouroid.timetable.bus;

import com.ajouroid.timetable.R;
import com.ajouroid.timetable.R.id;
import com.ajouroid.timetable.R.layout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class FavoriteList extends Activity {

	Cursor c;
	
	Button btn_Add;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		setContentView(R.layout.favoritelist);
		
		
		btn_Add = (Button)findViewById(R.id.fav_add);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		btn_Add.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent i = new Intent(FavoriteList.this, AddFavorite.class);
				startActivity(i);
			}
			
		});
	}

	private class FavoriteAdapter extends CursorAdapter
	{

		public FavoriteAdapter()
		{			
			super(FavoriteList.this, c);
		}

		@Override
		public void bindView(View v, Context context, Cursor cursor) {
			
			TextView tv_start = (TextView)v.findViewById(R.id.fav_row_start);
			TextView tv_dest = (TextView)v.findViewById(R.id.fav_row_dest);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
 
			return View.inflate(context, R.layout.favorite_row, null);
		}
	}
}
