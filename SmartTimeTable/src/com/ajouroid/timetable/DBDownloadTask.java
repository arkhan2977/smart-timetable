package com.ajouroid.timetable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Enumeration;
import java.util.zip.*;

public class DBDownloadTask extends AsyncTask<Void, Integer, Void> {
	ProgressDialog Dialog;
	Context context;
	DBAdapterBus dbA;
	
	final int bufsize=8192;
	
	Resources r;
	
	String new_version;
	
	public DBDownloadTask(Context ctx)
	{
		context = ctx;
		
		r = ctx.getResources();
	}
	
	public void run(String version)
	{
		new_version = version;
		@SuppressWarnings("unused")
		AlertDialog alert_dialog = new AlertDialog.Builder(context)
		.setTitle(r.getString(R.string.dbdown_title))
		.setMessage(r.getString(R.string.dbdown_alert))
		.setPositiveButton(r.getString(R.string.ok), new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				DBDownloadTask.this.execute();
			}
		}).setNegativeButton(r.getString(R.string.cancel), new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{							
				dialog.dismiss();
			}
		}).show();
	}
	
	@Override
	protected void onPreExecute() {
		

		Dialog = new ProgressDialog(context);
		Dialog.setTitle(r.getString(R.string.dbdown_downloading));
		Dialog.setMessage(r.getString(R.string.dbdown_downMsg));
		Dialog.setIndeterminate(true);
		Dialog.setCancelable(false);
		Dialog.show();
		super.onPreExecute();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Void doInBackground(Void... params) {
		URL url;
		try {
			url = new URL("http://smart-timetable.googlecode.com/files/timetable_bus.zip");	
		
			HttpURLConnection uConn;
			
			uConn = (HttpURLConnection)url.openConnection();
			
	
			uConn.setConnectTimeout(60000);
			uConn.setReadTimeout(60000);
	
	
			int size = uConn.getContentLength();
			if (size < 0)
			{
				return null;
			}
	
			int progress=0;
	
			InputStream inStream;
			inStream = new BufferedInputStream(uConn.getInputStream());
			//InputStreamReader in = new InputStreamReader(inStream,"euc-kr");
			
			File f = new File("/data/data/com.ajouroid.timetable/databases/timetable_bus.zip");
			if (f.exists())
			{
				f.delete();
				f.createNewFile();
			}
			else
			{
				f.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(f);
	
			int n;
	
			byte[] buf = new byte[bufsize];
	
			while ((n = inStream.read(buf)) != -1)
			{	
				progress += n;
				publishProgress(progress, size);
				fos.write(buf,0,n);
			}			
			inStream.close();
			fos.flush();
			fos.close();
			
			ZipFile zf = new ZipFile("/data/data/com.ajouroid.timetable/databases/timetable_bus.zip");
			
			Enumeration e = zf.entries();
			while (e.hasMoreElements())
			{
				ZipEntry ze = (ZipEntry)e.nextElement();
				String entry = ze.getName();
				Log.d("SmartTimeTable", "Entry: " + entry);
				InputStream ins = zf.getInputStream(ze);
				f = new File("/data/data/com.ajouroid.timetable/databases/timetable_bus.db");
				//f = new File("/sdcard/database.db");
				if (f.exists())
				{
					f.delete();
					f.createNewFile();
				}
				else
				{
					f.createNewFile();
				}
				fos = new FileOutputStream(f);
				
				while ((n=ins.read(buf)) != -1)
				{
					fos.write(buf, 0, n);
				}
				fos.flush();
				fos.close();
				
				ins.close();
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		if (values[0] < values[1])
			Dialog.setMessage(r.getString(R.string.dbdown_downMsg) + "\n" + values[0]/1024 + "kb / " + values[1]/1024 + "kb");
		else
			Dialog.setMessage(r.getString(R.string.dbdown_installMsg));
		super.onProgressUpdate(values);
	}

	@Override
	protected void onPostExecute(Void result) {
		Dialog.dismiss();
		SharedPreferences.Editor ed = PreferenceManager.getDefaultSharedPreferences(context).edit();
		ed.putBoolean("db_complete", true);
		ed.putString("db_version", new_version);
		ed.commit();
		Intent intent = new Intent();
		intent.setAction("com.ajouroid.timetable.DOWNLOAD_COMPLETE");
		context.sendBroadcast(intent);
		super.onPostExecute(result);
	}
}
