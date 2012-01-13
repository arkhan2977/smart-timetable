package com.ajouroid.timetable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.util.ByteArrayBuffer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class DBDownloadTask extends AsyncTask<String, Integer, Void> {
	ProgressDialog Dialog;
	Context context;
	DBAdapterBus dbA;
	
	final int bufsize=8192;
	
	public DBDownloadTask(Context ctx)
	{
		context = ctx;
	}
	
	@Override
	protected void onPreExecute() {
		
		Dialog = new ProgressDialog(context);
		Dialog.setTitle("DB 다운로드중 ...");
		Dialog.setMessage("GBus 데이터베이스 파일을 다운로드중입니다...");
		Dialog.setIndeterminate(true);
		Dialog.setCancelable(false);
		Dialog.show();
		
		super.onPreExecute();
	}
	
	@Override
	protected Void doInBackground(String... params) {
		URL url;
		try {
			url = new URL("http://smart-timetable.googlecode.com/files/timetable_bus.db");	
		
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
			
			File f = new File("/data/data/com.ajouroid.timetable/databases/timetable_bus.db");
			File f2 = new File("/sdcard/bus.db");
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
			FileOutputStream fos2 = new FileOutputStream(f2);
	
			int n;
	
			byte[] buf = new byte[bufsize];
	
			while ((n = inStream.read(buf)) != -1)
			{	
				progress += n;
				publishProgress(progress, size);
				fos.write(buf,0,n);
				fos2.write(buf,0,n);
			}			
			inStream.close();
			fos.flush();
			fos.close();
			fos2.flush();
			fos2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		Dialog.setMessage("GBus 데이터베이스 파일을 다운로드중입니다...\n" + values[0]/1024 + "kb / " + values[1]/1024 + "kb");
		super.onProgressUpdate(values);
	}

	@Override
	protected void onPostExecute(Void result) {
		Dialog.dismiss();
		SharedPreferences.Editor ed = PreferenceManager.getDefaultSharedPreferences(context).edit();
		ed.putBoolean("db_complete", true);
		ed.putString("db_version", "1");
		ed.commit();
		Intent intent = new Intent();
		intent.setAction("com.ajouroid.timetable.DOWNLOAD_COMPLETE");
		context.sendBroadcast(intent);
		super.onPostExecute(result);
	}
}
