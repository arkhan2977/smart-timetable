package com.ajouroid.timetable;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class NotifyReceiver extends BroadcastReceiver{
	int ID = 10000;
	int ID1 = 0;
	int ID2 = 5000;
	public static final String TAG = "com.ajouroid.timetable.NotifyReceiver";
	
	Resources r;
	@Override
	public void onReceive(Context context, Intent intent) {
		
		NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		r = context.getResources();
		
		if (intent.getAction().compareTo("com.ajouroid.timetable.NOTIFY_CLASS") == 0)
		{
			String past_minute = prefs.getString("alarm_time", "5");
			String subject = intent.getStringExtra("subject");
			
			Notification notice = new Notification(R.drawable.alarmclockicon, "[" + subject + "] "+ past_minute + r.getString(R.string.alarm_beforeMinute), System.currentTimeMillis());
						
			String Title = r.getString(R.string.alarm_notifyClass);
			String Text  = "[" + subject + "] "+ past_minute + r.getString(R.string.alarm_beforeMinute);
			
			Intent i = new Intent(context, MainActivity.class);
			
			PendingIntent pendingintent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
			notice.setLatestEventInfo(context, Title, Text, pendingintent);
			
			notice.defaults = Notification.DEFAULT_VIBRATE;
			notice.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
			
			nm.notify(TAG, ID++, notice);
			
		}
		else if(intent.getAction().compareTo("com.ajouroid.timetable.NOTIFY_TASK") == 0)
		{
			final String[] tasksubject = r.getStringArray(R.array.tasks);
			String subject = intent.getStringExtra("subject");
			String title = intent.getStringExtra("title");
			int type = intent.getIntExtra("type", 0);
			
			int mins = Integer.parseInt(prefs.getString("task_time", "60"));
	
			String notifyTitle = tasksubject[type] + " ";
			
			String Text;
			if (mins < 60)
			{
				notifyTitle += mins + r.getString(R.string.alarm_beforeMinute);
				Text = mins + r.getString(R.string.alarm_afterMinute);
			}
			else
			{
				notifyTitle += (mins/60) + r.getString(R.string.alarm_beforeHour);
				Text = (mins/60) + r.getString(R.string.alarm_afterHour);
			}
			
			
			Notification tNotice = new Notification(R.drawable.alarmtaskicon, notifyTitle, System.currentTimeMillis());
			
			
			String Title = tasksubject[type];
			Text += title + "(" + subject + ") " + r.getString(R.string.alarm_taskAlert);
			
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

			Notification nNotice = new Notification(R.drawable.alarmtaskicon, r.getString(R.string.alarm_todayTaskTitle), System.currentTimeMillis());
			
			String Title = r.getString(R.string.alarm_todayTask);
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
