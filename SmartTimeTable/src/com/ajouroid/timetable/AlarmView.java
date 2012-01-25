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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

//Alarm View
public class AlarmView extends Activity {

	MediaPlayer mp = null;
	String uri;
	Vibrator vibe;
	AlarmGame gameView;
	
	PowerManager.WakeLock sCpuWakeLock;
	
	boolean snooze = false;
	boolean running = true;
	int snoozeMinute;
	
	Resources r;
	
	NotificationManager nm;
	
	final int NOTIFY_ID = 198781;
	final String TAG = "com.ajouroid.timetable.AlarmView";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		r = getResources();
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		sCpuWakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "SmartTimeTable");
		sCpuWakeLock.acquire();
		
		gameView = new AlarmGame(this);
		
		this.setContentView(gameView);
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		uri = pref.getString("alarm_music", Settings.System.DEFAULT_RINGTONE_URI.toString());
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
		// TODO Auto-generated method stub
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
					gameView.invalidate();
				}
				else
				{
					snooze=false;
					gameView.invalidate();
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
	
	public class AlarmGame extends View
	{
		Context ctx;
		Box[][] box;
		
		int width;
		int height;
		
		int boxwidth;
		int boxheight;
		
		int targetColor;
		
		int pX;
		int pY;
		
		int remain;
		
		final int[] colors = {0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00, 0xFFFF00FF, 0xFF00FFFF, 0xFFFFFFFF};
		final static int RED = 0xFFFF0000;
		final static int GREEN = 0xFF00FF00;
		final static int BLUE = 0xFF0000FF;
		final static int YELLOW = 0xFFFFFF00;
		final static int PURPLE = 0xFFFF00FF;
		final static int CYAN = 0xFF00FFFF;
		final static int WHITE = 0xFFFFFFFF;
		
		class Box
		{
			int color;
			boolean checked = false;
			
			public Box(int _color)
			{
				color = _color;
			}
			
			public boolean isChecked()
			{
				return checked;
			}
			
			public int check()
			{
				checked = true;
				return color;
			}
			
			public int getColor()
			{
				return color;
			}
			
			public void init(int _color)
			{
				color = _color;
				checked = false;
			}
		}
		
		public AlarmGame(Context context)
		{
			super(context);
			ctx = context;
			
			init();
		}
		
		public void init()
		{
			box = new Box[10][10];
			remain = 0;
			
			Random rand = new Random();
			
			int color = rand.nextInt(6);
			targetColor = colors[color];
			
			for (int i=0; i<10; i++)
			{
				for (int j=0; j<10; j++)
				{
					color = rand.nextInt(6);
					box[i][j] = new Box(colors[color]);
					
					if (colors[color] == targetColor)
						remain++;
				}
			}

			invalidate();
		}
		
		public AlarmGame(Context context, AttributeSet attrs) {
			super(context, attrs);
			ctx=context;
		}
		
		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			width = measure(widthMeasureSpec);
			height = measure(heightMeasureSpec);
			
			boxwidth = (width - 50) /10;
			boxheight = boxwidth;
			
			top = 120;
			left = 0;
			right = (boxwidth + 5) * 10;
			bottom = 120 + 10 * (boxheight + 5);

			setMeasuredDimension(width, height);
			// TODO Auto-generated method stub
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}

		private int measure(int spec) {
			int mode = MeasureSpec.getMode(spec);
			int size = MeasureSpec.getSize(spec);

			int result;

			if (mode == MeasureSpec.UNSPECIFIED)
				result = 200;
			else
				result = size;

			return result;
		}

		
		int top;
		int left;
		int right;
		int bottom;
		
		int snoozeX;
		int snoozeY;
		int snoozeRight;
		int snoozeBottom;
		
		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			Paint fillPaint = new Paint();
			int x;
			int y;
			
			fillPaint.setColor(Color.BLACK);
			canvas.drawRect(new Rect(0,0,width,height), fillPaint);
			
			fillPaint.setColor(targetColor);
			canvas.drawRoundRect(new RectF(0,50,width, 100), 8, 8, fillPaint);
			
			for (int i=0; i<10; i++)
			{
				for (int j=0; j<10; j++)
				{
					x = left + i * (boxwidth + 5);
					y = top + j * (boxheight + 5);
					
					fillPaint.setColor(box[i][j].getColor());
					
					if (!box[i][j].isChecked())
						canvas.drawRoundRect(new RectF(x,y,x+boxwidth, y+boxheight), 8, 8, fillPaint);
				}
			}
			
			Paint font = new Paint();
			font.setTextSize(24);
			font.setColor(Color.WHITE);
			canvas.drawText(remain + r.getString(R.string.alarmview_remain), 50, bottom + 100, font);
			
			fillPaint.setColor(Color.WHITE);
			fillPaint.setAlpha(0xFF);
			snoozeX = right-120;
			snoozeY = bottom+50;
			snoozeRight = right-20;
			snoozeBottom = bottom+100;
			canvas.drawRoundRect(new RectF(snoozeX,snoozeY,snoozeRight, snoozeBottom), 8, 8, fillPaint);
			font.setColor(Color.BLACK);
			font.setTextAlign(Align.CENTER);
			if (!snooze)
				canvas.drawText(r.getString(R.string.alarmview_snooze), snoozeX + 50, snoozeBottom-15, font);
			else
			{
				String timeStr = snoozeMinute / 60 + ":" + snoozeMinute%60;
				canvas.drawText(timeStr, snoozeX + 50, snoozeBottom-10, font);
			}
				
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {

			float x = event.getX();
			float y = event.getY();
			calculatePosition(x, y);
			
			if (pX > -1 && pY > -1)
			{
				if (!box[pX][pY].isChecked())
				{
					if (box[pX][pY].getColor() == targetColor)
					{
						box[pX][pY].check();
						remain--;
						if (remain == 0)
						{
							stopAlarm();
						}
						invalidate();
					}
					else
					{
						init();
					}
				}
			}
			
			else
			{
				if ((x>=snoozeX && x<=snoozeRight) && (y>=snoozeY && y<=snoozeBottom))
				{
					goSnooze();
					Toast.makeText(ctx, r.getString(R.string.alarmview_snoozeMsg), Toast.LENGTH_SHORT).show();
				}
			}
			
			return super.onTouchEvent(event);
		}
		
		public void calculatePosition(float x, float y)
		{
			pX = -1;
			pY = -1;
			
			if (y>=top && y <= bottom)
			{
				pX = (int)(x / (boxwidth + 5));
				pY = (int)((y - 120) / (boxheight + 5));
			}
		}
	}
}
