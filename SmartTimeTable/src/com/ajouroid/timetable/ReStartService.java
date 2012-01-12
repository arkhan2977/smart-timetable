package com.ajouroid.timetable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReStartService extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent(context, AlarmService.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startService(i);
		Log.w("SmartTimeTable","Start Service");
	}
}
