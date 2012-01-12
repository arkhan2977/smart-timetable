package com.ajouroid.timetable;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class SmartTimeTable extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		Intent i = new Intent(this, AlarmService.class);
		startService(i);
		bindService(i, conn, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		if (isBound)
			unbindService(conn);
	}

	
	public AlarmService morningCallService = null;
	
	boolean isBound = false;
	private ServiceConnection conn = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder rawBinder) {
			morningCallService = ((AlarmService.AlarmBinder)rawBinder).getService();
			isBound = true;
		}

		public void onServiceDisconnected(ComponentName name) {
			morningCallService = null;
			
			isBound = false;
		}
	};
}
