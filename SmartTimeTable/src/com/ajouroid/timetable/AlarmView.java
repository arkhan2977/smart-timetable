package com.ajouroid.timetable;

import java.io.IOException;
import java.util.Random;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//Alarm View
public class AlarmView extends Activity implements OnClickListener {

	MediaPlayer mp = null;
	String uri;
	Vibrator vibe;
	
	Button[][] btns;
	int[][] boxes;
	Button btn_snooze;
	TextView tv_curColor;
	TextView tv_remain;
	
	Button btn_left;
	Button btn_right;
	TextView tv_tapRemain;
	
	int targetColor;
	int remain;
	
	int tapRemain = 50;
	
	PowerManager.WakeLock sCpuWakeLock;
	
	boolean snooze = false;
	boolean running = true;
	int snoozeMinute;
	
	Resources r;
	
	final int[] colors = {0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00, 0xFFFF00FF, 0xFF00FFFF, 0xFFFFFFFF};
	final static int RED = 0xFFFF0000;
	final static int GREEN = 0xFF00FF00;
	final static int BLUE = 0xFF0000FF;
	final static int YELLOW = 0xFFFFFF00;
	final static int PURPLE = 0xFFFF00FF;
	final static int CYAN = 0xFF00FFFF;
	final static int WHITE = 0xFFFFFFFF;
	
	NotificationManager nm;
	
	final int NOTIFY_ID = 198781;
	final String TAG = "com.ajouroid.timetable.AlarmView";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);		
		r = getResources();
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		sCpuWakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "SmartTimeTable");
		sCpuWakeLock.acquire();
		
		//gameView = new AlarmGame(this);
		
		this.setContentView(R.layout.alarmview);
		
		btns = new Button[10][10];
		boxes = new int[10][10];
		StringBuilder sb;
		for (int i=0; i<10; i++)
		{
			for (int j=0; j<10; j++)
			{
				sb = new StringBuilder();
				sb.append("id/alarm_btn").append(i).append(j);
				btns[i][j] = (Button)findViewById(r.getIdentifier(sb.toString(), "id", this.getPackageName()));
				btns[i][j].setTag(i + " " + j);
				btns[i][j].setOnClickListener(this);
			}
		}
		tv_curColor = (TextView)findViewById(R.id.alarm_color);
		tv_remain = (TextView)findViewById(R.id.alarm_remain);
		btn_snooze = (Button)findViewById(R.id.alarm_snooze);
		
		
		btn_left = (Button)findViewById(R.id.alarm_left);
		btn_right = (Button)findViewById(R.id.alarm_right);
		tv_tapRemain = (TextView)findViewById(R.id.alarm_tapRemain);
		
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		uri = pref.getString("alarm_music", Settings.System.DEFAULT_RINGTONE_URI.toString());
		init();
		playAlarm();
	}
	
	@Override
	public void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
	}

	@Override
	protected void onResume() {
		btn_snooze.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (!snooze)
					goSnooze();
			}
			
		});
		
		OnClickListener tapListener = new OnClickListener() {

			public void onClick(View v) {
				tapRemain--;
				update();
				if (tapRemain <= 0)
					stopAlarm();
			}
			
		};
		btn_left.setOnClickListener(tapListener);
		btn_right.setOnClickListener(tapListener);
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		stopAlarm();
	}
	
	@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) { // 백 버튼
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH) { // 검색버튼
        }
        else if (event.getKeyCode() == KeyEvent.KEYCODE_HOME) {
        }
        return true;
    }
	
	public void init()
	{
		remain = 0;
		
		Random rand = new Random();
		
		int color = rand.nextInt(6);
		targetColor = colors[color];
		tv_curColor.setBackgroundColor(targetColor);
		for (int i=0; i<10; i++)
		{
			for (int j=0; j<10; j++)
			{
				color = rand.nextInt(6);
				btns[i][j].setBackgroundColor(colors[color]);
				btns[i][j].setVisibility(View.VISIBLE);
				boxes[i][j] = colors[color];
				if (colors[color] == targetColor)
					remain++;
			}
		}
		
		update();
	}
	
	public void update()
	{
		tv_remain.setText(remain + "개");
		tv_tapRemain.setText(tapRemain + "");
		if (snooze)
		{
			String timeStr = snoozeMinute / 60 + ":" + snoozeMinute%60;
			btn_snooze.setText(timeStr);
		}
		else
		{
			btn_snooze.setText("Snooze");
		}
	}

	public void playAlarm()
	{
		Notification notice = new Notification(R.drawable.alarmclockicon, "똑똑한시간표 모닝콜", System.currentTimeMillis());
		Intent i = new Intent(this, AlarmView.class);
		PendingIntent pendingintent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		notice.setLatestEventInfo(this, "똑똑한시간표 모닝콜", "일어나세요!", pendingintent);
		notice.flags += Notification.FLAG_ONGOING_EVENT;
		nm.notify(TAG, NOTIFY_ID, notice);
		
		mp = new MediaPlayer();
		try {
			vibe = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
			long[] pattern = {1000, 1500};
			vibe.vibrate(pattern, 0);
			mp.setDataSource(this, Uri.parse(uri));
			mp.setAudioStreamType(AudioManager.STREAM_ALARM);
			mp.setLooping(true);
			mp.prepare();
			mp.start();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void pauseAlarm()
	{
		if (mp != null)
		{
			mp.pause();
		}
		
		if(vibe != null)
			vibe.cancel();
	}
	
	public void goSnooze()
	{
		if (!snooze)
		{
			pauseAlarm();
			snooze = true;
			snoozeMinute=300;
			handler.sendEmptyMessage(1);
		}
	}
	
	Handler handler = new Handler() {
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			if (running)
			{
				if (snoozeMinute > 0)
				{
					snoozeMinute--;
					sendEmptyMessageDelayed(1,1000);
					update();
				}
				else
				{
					snooze=false;
					update();
					playAlarm();
				}
			}
		}
	};
	
	public void stopAlarm()
	{
		if (mp != null)
		{
			vibe.cancel();
			mp.stop();
			mp.release();
			mp = null;
			SmartTimeTable app = (SmartTimeTable)getApplication();
			app.morningCallService.setMorningCall();
			sCpuWakeLock.release();
		}
		running = false;
		nm.cancel(TAG, NOTIFY_ID);
		finish();
	}
	
	public void onClick(View v) {
		String[] pos = ((String)v.getTag()).split(" ");
		
		int i = Integer.parseInt(pos[0]);
		int j = Integer.parseInt(pos[1]);
		
		if (boxes[i][j] != 0)
		{
			if (boxes[i][j] == targetColor)
			{
				btns[i][j].setVisibility(View.INVISIBLE);
				boxes[i][j] = 0;
				remain--;
				
				if (remain <= 0)
					stopAlarm();
				update();
			}
			else
				init();
		}
	}
}
