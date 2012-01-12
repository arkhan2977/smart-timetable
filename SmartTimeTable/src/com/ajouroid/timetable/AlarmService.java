package com.ajouroid.timetable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

public class AlarmService extends Service {
	public static final String UPDATE_ACTION = "com.ajouroid.timetable.UPDATE_ALARM";

	AlarmBinder binder = new AlarmBinder();
	AlarmManager am;
	String ringtone;
	SharedPreferences prefs;
	PendingIntent pi;

	public final static int ALARM_VIEW = 1;
	public final static int NOTIFY_RECEIVER = 2;
	public final static int NOTIFY_TASK = 3;
	public final static int NOTIFY_DAY_TASK = 4;
	public final static int NOTIFY_SILENT = 5;

	final static String[] daysArr = { "월", "화", "수", "목", "금", "토", "일" };

	@Override
	public void onCreate() {
		am = (AlarmManager) getSystemService(ALARM_SERVICE);
		unregisterRestart();
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		setMorningCall();
		setNextClassStartCall();
		setClassSilentCall();
		setTaskTimeCall();
		setDayTaskCall();
		unregisterRestart();
		return START_STICKY;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		registerRestart();
	}

	public void registerRestart() {
		Intent i = new Intent(this, ReStartService.class);
		i.setAction(UPDATE_ACTION);

		PendingIntent sender = PendingIntent.getBroadcast(this, 0, i, 0);

		long firstTime = SystemClock.elapsedRealtime();
		firstTime += 10000; // 10초 후에 알람이벤트 발생
		AlarmManager a = (AlarmManager) getSystemService(ALARM_SERVICE);
		a.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime,
				10000, sender);
	}

	public void unregisterRestart() {
		Log.d("AlarmService", "Service Restarted.");
		Intent i = new Intent(this, ReStartService.class);
		i.setAction(UPDATE_ACTION);

		PendingIntent sender = PendingIntent.getBroadcast(this, 0, i, 0);

		AlarmManager a = (AlarmManager) getSystemService(ALARM_SERVICE);
		a.cancel(sender);
	}

	@Override
	public IBinder onBind(Intent arg0) {

		return binder;
	}

	// //////////////////////////////////////////////////////////////////////////////////
	public void setMorningCall() {
		boolean set = prefs.getBoolean("morningcall", false);

		cancelMorningCall();

		if (!set) {
			Log.d("AlarmService", "MorningCall Canceled");
			return;
		}
		DBAdapter dbA = new DBAdapter(this);
		dbA.open();

		Calendar today = Calendar.getInstance();
		int day = today.get(Calendar.DAY_OF_WEEK) - 2; // 오늘
		if (day < 0)
			day = 6; // 2를뺐을때 0보다 작으면 day를 6으로 일요일.

		int today_date = day; // 오늘을 받아옴.
		Time t = null;
		int adder = 0;

		String goingTime = prefs.getString("goingtime", "00:30");
		Time t_goingTime = new Time(goingTime);

		while (true) {
			t = dbA.getFirstClassTime(day);
			if (day == today_date
					&& t != null
					&& t.toMinute() - t_goingTime.toMinute() > (today
							.get(Calendar.HOUR_OF_DAY) * 60 + today
							.get(Calendar.MINUTE))) {
				break;
			}

			if (day != today_date && t != null) {
				break;
			} else {
				adder++;
				day++;
				if (day > 6)
					day = 0;
				if (day == today_date) {
					t = null;
					break;
				}
			}
		}

		if (t != null) {
			t = t.subTime(t_goingTime);
			today.add(Calendar.DAY_OF_MONTH, adder);
			today.set(Calendar.HOUR_OF_DAY, t.getHour());
			today.set(Calendar.MINUTE, t.getMinute());
			today.set(Calendar.SECOND, 0);
			ringtone = prefs.getString("alarm_music", "DEFAULT_RINGTONE_URI");
			Intent intent = new Intent(this, AlarmView.class);
			pi = PendingIntent.getActivity(this, ALARM_VIEW, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			am.set(AlarmManager.RTC_WAKEUP, today.getTimeInMillis(), pi);

			// 우리 디비 월0화1수2목3금4토5일6
			// calendar 월2화3수4목5금6토7일1
			Log.d("AlarmService",
					"MorningCall Set: " + daysArr[day] + "요일 "
							+ today.get(Calendar.DAY_OF_MONTH) + "일 "
							+ today.get(Calendar.HOUR_OF_DAY) + ":"
							+ today.get(Calendar.MINUTE));
		}
		dbA.close();
	}

	public void cancelMorningCall() {
		Intent intent = new Intent(this, AlarmView.class);
		pi = PendingIntent.getActivity(this, ALARM_VIEW, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		am.cancel(pi);
	}

	public void cancelNextClass() {
		Intent intent = new Intent(this, NotifyReceiver.class);
		PendingIntent notifySender = PendingIntent.getBroadcast(this,
				NOTIFY_RECEIVER, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		am.cancel(notifySender);
	}

	public void cancelTask() {
		Intent intent = new Intent(this, NotifyReceiver.class);
		PendingIntent taskSender = PendingIntent.getBroadcast(this,
				NOTIFY_TASK, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		am.cancel(taskSender);
	}

	public void setNextClassStartCall() {
		boolean set = prefs.getBoolean("alarm", false); // preference에서 alarm이
														// 체크 되어있는지 확인.
		cancelNextClass();

		if (!set) {
			Log.d("AlarmService", "ClassAlarm Canceled");
			return;
		}
		DBAdapter dbA = new DBAdapter(this); // db adapter를 선언.
		dbA.open(); // db adapter open

		Calendar today = Calendar.getInstance(); // calendar 선언 .오늘로.
		int day = today.get(Calendar.DAY_OF_WEEK) - 2; // 우리 디비에서는 calendar에서
														// 받아오는 날짜보다 2가작다.
		if (day < 0) // day가 0보다 작다면 day를 6으로 설정해서 일요일로 만드는것.
			day = 6;

		int past_minute = Integer.parseInt(prefs.getString("alarm_time", "5")); // prefs에서	// 받아온 // 알람타임그러니깐  몇분전  그//
																				// 분을//
																				// 받아온다.
		Time t_beforeTime = new Time(past_minute); // 그 분을 타임 함수로 만든다.
		AlarmTime t = null; // 알람타임 함수 선언.

		Time beforeTime = new Time(); // beforetime함수 선언.
		beforeTime.setHour(today.get(Calendar.HOUR_OF_DAY)); 						// beforeTime 함수에// 오늘의 시간을 넣어줌
		beforeTime.setMinute(today.get(Calendar.MINUTE)); 							// beforeTime 함수에 오늘의 			// 분을 넣어줌
		beforeTime.addTime(t_beforeTime); 													// beforeTime에 몇분전 time함수를 더해준다.

		t = dbA.getNextClassTime(day, beforeTime); 										// starttime을 time으로 가져온다.

		if (t != null) { 																					// 그 알람 타임이 널이 아니면. notification을 보낼수 있다는
			int diffday = t.getDay() - day; 											// 날의 차이 가져온.. nextClasstime의 요일을 가져오고, 오늘의 요일을 뺀다.
			if (diffday == 0) {															 // 날의 차이가 0 이면 오늘이면
				if (!t.isToday()) { 															// 만약 오늘이 아닌게 맞으면.
					diffday = 7; 															// 날의 차이를 7로.
				}
			} else if (diffday < 0) 														// 날의 차이가 0보다 작으면
				diffday = 7 + diffday; 																//

			Time time = t.subTime(new Time(0, past_minute)); // 타임에서 몇분전에서 선택한		// 분을 빼서 넣는다.

			today.add(Calendar.DAY_OF_MONTH, diffday); 							// 요일의 차이를 더한다.
			today.set(Calendar.HOUR_OF_DAY, time.getHour());								 // 타임을 시간을 셋
			today.set(Calendar.MINUTE, time.getMinute()); 													// 타임에서 분을 셋
			today.set(Calendar.SECOND, 0); 											// 초를 0으로 // 그러면 이시간에 notification을	// 보내는 거구나?
			Intent intentNext = new Intent(this, NotifyReceiver.class); 						// 인텐트 만들고
			intentNext.setAction("com.ajouroid.timetable.NOTIFY_CLASS");
			PendingIntent notifySender = PendingIntent.getBroadcast(this,
					NOTIFY_RECEIVER, intentNext, PendingIntent.FLAG_UPDATE_CURRENT); // pending intent를 broadcast로

			am.set(AlarmManager.RTC_WAKEUP, today.getTimeInMillis(), notifySender); // 알람매니져를 set한다.

			day = today.get(Calendar.DAY_OF_WEEK) - 2; // 우리 디비에서는 calendar에서
			// 받아오는 날짜보다 2가작다.
			if (day < 0) // day가 0보다 작다면 day를 6으로 설정해서 일요일로 만드는것.
			day = 6;
			Log.d("AlarmService", "ClassAlarm Set: " + daysArr[day] + "요일 "
					+ time.toString());

		}
		dbA.close();
	}
	public void setClassSilentCall()
	{
		boolean set = prefs.getBoolean("vibrate_mode", false);
		
		
		Intent intentSilent = new Intent(this, NotifyReceiver.class);
		intentSilent.setAction("com.ajouroid.timetable.NOTIFY_SILENT");
		PendingIntent notifySilent = PendingIntent.getBroadcast(this, NOTIFY_SILENT, intentSilent, PendingIntent.FLAG_UPDATE_CURRENT);
		am.cancel(notifySilent);

		if (!set)
		{
			Log.d("AlarmService", "Vibrate Mode Canceled");
			return;
		}
		
		DBAdapter dbA = new DBAdapter(this); // db adapter를 선언.
		dbA.open(); // db adapter open

		Calendar today = Calendar.getInstance(); // calendar 선언 .오늘로.
		int day = today.get(Calendar.DAY_OF_WEEK) - 2; // 우리 디비에서는 calendar에서  받아오는 날짜보다 2가작다.
		
		if (day < 0) // day가 0보다 작다면 day를 6으로 설정해서 일요일로 만드는것.
			day = 6;
		
		AlarmTime t = null; // 알람타임 함수 선언.

		Time currentTime = new Time(); 														// 현재시간함수 선언.
		currentTime.setHour(today.get(Calendar.HOUR_OF_DAY)); 						// 현재시간 함수에 현재의 시간을 넣어줌
		currentTime.setMinute(today.get(Calendar.MINUTE)); 							// 현재시간 함수에 현재 			// 분을 넣어줌
		
		t = dbA.getNextClassTime(day, currentTime); 										// starttime을 time으로 가져온다.

		if (t != null) { 																					// 그 알람 타임이 널이 아니면. notification을 보낼수 있다는
			int diffday = t.getDay() - day; 											// 날의 차이 가져온.. nextClasstime의 요일을 가져오고, 오늘의 요일을 뺀다.
			if (diffday == 0) {															 // 날의 차이가 0 이면 오늘이면
				if (!t.isToday()) { 															// 만약 오늘이 아닌게 맞으면.
					diffday = 7; 															// 날의 차이를 7로.
				}
			} else if (diffday < 0) 														// 날의 차이가 0보다 작으면
				diffday = 7 + diffday; 																//

			Time time = t.getStartTime();			 						

			today.add(Calendar.DAY_OF_MONTH, diffday); 							// 요일의 차이를 더한다.
			today.set(Calendar.HOUR_OF_DAY, time.getHour());								 // 타임을 시간을 셋
			today.set(Calendar.MINUTE, time.getMinute()); 													// 타임에서 분을 셋
			today.set(Calendar.SECOND, 0); 											// 초를 0으로 // 그러면 이시간에 notification을	// 보내는 거구나?
			
			am.set(AlarmManager.RTC_WAKEUP, today.getTimeInMillis(), notifySilent);
			day = today.get(Calendar.DAY_OF_WEEK) - 2; // 우리 디비에서는 calendar에서
			// 받아오는 날짜보다 2가작다.
			if (day < 0) // day가 0보다 작다면 day를 6으로 설정해서 일요일로 만드는것.
			day = 6;
			Log.d("AlarmService", "SilentMode Set: " + daysArr[day] + ", "
					+ time.toString());

		}
		dbA.close();
	}
	public void setTaskTimeCall() {

		DBAdapter dbA = new DBAdapter(this); // dbAdapter를 선언
		dbA.open(); // dbadapter를 오픈
		try {
			Task task = null; // taskTime이 설정 되어있는지 안되있는지 확인


			task = dbA.getTaskTime();
			
			if (task != null) {
				SimpleDateFormat format = new SimpleDateFormat(getResources()
						.getString(R.string.dateformat), Locale.US);
				Calendar today = Calendar.getInstance();
				
				Date startTaskTime = format.parse(task.getTaskDate()); // DB에서 가져온
																	// 날짜를//
																	// Date로
																	// parse
				if (startTaskTime.getTime() > System.currentTimeMillis()) {
					Time t = new Time(); // startTaskTime을 time함수로.
					t.setHour(startTaskTime.getHours()); // 이것도 디비에서 가져온 시간.
					t.setMinute(startTaskTime.getMinutes());

					Time time = t.subTime(new Time(1, 0)); // 타임에서 몇분전에서 선택한 분을
															// 빼서 넣는다.
					
					today.set(startTaskTime.getYear() + 1900, startTaskTime.getMonth(), startTaskTime.getDate());
					today.set(Calendar.HOUR_OF_DAY, time.getHour()); // 타임을
																		// 시간을//
																		// 셋
					today.set(Calendar.MINUTE, time.getMinute()); // 타임에서 분을 셋
					today.set(Calendar.SECOND, 0); // 초를 0으로

					Intent intentTask = new Intent(this, NotifyReceiver.class); // 인텐트//
																			// 만들고
					intentTask.setAction("com.ajouroid.timetable.NOTIFY_TASK");
					intentTask.putExtra("subject", task.getSubject());
					intentTask.putExtra("type", task.getType());
					intentTask.putExtra("title", task.getName());
					PendingIntent taskSender = PendingIntent.getBroadcast(this,
							NOTIFY_TASK, intentTask, PendingIntent.FLAG_UPDATE_CURRENT); // pending intent를//
														// broadcast로
					if (today.getTimeInMillis() > System.currentTimeMillis())
						am.set(AlarmManager.RTC_WAKEUP, today.getTimeInMillis(),
								taskSender); // 알람매니져를 set한다.

					
					Log.d("TaskService",
							"TaskAlarm Set: " + format.format(today.getTime()));
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		dbA.close();
	}

	public void setDayTaskCall() {

		DBAdapter dbA = new DBAdapter(this); // dbAdapter를 선언
		dbA.open(); // dbadapter를 오픈

		ArrayList<Task> taskList = dbA.getNextDayTasks();

		if (taskList.size() > 0) {
			Calendar date = Calendar.getInstance();
			date.set(Calendar.HOUR_OF_DAY, 0);					// 이부분 0으로 고칠것.
			date.set(Calendar.MINUTE, 0);
			date.set(Calendar.SECOND, 0);
			date.add(Calendar.DAY_OF_MONTH, 1);					// 

			Intent intent = new Intent(this, NotifyReceiver.class); // 인텐트// 만들고
			intent.setAction("com.ajouroid.timetable.NOTIFY_DAY_TASK");
			
			ArrayList<String> subjectList = new ArrayList<String>();
			ArrayList<String> titleList = new ArrayList<String>();
			
			for (int i=0; i<taskList.size(); i++)
			{
				subjectList.add(taskList.get(i).getSubject());
				titleList.add(taskList.get(i).getName());
			}
			
			intent.putStringArrayListExtra("subject", subjectList);
			intent.putStringArrayListExtra("title", titleList);
			PendingIntent taskNextSender = PendingIntent.getBroadcast(this,
					NOTIFY_DAY_TASK, intent, PendingIntent.FLAG_UPDATE_CURRENT); // pending intent를// broadcast로
			am.set(AlarmManager.RTC_WAKEUP, date.getTimeInMillis(), taskNextSender); // 알람매니져를
										
			SimpleDateFormat format = new SimpleDateFormat(getResources()
					.getString(R.string.dateformat), Locale.US);// set한다.
			Log.d("TaskService",
					"DayTaskAlarm Set: " + format.format(date.getTime()));
		}
		dbA.close();
	}

	public class AlarmBinder extends Binder //
	{
		public AlarmService getService() //
		{
			return AlarmService.this; //
		}
	}
}
