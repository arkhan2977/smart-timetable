package com.ajouroid.timetable;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RemoteViews;

public class TimeTableWidget extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		for (int i = 0; i < appWidgetIds.length; i++) {
			int widgetId = appWidgetIds[i];
			updateWidget(context, appWidgetManager, widgetId);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();

		if (action.equalsIgnoreCase("android.appwidget.action.APPWIDGET_UPDATE"))
		{
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			ComponentName cName = new ComponentName(context.getPackageName(), TimeTableWidget.class.getName());
			int[] ids = appWidgetManager.getAppWidgetIds(cName);
			
			this.onUpdate(context, appWidgetManager, ids);
		}
		else if (action.equalsIgnoreCase("com.ajouroid.timetable.WIDGET_UPDATE")) {
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(
					context, TimeTableWidget.class));
			
			this.onUpdate(context, appWidgetManager, ids);
		}
		else
		{
			super.onReceive(context, intent);
		}
	}

	public void updateWidget(Context context,
			AppWidgetManager appWidgetManager, int widgetId) {
		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent pIntent = PendingIntent
				.getActivity(context, 0, intent, 0);

		Display display = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		int displayWidth = display.getWidth();
		int displayHeight = display.getHeight();

		TimeTable tbl = new TimeTable(context);
		tbl.getWidgetImage(displayWidth, (int)(displayHeight * 0.9));

		RemoteViews remoteView = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);
		remoteView.setImageViewResource(R.id.widget_timetable, R.drawable.ajouroid);
		
		String abspath = "/sdcard/SmartTimeTable/widget/widget.png";
		remoteView.setImageViewUri(R.id.widget_timetable,
				Uri.parse("file://" + abspath));
		remoteView.setOnClickPendingIntent(R.id.widget_timetable, pIntent);

		appWidgetManager.updateAppWidget(widgetId, remoteView);
		
		Log.d("TimeTableAppWidget","Widget " + widgetId + " is Updating");
	}
	
}
