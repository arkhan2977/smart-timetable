package com.ajouroid.timetable;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

@Deprecated
class BaseCheckTask extends AsyncTask<Void, Integer, Boolean>{

	String key;
	String TEST_KEY = "1234567890";
	String KEY= "O904OQN755V3irRmS5Hmqux3lB/xFZVw6b7Pb7RORO9kElznBFFIj3Kn4xkDCKOuHaT97ceTxM1hchELb6qwyA==";
	String baseurl = "http://openapi.gbis.go.kr/ws/rest/baseinfoservice";
	URL requestURL;
	BaseInfo b_info;
	int statusCode;
	ProgressDialog Dialog;
	String[] base_url;
	
	Context context;
	
	DBAdapterBus dbA;
	
	//http://openapi.gbis.go.kr/ws/rest/baseinfoservice?serviceKey=ZfCG/fCvFJnpaRPKfkGhTh+D0XBBXvumXKSu7h9pPb28X+21MHEZdi7s2yQYayc9h/cvXZQZuCms9MuFKA9GrQ==
	
	BaseCheckTask(Context ctx)
	{
		context = ctx;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {		
		
		super.onProgressUpdate(values);

	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		b_info = new BaseInfo();
		
		dbA = new DBAdapterBus(context);
		dbA.open();
		
		Dialog = new ProgressDialog(context);
		Dialog.setTitle("기반정보 확인중...");
		Dialog.setMessage("기반정보를 확인중 입니다. \n잠시만 기다려 주세요.");
		Dialog.setIndeterminate(true);
		Dialog.setCancelable(false);
		Dialog.show();
		
	}
	@Override
	protected Boolean doInBackground(Void... params) {
		// TODO Auto-generated method stub
		try {
			key = URLEncoder.encode(Keyring.BUS_KEY, "UTF-8");
			//key = URLEncoder.encode(KEY, "UTF-8");
			XmlPullParserFactory baseparser = XmlPullParserFactory.newInstance();
			baseparser.setNamespaceAware(true);
			XmlPullParser xpp = baseparser.newPullParser();

			String urlStr = baseurl + "?serviceKey="+key;
			
			Log.d("SmartTimeTable", "Requesting Base Information...");
			Log.d("SmartTimeTable", "URL: " + urlStr);
			requestURL = new URL(urlStr);
			InputStream input = requestURL.openStream();
			xpp.setInput(input,"UTF-8");

			int parserEvent = xpp.getEventType();
			parserEvent=xpp.next();//파싱한  자료에서 다음 라인으로 이동 
			boolean check = true;

			while(parserEvent != XmlPullParser.END_DOCUMENT){
				if(!check){break;}

				switch(parserEvent) {
				case XmlPullParser.END_TAG:			
					break;
				case XmlPullParser.START_TAG:
					if(xpp.getName().compareToIgnoreCase("returnCode")==0) //<returnCode> 인 경우.
					{
						xpp.next();
						String tempCode = xpp.getText();
						statusCode = Integer.parseInt(tempCode);
						xpp.next();
						check = false;
					}
					if(xpp.getName().compareToIgnoreCase("resultCode")==0) //<returnCode> 인 경우.
					{
						xpp.next();
						String tempCode = xpp.getText();
						statusCode = Integer.parseInt(tempCode);
						xpp.next();
					}
					if(xpp.getName().compareToIgnoreCase("areaDownloadUrl") == 0){
						xpp.next();
						b_info.setAreadownloadurl(xpp.getText());
						xpp.next();
					}
					if(xpp.getName().compareToIgnoreCase("routeDownloadUrl") == 0){
						xpp.next();
						b_info.setRoutedownloadurl(xpp.getText());
						xpp.next();
					}
					if(xpp.getName().compareToIgnoreCase("stationDownloadUrl") == 0){
						xpp.next();
						b_info.setStationdownloadurl(xpp.getText());
						xpp.next();
					}
					if(xpp.getName().compareToIgnoreCase("routeStationDownloadUrl") == 0){
						xpp.next();
						b_info.setRoutestationdownloadurl(xpp.getText());
						xpp.next();
					}
					if(xpp.getName().compareToIgnoreCase("areaVersion") == 0){
						xpp.next();
						b_info.setAreaversion(xpp.getText());			
						xpp.next();
					}
					if(xpp.getName().compareToIgnoreCase("routeVersion") == 0){
						xpp.next();
						b_info.setRouteversion(xpp.getText());
						xpp.next();
					}
					if(xpp.getName().compareToIgnoreCase("stationVersion") == 0){
						xpp.next();
						b_info.setStationversion(xpp.getText());
						xpp.next();
					}	
					if(xpp.getName().compareToIgnoreCase("routeStationVersion") == 0){
						xpp.next();
						b_info.setRoutestationversion(xpp.getText());
						xpp.next();
					}
				}
				parserEvent = xpp.next(); //다음 태그를 읽어 들입니다.
			}
			//return null; //완료되면 arrayList를 리턴합니다.			

		} catch (Exception e) {
			// TODO Auto-generated catch block
			return false;
		}
		Log.d("BaseCheckTask","Downloading Base Information completed.");
		return checkXml();			
	}

	public boolean checkXml(){			
		
		if(statusCode != 0){
			//xml 에러의 경우
			switch(statusCode){			
			case 20:
				statusCode = 9;
				break;
			case 21:
				statusCode = 10;
				break;
			case 22:
				statusCode = 11;
				break;
			case 23:
				statusCode = 12;
				break;
			case 30:
				statusCode = 13;
				break;
			case 31:
				statusCode = 14;
				break;
			case 32:
				statusCode = 15;
				break;
			case 99:
				statusCode = 16;
				break;
			default:
				statusCode = 0;
				break;
			}			
			//error code에 해당하는 메시지를 띄운다.
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unused")
	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);			
		Dialog.dismiss();
		int index = dbA.getAreainfo();
		String ver = PreferenceManager.getDefaultSharedPreferences(context).getString("db_version", "-1");
		dbA.close();
		
		if(!result){
			String[] ErrorMsg = context.getResources().getStringArray(R.array.errorCode);

			AlertDialog error_dialog = new AlertDialog.Builder(context)
			.setTitle("Error!")
			.setMessage(ErrorMsg[statusCode] + "\n다시 시도해 주십시오.")
			.setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			}).show();
		}
		else{
			//새로운 asynctask 실행				
			
			Dialog = new ProgressDialog(context);
			
			
			Dialog.setTitle("DB version 검색중...");
			Dialog.setMessage("DB version을 검색중입니다...");
			Dialog.setIndeterminate(true);
			Dialog.setCancelable(true);
			Dialog.show();
			
			if(index == 0 || !PreferenceManager.getDefaultSharedPreferences(context).getBoolean("db_complete", false)) // DB가 없는 경우.
			{
				Dialog.dismiss();
				AlertDialog alert_dialog = new AlertDialog.Builder(context)
				.setTitle("DB가 존재하지 않습니다!")
				.setMessage("버스기반정보를 구성합니다.\nWi-Fi에서 다운로드를 권장합니다.(Size : 약10MB)" +
						"\n설치를 원하지 않으면 취소를 클릭하시오.\n(단, DB미설치시 버스정보 이용불가.)")
				.setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						base_url = new String[4];
						base_url[0] = b_info.getAreadownloadurl();
						base_url[1] = b_info.getRoutedownloadurl();
						base_url[2] = b_info.getStationdownloadurl();
						base_url[3] = b_info.getRoutestationdownloadurl();
						dialog.dismiss();
						BaseDownTask down_task = new BaseDownTask(b_info,base_url, context);
						down_task.execute();
					}
				}).setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{							
						dialog.dismiss();
					}
				}).show();
			}
			else// DB가 이미 있는 경우
			{				
				Dialog.dismiss();
				Log.d("BaseCheckTask","Comparing Version : " + ver + "/" + b_info.getAreaversion());
				
				if (b_info == null)
				{
					Toast.makeText(context, "기반정보 다운로드에 실패했습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
				}
				else if(ver.compareToIgnoreCase(b_info.getAreaversion()) != 0){
					AlertDialog alert_dialog = new AlertDialog.Builder(context)
					.setTitle("최신 DB 업데이트.")
					.setMessage("버스기반정보를 업데이트 합니다.\nWi-Fi에서 다운로드를 권장합니다.(Size : 약10MB)" +
							"\n설치를 원하지 않으면 취소를 클릭하시오.\n(단, 제공정보가 부정확할 수 있습니다.")
					.setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							base_url = new String[4];
							base_url[0] = b_info.getAreadownloadurl();
							base_url[1] = b_info.getRoutedownloadurl();
							base_url[2] = b_info.getStationdownloadurl();
							base_url[3] = b_info.getRoutestationdownloadurl();
							dialog.dismiss();
							BaseDownTask down_task = new BaseDownTask(b_info,base_url, context);
							down_task.execute();
						}
					}).setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{							
							dialog.dismiss();
						}
					}).show();					
				}
				else{
					Toast.makeText(context, "DB가 최신입니다.", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}			
}