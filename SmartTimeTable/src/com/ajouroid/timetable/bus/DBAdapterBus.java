package com.ajouroid.timetable.bus;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

public class DBAdapterBus {

	private SQLiteDatabase mDb; // 데이터베이스를 저장
	
	private final Context mCtx;

	public static final int TYPE_ASSIGNMENT = 0;
	public static final int TYPE_TEST = 1;
	public static final int TYPE_EXTRA = 2;
	public static final int TYPE_ETC = 3;

	
	
	public DBAdapterBus(Context ctx) {
		this.mCtx = ctx;
	}

	public boolean open() throws SQLException {
		
		try{
			mDb = SQLiteDatabase.openDatabase("/data/data/com.ajouroid.timetable/databases/timetable_bus.db", null, SQLiteDatabase.OPEN_READWRITE|SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		} catch(SQLiteException e)
		{
			e.printStackTrace();
			return false;
		}
		
		if (!mDb.isDbLockedByOtherThreads())
		{
			return true;
		}
		else
			return false;
	}

	public void close() {
		//mDbHelper.close();
		if (mDb != null && mDb.isOpen())
			mDb.close();
	}
	
	public void initData()
	{
		mDb.delete("version", null, null);
		mDb.delete("area", null, null);
		mDb.delete("route", null, null);
		mDb.delete("station", null, null);
		mDb.delete("routestation", null, null);
		mDb.delete("favorite", null, null);
		
		Editor edit = PreferenceManager.getDefaultSharedPreferences(mCtx).edit();
		edit.remove("db_complete");
		edit.remove("db_version");
		edit.commit();
	}

	public int getAreainfo(){

		Cursor c = mDb.rawQuery("select count(*) from routestation", null);
		c.moveToFirst();
		int count = c.getInt(0);
		c.close();

		return count;
	}
	

	public String addAreaInfo(String str){
		String remain = null;
		String[] splitLine;
		String[] splitcolumn;
		ContentValues initialValues = new ContentValues();

		splitLine = str.split("\\^");
		if(splitLine != null){
			try {
				mDb.beginTransaction();
				for(int i=1; i<splitLine.length; i++){
					splitLine[i] = splitLine[i].replaceAll(" ", "");
					splitLine[i] = splitLine[i].replaceAll("\n", "");
					splitcolumn = splitLine[i].split("\\|"); 

					if(splitcolumn.length != 3){
						remain = splitLine[i];
						break;
					}	

					int c_id = Integer.parseInt(splitcolumn[0]);
					int a_id = Integer.parseInt(splitcolumn[1]);

					initialValues.clear();
					initialValues.put("CENTER_ID", c_id);
					initialValues.put("AREA_ID", a_id);
					initialValues.put("AREA_NAME", splitcolumn[2]);

					mDb.insert("area", null, initialValues);		

				}			
				mDb.setTransactionSuccessful();
			} finally {
				mDb.endTransaction();
			}
		}
		return remain;
	}

	public String addRouteInfo(String str){
		String remain = null;
		String[] splitLine;
		String[] splitcolumn;
		ContentValues initialValues = new ContentValues();
		splitLine = str.split("\\^");
		if(splitLine != null){
			try {
				mDb.beginTransaction();
				for(int i=1; i<splitLine.length; i++){
					splitLine[i] = splitLine[i].replaceAll(" ", "");
					splitLine[i] = splitLine[i].replaceAll("\n", "");
					splitcolumn = splitLine[i].split("\\|");
					
					if(splitcolumn.length != 20){
						remain = splitLine[i];
						break;
					}	
					
					initialValues.clear();
					initialValues.put("ROUTE_ID", splitcolumn[0]);
					initialValues.put("ROUTE_NM", splitcolumn[1]);
					initialValues.put("ROUTE_TP", splitcolumn[2]);
					initialValues.put("ST_STA_ID", splitcolumn[3]);
					initialValues.put("ST_STA_NM", splitcolumn[4]);
					initialValues.put("ST_STA_NO", splitcolumn[5]);
					initialValues.put("ED_STA_ID", splitcolumn[6]);
					initialValues.put("ED_STA_NM", splitcolumn[7]);
					initialValues.put("ED_STA_NO", splitcolumn[8]);
					initialValues.put("UP_FIRST_TIME", splitcolumn[9]);
					initialValues.put("UP_LAST_TIME", splitcolumn[10]);
					initialValues.put("DOWN_FIRST_TIME", splitcolumn[11]);
					initialValues.put("DOWN_LAST_TIME", splitcolumn[12]);
					initialValues.put("PEEK_ALLOC", splitcolumn[13]);
					initialValues.put("NPEEK_ALLOC", splitcolumn[14]);
					initialValues.put("COMPANY_ID", splitcolumn[15]);
					initialValues.put("COMPANY_NM", splitcolumn[16]);
					initialValues.put("TEL_NO", splitcolumn[17]);
					initialValues.put("REGION_NAME", splitcolumn[18]);
					initialValues.put("DISTRICT_CD", splitcolumn[19]);

					mDb.insert("route", null, initialValues);		

				}			
				mDb.setTransactionSuccessful();
			} finally {
				mDb.endTransaction();
			}
		}	
		return remain;
	}

	public String addStationInfo(String str){

		String remain = null;
		String[] splitLine;
		String[] splitcolumn;
		ContentValues initialValues = new ContentValues();
		splitLine = str.split("\\^");
		if(splitLine != null){
			try {
				mDb.beginTransaction();
				for(int i=1; i<splitLine.length; i++){
					splitLine[i] = splitLine[i].replaceAll(" ", "");
					splitLine[i] = splitLine[i].replaceAll("\n", "");
					splitcolumn = splitLine[i].split("\\|");				
					
					
					if(splitcolumn.length != 9){
						remain = splitLine[i];
						break;
					}	

					initialValues.clear();
					initialValues.put("STATION_ID", splitcolumn[0]);
					initialValues.put("STATION_NM", splitcolumn[1]);
					initialValues.put("CENTER_ID", splitcolumn[2]);
					initialValues.put("CENTER_YN", splitcolumn[3]);
					initialValues.put("LNG", splitcolumn[4]);
					initialValues.put("LAT", splitcolumn[5]);
					initialValues.put("REGION_NAME", splitcolumn[6]);
					initialValues.put("STATION_NO", splitcolumn[7]);
					initialValues.put("DISTRICT_CD", splitcolumn[8]);
					mDb.insert("station", null, initialValues);	
				}
				mDb.setTransactionSuccessful();
			} finally {
				mDb.endTransaction();
			}
		}
		return remain;
	}

	public String addRSInfo(String str){
		String remain = null;
		String[] splitLine;
		String[] splitcolumn;
		ContentValues initialValues = new ContentValues();
		splitLine = str.split("\\^");
		if(splitLine != null){
			mDb.beginTransaction();
			try {
				for(int i=1; i<splitLine.length; i++){
					splitLine[i] = splitLine[i].replaceAll(" ", "");
					splitLine[i] = splitLine[i].replaceAll("\n", "");
					splitcolumn = splitLine[i].split("\\|");
					
					if(splitcolumn.length != 6){
						remain = splitLine[i];
						break;
					}	

					initialValues.clear();
					initialValues.put("ROUTE_ID", splitcolumn[0]);
					initialValues.put("STATION_ID", splitcolumn[1]);
					initialValues.put("UPDOWN", splitcolumn[2]);
					initialValues.put("STA_ORDER", splitcolumn[3]);
					initialValues.put("ROUTE_NM", splitcolumn[4]);
					initialValues.put("STATION_NM", splitcolumn[5]);
					
					mDb.insert("routestation", null, initialValues);
				}
				mDb.setTransactionSuccessful();
			} finally {
				mDb.endTransaction();
			}
		}	
		return remain;
	}
	
	public ArrayList<BusStopInfo> findNearStops(double latitude, double longitude)
	{
		ArrayList<BusStopInfo> stopList = new ArrayList<BusStopInfo>();
		
		Cursor c;
		
		int lat_hour = (int)latitude;
		int lat_min = (int)((latitude - lat_hour) * 60f);
		double lat_sec = (((latitude - lat_hour) * 60) - lat_min) * 60;
		
		int lng_hour = (int)longitude;
		int lng_min = (int)((longitude - lng_hour) * 60f);
		double lng_sec = (((longitude - lng_hour) * 60) - lng_min) * 60;
		
		final int latDistanceSec = 700 / 25;
		final int lngDistanceSec = 700 / 31;
		
		lat_sec -= latDistanceSec;
		lng_sec -= lngDistanceSec;
		
		if (lat_sec < 0)
		{
			lat_sec = 60 + lat_sec;
			lat_min--;
			if (lat_min < 0)
			{
				lat_min = 60 + lat_min;
				lat_hour--;
			}
		}
		
		if (lng_sec < 0)
		{
			lng_sec = 60 + lng_sec;
			lng_min--;
			if (lng_min < 0)
			{
				lng_min = 60 + lng_min;
				lng_hour--;
			}
		}
		
		// 범위
		double lessLat =lat_hour + (double)lat_min/60 + lat_sec/60/60;
		double lessLong =lng_hour + (double)lng_min/60 + lng_sec/60/60;
		
		lat_hour = (int)latitude;
		lat_min = (int)((latitude - lat_hour) * 60f);
		lat_sec = (((latitude - lat_hour) * 60) - lat_min) * 60;
		
		lng_hour = (int)longitude;
		lng_min = (int)((longitude - lng_hour) * 60f);
		lng_sec = (((longitude - lng_hour) * 60) - lng_min) * 60;
		
		lat_sec += latDistanceSec;
		lng_sec += lngDistanceSec;
		
		if (lat_sec > 60)
		{
			lat_sec = lat_sec - 60;
			lat_min++;
			if (lat_min > 60)
			{
				lat_min = lat_min - 60;
				lat_hour++;
			}
		}
		
		if (lng_sec > 60)
		{
			lng_sec = lng_sec - 60;
			lng_min++;
			if (lng_min > 60)
			{
				lng_min = lng_min - 60;
				lng_hour++;
			}
		}
		
		
		double bigLat =lat_hour + (double)lat_min/60 + lat_sec/60/60;
		double bigLong =lng_hour + (double)lng_min/60 + lng_sec/60/60;
		

		String sql = "SELECT * FROM station " +
				"WHERE (LAT > '" + lessLat + "' AND LAT < '" + bigLat + "' AND LNG > '" + lessLong + "' AND LNG < '" + bigLong + "')";
		
		Log.d("Bus Database", sql);
		
		c = mDb.rawQuery(sql, null);
		
		int iId = c.getColumnIndex("STATION_ID");
		int iName = c.getColumnIndex("STATION_NM");
		int iLng = c.getColumnIndex("LNG");
		int iLat = c.getColumnIndex("LAT");
		int iNum = c.getColumnIndex("STATION_NO");
		int iRegion = c.getColumnIndex("REGION_NAME");
		
		while (c.moveToNext())
		{
			double s_lat = c.getDouble(iLat);
			double s_lng = c.getDouble(iLng);
			int dis = calc_distance(latitude,longitude,s_lat,s_lng);
			BusStopInfo val = new BusStopInfo(c.getString(iId), c.getString(iName), c.getString(iRegion), s_lat, s_lng, c.getInt(iNum),dis);
			
			stopList.add(val);
		}
		c.close();
		return stopList;
	}
	
	
	// 두 지점을 통과하는 버스 찾기
	public Boolean findBus(String sp_route_id, String dest_bs_id )
	{		
		String sql = "select ROUTE_ID, STATION_ID from routestation where ROUTE_ID = '" + sp_route_id+ "' AND STATION_ID = '" + dest_bs_id  + "'";
		Log.d("Bus Database", sql);
		Cursor c = mDb.rawQuery(sql, null);
		
		if(c.getCount() > 0){
			return true;
		}
		else{
			return false;
		}
	}
	
	public ArrayList<String> findBuses(String sp_route_id, String dest_route_id)
	{
		Long startTime = System.currentTimeMillis();
		Log.d("Bus Database", "Starting find bus...");
		String sql = "select a.route_id 'ROUTE_ID'" +
				" from routestation a inner join routestation b" +
				" where a.station_id='" + sp_route_id + "' and b.station_id='" + dest_route_id + "'" +
				" and a.updown = b.updown" +
				" and a.sta_order < b.sta_order and a.route_nm = b.route_nm;";
		Log.d("Bus Database", sql);
		Cursor c = mDb.rawQuery(sql, null);
		
		ArrayList<String> retVal = new ArrayList<String>();
		
		while(c.moveToNext())
		{
			retVal.add(c.getString(0));
		}
		
		Long endTime = System.currentTimeMillis();
		Log.d("Bus Database", "Finding Bus: " + (endTime - startTime) + "ms");
		
		return retVal;
	}
	
	public int calc_distance(double a_lat, double a_lng, double b_lat, double b_lng){
				
		Location locationA = new Location("point A");
		 
		locationA.setLatitude(a_lat);
		locationA.setLongitude(a_lng);		

		Location locationB = new Location("point B");

		locationB.setLatitude(b_lat);
		locationB.setLongitude(b_lng);


		double distance = locationA.distanceTo(locationB);
		
		return (int)distance;		
	}

	public ArrayList<BusStopInfo> getBusStopInfo(int number)
	{
		ArrayList<BusStopInfo> infoList = new ArrayList<BusStopInfo>();
		BusStopInfo info = null;

		String sql = "SELECT * FROM station WHERE STATION_NO = '" + number + "'";
		Log.d("Bus Database", sql);
		Cursor c = mDb.rawQuery(sql, null);
		
		while(c.moveToNext())
		{
			int iId = c.getColumnIndex("STATION_ID");
			int iName = c.getColumnIndex("STATION_NM");
			int iLng = c.getColumnIndex("LNG");
			int iLat = c.getColumnIndex("LAT");
			int iRegion = c.getColumnIndex("REGION_NAME");
			
			info = new BusStopInfo(c.getString(iId), c.getString(iName), c.getString(iRegion), c.getDouble(iLat), c.getDouble(iLng), number, -1);
			
			/*
			String sql2 = "SELECT DISTINCT ROUTE_ID, ROUTE_NM FROM routestation WHERE STATION_ID = '" + c.getString(iId) + "'";
			
			Cursor busC = mDb.rawQuery(sql2, null);
			
			int iBusId = busC.getColumnIndex("ROUTE_ID");
			int iBusNum = busC.getColumnIndex("ROUTE_NM");
			BusInfo busInfo;
			ArrayList<BusInfo> busList = new ArrayList<BusInfo>();
			while (busC.moveToNext())
			{
				busInfo = new BusInfo(c.getString(iBusId), c.getString(iBusNum));
				busList.add(busInfo);
			}
			busC.close();
			info.setBusList(busList);
			*/
			infoList.add(info);
		}
		c.close();
		
		return infoList;
	}
	
	public BusStopInfo getBusStopInfoById(String id)
	{
		BusStopInfo info = null;

		String sql = "SELECT * FROM station WHERE STATION_ID = '" + id + "'";
		Log.d("Bus Database", sql);
		Cursor c = mDb.rawQuery(sql, null);
		
		if (c.getCount() > 0)
		{
			c.moveToFirst();
			int iId = c.getColumnIndex("STATION_ID");
			int iName = c.getColumnIndex("STATION_NM");
			int iNum = c.getColumnIndex("STATION_NO");
			int iLng = c.getColumnIndex("LNG");
			int iLat = c.getColumnIndex("LAT");
			int iRegion = c.getColumnIndex("REGION_NAME");
			
			info = new BusStopInfo(c.getString(iId), c.getString(iName), c.getString(iRegion), c.getDouble(iLat), c.getDouble(iLng), c.getInt(iNum), -1);
			/*
			String sql2 = "SELECT DISTINCT ROUTE_ID, ROUTE_NM FROM routestation WHERE STATION_ID = '" + c.getString(iId) + "'";
			
			Cursor busC = mDb.rawQuery(sql2, null);
			
			int iBusId = busC.getColumnIndex("ROUTE_ID");
			int iBusNum = busC.getColumnIndex("ROUTE_NM");
			int iBusRegion = c.getColumnIndex("REGION_NAME");
			BusInfo busInfo;
			ArrayList<BusInfo> busList = new ArrayList<BusInfo>();
			while (busC.moveToNext())
			{
				busInfo = new BusInfo(c.getString(iBusId), c.getString(iBusNum), c.getString(iBusRegion));
				busList.add(busInfo);
			}
			busC.close();
			
			info.setBusList(busList);
			*/
		}
		c.close();
		
		return info;
	}
	
	public ArrayList<BusStopInfo> getBusStopInfo(String name)
	{
		ArrayList<BusStopInfo> infoList = new ArrayList<BusStopInfo>();
		BusStopInfo info = null;

		String sql = "SELECT * FROM station WHERE STATION_NM LIKE '%" + name + "%'";
		Log.d("Bus Database", sql);
		Cursor c = mDb.rawQuery(sql, null);
		
		while(c.moveToNext())
		{
			int iId = c.getColumnIndex("STATION_ID");
			int iName = c.getColumnIndex("STATION_NM");
			int iLng = c.getColumnIndex("LNG");
			int iLat = c.getColumnIndex("LAT");
			int iNum = c.getColumnIndex("STATION_NO");
			int iRegion = c.getColumnIndex("REGION_NAME");
			
			info = new BusStopInfo(c.getString(iId), c.getString(iName), c.getString(iRegion), c.getDouble(iLat), c.getDouble(iLng), c.getInt(iNum), -1);
			
			/*
			String sql2 = "SELECT DISTINCT ROUTE_ID, ROUTE_NM FROM routestation WHERE STATION_ID = '" + c.getString(iId) + "'";
			
			Cursor busC = mDb.rawQuery(sql2, null);
			
			int iBusId = busC.getColumnIndex("ROUTE_ID");
			int iBusNum = busC.getColumnIndex("ROUTE_NM");
			int iBusRegion = c.getColumnIndex("REGION_NAME");
			BusInfo busInfo;
			ArrayList<BusInfo> busList = new ArrayList<BusInfo>();
			while (busC.moveToNext())
			{
				busInfo = new BusInfo(c.getString(iBusId), c.getString(iBusNum), c.getString(iBusRegion));
				busList.add(busInfo);
			}
			busC.close();
			info.setBusList(busList);
			*/
			infoList.add(info);
		}
		c.close();
		
		return infoList;
	}
	
	public BusInfo getBusInfo(String id)
	{
		BusInfo info = null;

		String sql = "SELECT * FROM route WHERE ROUTE_ID = '" + id + "'";
		Log.d("Bus Database", sql);
		Cursor c = mDb.rawQuery(sql, null);
		
		if (c.getCount() > 0)
		{
			c.moveToFirst();
			
			int iId = c.getColumnIndex("ROUTE_ID");
			int iName = c.getColumnIndex("ROUTE_NM");
			int iUpStart = c.getColumnIndex("UP_FIRST_TIME");
			int iUpEnd = c.getColumnIndex("UP_LAST_TIME");
			int iDownStart = c.getColumnIndex("DOWN_FIRST_TIME");
			int iDownEnd = c.getColumnIndex("DOWN_LAST_TIME");
			int iPeekTerm = c.getColumnIndex("PEEK_ALLOC");
			int iNPeekTerm = c.getColumnIndex("NPEEK_ALLOC");
			
			info = new BusInfo(c.getString(iId), c.getString(iName), c.getString(iUpStart), c.getString(iUpEnd), c.getString(iDownStart), c.getString(iDownEnd), c.getString(iPeekTerm), c.getString(iNPeekTerm));
			
			/*
			String sql2 = "SELECT * FROM routestation WHERE ROUTE_ID = '" + c.getString(iId) + "' ORDER BY STA_ORDER ASC";
			
			Cursor lineC = mDb.rawQuery(sql2, null);
			
			int iStopNo = lineC.getColumnIndex("STATION_ID");
			ArrayList<BusStopInfo> stopList = new ArrayList<BusStopInfo>();
			while (lineC.moveToNext())
			{
				BusStopInfo busStop = getBusStopInfoById(lineC.getString(iStopNo));
				if (busStop != null)
				{
					stopList.add(busStop);
				}
			}
			lineC.close();
			
			info.setStopList(stopList);
			*/
		}
		c.close();
		
		return info;
	}
	
	public ArrayList<BusInfo> getBusInfoByNumber(String number)
	{
		ArrayList<BusInfo> infoList = new ArrayList<BusInfo>();
		BusInfo info = null;

		String sql = "SELECT * FROM route WHERE ROUTE_NM LIKE '" + number + "%' ORDER BY ROUTE_NM ASC";
		Log.d("Bus Database", sql);
		Cursor c = mDb.rawQuery(sql, null);
		
		while (c.moveToNext())
		{
			int iId = c.getColumnIndex("ROUTE_ID");
			int iName = c.getColumnIndex("ROUTE_NM");
			int iRegion = c.getColumnIndex("REGION_NAME");
			int iUpStart = c.getColumnIndex("UP_FIRST_TIME");
			int iUpEnd = c.getColumnIndex("UP_LAST_TIME");
			int iDownStart = c.getColumnIndex("DOWN_FIRST_TIME");
			int iDownEnd = c.getColumnIndex("DOWN_LAST_TIME");
			int iPeekTerm = c.getColumnIndex("PEEK_ALLOC");
			int iNPeekTerm = c.getColumnIndex("NPEEK_ALLOC");
			
			info = new BusInfo(c.getString(iId), c.getString(iName), c.getString(iRegion),c.getString(iUpStart), c.getString(iUpEnd), c.getString(iDownStart), c.getString(iDownEnd), c.getString(iPeekTerm), c.getString(iNPeekTerm));
			infoList.add(info);
		}
		c.close();
		
		return infoList;
	}
	
	public ArrayList<BusStopInfo> getBusStopOfBus(String busId)
	{
		String sql = "SELECT STATION_ID, STATION_NM, UPDOWN, STA_ORDER FROM routestation WHERE ROUTE_ID = '" + busId + "' ORDER BY STA_ORDER ASC";
		Log.d("Bus Database", sql);
		
		Cursor lineC = mDb.rawQuery(sql, null);
		
		int iStopNo = lineC.getColumnIndex("STATION_ID");
		int iStopName = lineC.getColumnIndex("STATION_NM");
		int iUpDown = lineC.getColumnIndex("UPDOWN");
		ArrayList<BusStopInfo> stopList = new ArrayList<BusStopInfo>();
		while (lineC.moveToNext())
		{
			BusStopInfo busStop = new BusStopInfo(lineC.getString(iStopNo), lineC.getString(iStopName), lineC.getString(iUpDown));

			stopList.add(busStop);

		}
		lineC.close();
		
		return stopList;
	}
	
	public boolean isOpen()
	{
		if (mDb != null)
			return mDb.isOpen();
		else
			return false;
	}
}
