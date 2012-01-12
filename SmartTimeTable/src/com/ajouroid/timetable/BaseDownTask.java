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
import org.apache.http.util.EncodingUtils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

class BaseDownTask extends AsyncTask<Void, Integer, Boolean>{
	ProgressDialog Dialog;
	BaseInfo b_info;
	String[] base_url;
	DBAdapterBus dbA;
	Context context;
	
	BaseDownTask(BaseInfo _b_info, String[] _base_url, Context ctx){
		b_info = _b_info;
		base_url = _base_url;
		context = ctx;
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();	
		dbA = new DBAdapterBus(context);
		dbA.open();
		dbA.initData();
		Dialog = new ProgressDialog(context);
		Dialog.setTitle("DB 다운로드중 ...");
		Dialog.setMessage("DB 구성을 위한 데이터를 다운로드 중입니다.\n수 분이 소요될 수 있습니다.");
		Dialog.setIndeterminate(true);
		Dialog.setCancelable(false);
		Dialog.show();
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		// TODO Auto-generated method stub
	
		int progress = (int)(((float)values[2] * 100) / (float)values[3]);
		
		String top_msg = values[0] + " / " + values[1];
		String mid_msg =  progress + "% (" + values[2] + "/" + values[3] + ")";
		
		if(values[4] == 0){
			Dialog.setTitle("다운로드중...");
			Dialog.setMessage(top_msg + "\n" + mid_msg);
		}
		else{
			Dialog.setTitle("DB구성중...");
			Dialog.setMessage(top_msg + "\n" + mid_msg);
		}
		super.onProgressUpdate(values);
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		// TODO Auto-generated method stub
		final int fixedsize = 1048576;
		final int bufsize = 8192;

		String str=new String();	
		String remainLine = null;
		byte[] buf = new byte[bufsize];
		//char[] cbuf = new char[8192];

		//Area,Route,Station,Route-Station
		if(base_url != null){
			for(int i=0; i<base_url.length; i++){
				str = "";
				try {
					// Create a URL for the desired page

					int size = -1;
					URL url = new URL(base_url[i]);
					HttpURLConnection uConn = (HttpURLConnection)url.openConnection();

					uConn.setConnectTimeout(60000);
					uConn.setReadTimeout(60000);


					size = uConn.getContentLength();
					if (size < 0)
					{
						return false;
					}

					ByteArrayBuffer bab = new ByteArrayBuffer(size);

					int progress=0;

					InputStream inStream = new BufferedInputStream(uConn.getInputStream());
					//InputStreamReader in = new InputStreamReader(inStream,"euc-kr");
					
					FileOutputStream fos = new FileOutputStream(new File("/mnt/sdcard/SmartTimeTable/busdata" + i + ".txt"));
					Writer out = new OutputStreamWriter(fos, "euc-kr");

					int n;


					while (true)
					{	
						if (size - progress < bufsize)
						{
							int lastBufSize = size-progress;
							
							Log.d("BaseDownTask", "File Size: " + size + ", Progress: " + progress);
							Log.d("BaseDownTask", "Last Buffer Size: " + lastBufSize);
							
							byte[] lastbuf = new byte[lastBufSize];
							n = inStream.read(lastbuf);
							progress += lastBufSize;
							publishProgress(i+1,base_url.length,progress,size, 0);
							bab.append(lastbuf, 0, lastBufSize);
							break;
						}			

						if((n = inStream.read(buf)) == -1){
							Log.d("Here", new String(buf));
							break;
						}
						progress += n;
						publishProgress(i+1,base_url.length,progress,size, 0);
						bab.append(buf, 0, n);
					}			

					inStream.close();
					inStream = null;

					publishProgress(i+1,base_url.length, progress, size, 1);
					
					byte[] result;
					result = bab.toByteArray();
					bab = null;

					int part = result.length / fixedsize;

					for (int j=0; j<part; j++)
					{
						Log.d("BaseDownTask", "File " + (i+1) + ", Inserting into Database ... " + (j+1) + "/" + (part+1));

						String partStr = EncodingUtils.getString(result, j * fixedsize, fixedsize, "euc-kr").trim();
						out.write(partStr);
						
						str += partStr;

						switch(i){
						case 0: remainLine = dbA.addAreaInfo(str); break;  // Area parsing
						case 1: remainLine = dbA.addRouteInfo(str); break;  // Route parsing
						case 2: remainLine = dbA.addStationInfo(str); break;  // Station parsing
						case 3: remainLine = dbA.addRSInfo(str); break; // Route-station parsing
						}
						if (remainLine != null)
							str = remainLine;
						else
						{
							str = null;
							str = "";
						}
					}

					if (fixedsize * part < result.length)
					{
						Log.d("BaseDownTask", "File " + (i+1) + ", Inserting into Database ... " + (part+1) + "/" + (part+1));
						String partStr = EncodingUtils.getString(result, part * fixedsize, result.length - part*fixedsize, "euc-kr").trim();
						out.write(partStr);
						str += partStr;
						switch(i){
						case 0: remainLine = dbA.addAreaInfo(str); break;  // Area parsing
						case 1: remainLine = dbA.addRouteInfo(str); break;  // Route parsing
						case 2: remainLine = dbA.addStationInfo(str); break;  // Station parsing
						case 3: remainLine = dbA.addRSInfo(str); break; // Route-station parsing
						}
						
						if (remainLine != null)
							Log.d("BaseDownTask", "Remained String: " + remainLine);
					}
					out.flush();
					out.close();
					result = null;
					str = null;
					remainLine = null;

				} catch (MalformedURLException e) {	
					Log.d("BaseDownTask", "Error: " + e.toString());
					return false;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}						
			}	
		}
		return true;
	}
	
	@SuppressWarnings("unused")
	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);			
		
		dbA.close();
		
		if(!result){
			AlertDialog error_dialog = new AlertDialog.Builder(context)
			.setTitle("Error!")
			.setMessage("DB 구성중 에러가 발생하였습니다. \n다시 시도해 주십시오.")
			.setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();					
				}
			}).show();
		}
		else{
			SharedPreferences.Editor ed = PreferenceManager.getDefaultSharedPreferences(context).edit();
			ed.putBoolean("db_complete", true);
			ed.putString("db_version", b_info.getAreaversion());
			ed.commit();
			Intent intent = new Intent();
			intent.setAction("com.ajouroid.timetable.DOWNLOAD_COMPLETE");
			context.sendBroadcast(intent);
		}
		Dialog.dismiss();
	}	
}
