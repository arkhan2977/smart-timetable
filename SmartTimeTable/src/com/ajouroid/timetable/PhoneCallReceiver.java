package com.ajouroid.timetable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneCallReceiver extends BroadcastReceiver {

	Context ctx;
	
	static int ID = 0;

	@Override
	public synchronized void onReceive(Context context, Intent intent) {
		ctx = context;
		SharedPreferences sPref = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		boolean reject = sPref.getBoolean("usereject", false);
		final String message = sPref.getString("message",
				"수업중입니다. 나중에 다시 연락드리겠습니다.");
		final String resttime = sPref.getString("resttime", "0:15");
		
		Log.d("PhoneCallReceiver", "Calling Message Received.");

		if (reject) {
				Log.d("PhoneCallReceiver", "Now Calling...");

				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				try {
					@SuppressWarnings("rawtypes")
					Class c = Class.forName(tm.getClass().getName());
					Method m = c.getDeclaredMethod("getITelephony");
					m.setAccessible(true);
					final com.android.internal.telephony.ITelephony telephonyService = (com.android.internal.telephony.ITelephony) m
							.invoke(tm);

					tm.listen(new PhoneStateListener() {
						@Override
						public void onCallStateChanged(int state,
								String incomingNumber) {
							Log.i("PhoneCallReceiver",
									"phone call state changed");
							if (state == TelephonyManager.CALL_STATE_RINGING) {

								Log.i("PhoneCallReceiver", "ringing: "
										+ incomingNumber);

								Calendar now = Calendar.getInstance();

								Time nowTime = new Time(now
										.get(Calendar.HOUR_OF_DAY), now
										.get(Calendar.MINUTE));

								int day = now.get(Calendar.DAY_OF_WEEK) - 2;
								if (day < 0)
									day = 6;

								DBAdapter dbA = new DBAdapter(ctx);
								dbA.open();
								
								// 지금이 수업시간이면 전화를 끊음
								if (dbA.isNowClassTime(day, nowTime, new Time(resttime)))
								{
									try {
										if (telephonyService.endCall())
										{
										NotificationManager nm = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
										Notification tNotice = new Notification(R.drawable.icon_rejectcall, "수업중 전화가 있습니다.", System.currentTimeMillis());
										
										String Title = "수업중 전화가 있습니다.";
										String Text = incomingNumber;
										
										Uri number = Uri.parse("tel:" + incomingNumber);
										Intent i = new Intent(Intent.ACTION_DIAL, number);
										PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
										tNotice.setLatestEventInfo(ctx, Title, Text, pendingIntent);
										
										tNotice.flags |= Notification.FLAG_AUTO_CANCEL;
										nm.notify("com.ajouroid.timetable.PhoneCallReceiver", ID++, tNotice);
										sendSMS(incomingNumber, message);
										}
									} catch (RemoteException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									Log.d("PhoneCallReceiver", "수업시간입니다.");
								}
								dbA.close();
							}

							super.onCallStateChanged(state, incomingNumber);
						}
					}, PhoneStateListener.LISTEN_CALL_STATE);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		}

	}

	private void sendSMS(String phoneNumber, String message) {
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, null, null);
	}

}
