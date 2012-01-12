package com.ajouroid.timetable;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class NotifyReceiver extends BroadcastReceiver{
	int ID = 10000;
	int ID1 = 0;
	int ID2 = 5000;
	public static final String TAG = "SmartTimeTable.ClassNotification";
	@Override
	public void onReceive(Context context, Intent intent) {
		
		NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			
		if (intent.getAction().compareTo("com.ajouroid.timetable.NOTIFY_CLASS") == 0)
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			String past_minute = prefs.getString("alarm_time", "5");
			
			Notification notice = new Notification(R.drawable.alarmclockicon, "수업 "+past_minute+" 분 전입니다", System.currentTimeMillis());
	
			String Title = "수업 "+ past_minute + " 분 전";
			String Text  = "수업 "+ past_minute + " 분 전 입니다.";
			
			Intent i = new Intent(context, MainActivity.class);
			
			PendingIntent pendingintent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
			notice.setLatestEventInfo(context, Title, Text, pendingintent);
			
			notice.defaults = Notification.DEFAULT_VIBRATE;
			notice.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
			
			nm.notify(TAG, ID++, notice);
			
		}
		else if(intent.getAction().compareTo("com.ajouroid.timetable.NOTIFY_TASK") == 0)
		{
			final String[] tasksubject = {"과제", "시험", "보강", "기타" };
			String subject = intent.getStringExtra("subject");
			String title = intent.getStringExtra("title");
			int type = intent.getIntExtra("type", 0);
			Notification tNotice = new Notification(R.drawable.alarmtaskicon, tasksubject[type]+" 한시간 전입니다.", System.currentTimeMillis());
			
			String Title = tasksubject[type];
			String Text = "1시간 후에 " + title + "(" + subject + ") 일정이 있습니다.";
			
			Intent i = new Intent(context, MainActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
			tNotice.setLatestEventInfo(context, Title, Text, pendingIntent);
			
			tNotice.defaults = Notification.DEFAULT_VIBRATE;
			tNotice.flags |= Notification.FLAG_AUTO_CANCEL;
			nm.notify(TAG, ID1++, tNotice);
		}
		
		else if(intent.getAction().compareTo("com.ajouroid.timetable.NOTIFY_DAY_TASK") == 0)
		{
			ArrayList<String> subjectList = intent.getStringArrayListExtra("subject");
			ArrayList<String> titleList = intent.getStringArrayListExtra("title");

			Notification nNotice = new Notification(R.drawable.alarmtaskicon, "오늘의 일정이 있습니다.", System.currentTimeMillis());
			
			String Title = "오늘의 일정";
			String Text = "";
			
			for (int i=0; i<subjectList.size(); i++)
			{
				if (i>0)
					Text += ", ";
				Text += "[" + subjectList.get(i) + "] " + titleList.get(i);
			}
			
			Intent in = new Intent(context, MainActivity.class);
			PendingIntent pending = PendingIntent.getActivity(context, 0, in, PendingIntent.FLAG_UPDATE_CURRENT);
			nNotice.setLatestEventInfo(context, Title, Text, pending);
			
			nNotice.flags |= Notification.FLAG_AUTO_CANCEL;
			nm.notify(TAG, ID, nNotice);
		}
		else if(intent.getAction().compareTo("com.ajouroid.timetable.NOTIFY_SILENT") == 0)
		{
			AudioManager audioMg = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
			audioMg.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
			Log.d("SmartTimeTable","Set Vibrate Mode");
		}
		Intent updater = new Intent();
		updater.setAction(AlarmService.UPDATE_ACTION);
		context.sendBroadcast(updater);
	}
	
}
