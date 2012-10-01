package com.ajouroid.timetable;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class AboutUsActivity extends Activity implements OnClickListener {

	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.aboutus);
		
		Button close = ((Button)findViewById(R.id.aboutus_close));
		close.setOnClickListener(this);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		finish();
	}
}
