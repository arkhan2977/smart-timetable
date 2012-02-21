package com.ajouroid.timetable.bus;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.ajouroid.timetable.R;
import com.ajouroid.timetable.R.string;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class VersionCheckTask extends AsyncTask<Void, Void, String> {

	final int bufsize = 8192;
	Context context;
	Resources r;
	ProgressDialog dialog;
	
	public VersionCheckTask(Context ctx)
	{
		context = ctx;
		r = context.getResources();
	}
	@Override
	protected String doInBackground(Void... params) {
		URL url;
		String version = null;

		try {
			url = new URL(
					"http://smart-timetable.googlecode.com/files/version.txt");

			HttpURLConnection uConn;

			uConn = (HttpURLConnection) url.openConnection();

			uConn.setConnectTimeout(60000);
			uConn.setReadTimeout(60000);

			int size = uConn.getContentLength();
			if (size < 0) {
				return null;
			}

			InputStream inStream;
			inStream = new BufferedInputStream(uConn.getInputStream());

			int n;

			byte[] buf = new byte[bufsize];

			n = inStream.read(buf);
			version = new String(buf, 0, n);

			inStream.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return version;
	}

	@Override
	protected void onPostExecute(String result) {
		dialog.dismiss();
		if (result != null)
		{
			int cur_version = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("db_version", "-1"));
			int new_version = Integer.parseInt(result);
			Log.d("Version Check", "Current Version: " + cur_version + ", New Version: " + new_version);
			if (cur_version < new_version)
			{
				DBDownloadTask downTask = new DBDownloadTask(context);
				downTask.run(result);
			}
			else
			{
				Toast.makeText(context, R.string.dbdown_newestDB, Toast.LENGTH_SHORT).show();
			}
		}
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		dialog = new ProgressDialog(context);
		dialog.setTitle(r.getString(R.string.dbdown_checking));
		dialog.setMessage(r.getString(R.string.dbdown_checkMsg));
		dialog.setIndeterminate(true);
		dialog.setCancelable(false);
		dialog.show();
		super.onPreExecute();
	}
}
