package com.ajouroid.timetable;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.IBinder;
import android.util.Log;

public class DBAdapter {

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb; // 데이터베이스를 저장

	private static final int DATABASE_VERSION = 7;

	private final Context mCtx;

	public static final int TYPE_ASSIGNMENT = 0;
	public static final int TYPE_TEST = 1;
	public static final int TYPE_EXTRA = 2;
	public static final int TYPE_ETC = 3;

	private class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, "timetable.db", null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE subject (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "`name` TEXT UNIQUE,"
					+ "`classroom` TEXT,"
					+ "`professor` TEXT,"
					+ "`email` TEXT," + "`color` INTEGER)");

			db.execSQL("CREATE TABLE times ("
					+ "`_id` INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "`subject` TEXT,"
					+ "`day` INTEGER,"
					+ "`starttime` INTEGER,"
					+ "`endtime` INTEGER, "
					+ "FOREIGN KEY(subject) REFERENCES subject(name) ON DELETE CASCADE ON UPDATE CASCADE)");

			db.execSQL("CREATE TABLE tasks ("
					+ "`_id` INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ "`subject` TEXT,"
					+ "`type` INTEGER,"
					+ "`title` TEXT,"
					+ "`desc` TEXT,"
					+ "`taskdate` TEXT,"
					+ "`usetime` INTEGER,"
					+ "FOREIGN KEY(subject) REFERENCES subject(name) ON DELETE CASCADE ON UPDATE CASCADE)");
			
			db.execSQL("CREATE TABLE stations ("
					+ "`_id` INTEGER PRIMARY KEY, "
					+ "`station_nm` text, "
					+ "`station_number` int, "
					+ "`start` integer)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			backup("upgrade.db", db);

			db.execSQL("DROP TABLE IF EXISTS subject");
			db.execSQL("DROP TABLE IF EXISTS times");
			db.execSQL("DROP TABLE IF EXISTS tasks");
			db.execSQL("DROP TABLE IF EXISTS stations");
			onCreate(db);
			
			restore("upgrade.db", db);
		}
	}

	public DBAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	public void open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
	}

	public void close() {
		mDbHelper.close();
	}

	public boolean addSubject(String name, String classroom, String professor,
			String email, int color) {

		ContentValues row = new ContentValues();

		row.put("name", name);
		row.put("classroom", classroom);
		row.put("professor", professor);
		row.put("email", email);
		row.put("color", color);

		long result = mDb.insert("subject", null, row);

		if (result == -1)
			return false;
		else {
			Log.d("database", name + " (" + classroom
					+ ") : Added to the Database");
			return true;
		}
	}

	public boolean addSubject(Subject newSubject) {
		return addSubject(newSubject.getName(), newSubject.getClassRoom(),
				newSubject.getProfessor(), newSubject.getEmail(),
				newSubject.getColor());
	}

	public boolean addTime(String subject, ClassTime time) {
		time.setSubject(subject);
		boolean valid = isValid(time);
		if (!valid)
			return false;

		int startMin = time.getStartTime().toMinute();
		int endMin = time.getEndTime().toMinute();

		Cursor c = mDb.rawQuery("SELECT * FROM times WHERE subject = '"
				+ subject + "' and day = '" + time.getDay()
				+ "' AND (startTime <= '" + endMin + "' AND endTime >= '"
				+ startMin + "') ORDER BY starttime ASC", null);

		int iStart = c.getColumnIndex("starttime");
		int iEnd = c.getColumnIndex("endtime");

		while (c.moveToNext()) {
			int start = c.getInt(iStart);
			int end = c.getInt(iEnd);

			// 연결되는 시간을 합침
			if (end > endMin) {
				time.setEndTime(new Time(end));
				endMin = time.getEndTime().toMinute();
			}
			if (start < startMin) {
				time.setStartTime(new Time(start));
				startMin = time.getStartTime().toMinute();
			}

			mDb.delete("times", "_id = '" + c.getInt(0) + "'", null);
		}

		c.close();

		ContentValues row = new ContentValues();

		row.put("subject", subject);
		row.put("day", time.getDay());
		row.put("starttime", startMin);
		row.put("endtime", endMin);

		long result = mDb.insert("times", null, row);

		if (result == -1)
			return false;
		else {
			Intent updater = new Intent();
			updater.setAction(AlarmService.UPDATE_ACTION);
			mCtx.sendBroadcast(updater);
			return true;
		}
	}

	AlarmService morningCallService;

	public void bind() {
		Intent i = new Intent(mCtx, AlarmService.class);
		// startService(i);
		mCtx.bindService(i, conn, Context.BIND_AUTO_CREATE);
		if (morningCallService != null) {
			morningCallService.setMorningCall();
			morningCallService.setNextClassStartCall();
			morningCallService.setTaskTimeCall();
		}
	}

	public void unbind() {
		mCtx.unbindService(conn);
	}

	private ServiceConnection conn = new ServiceConnection() {
		public void onServiceConnected(ComponentName className,
				IBinder rawBinder) {
			morningCallService = ((AlarmService.AlarmBinder) rawBinder)
					.getService();
		}

		public void onServiceDisconnected(ComponentName name) {
			morningCallService = null;
		}
	};

	public Subject getSubject(String name) {
		Cursor c = mDb.rawQuery("SELECT * FROM subject WHERE name = '" + name
				+ "'", null);

		if (c.moveToNext()) {
			int iName = c.getColumnIndex("name");
			int iClass = c.getColumnIndex("classroom");
			int iProf = c.getColumnIndex("professor");
			int iEmail = c.getColumnIndex("email");
			int iColor = c.getColumnIndex("color");
			Subject retVal = new Subject(c.getString(iName),
					c.getString(iClass), c.getString(iProf),
					c.getString(iEmail), c.getInt(iColor));
			c.close();
			return retVal;
		} else {
			c.close();
			return null;
		}
	}
	
	public Subject getSubject(int id) {
		Cursor c = mDb.rawQuery("SELECT * FROM subject WHERE _id = '" + id
				+ "'", null);

		if (c.moveToNext()) {
			int iName = c.getColumnIndex("name");
			int iClass = c.getColumnIndex("classroom");
			int iProf = c.getColumnIndex("professor");
			int iEmail = c.getColumnIndex("email");
			int iColor = c.getColumnIndex("color");
			Subject retVal = new Subject(c.getString(iName),
					c.getString(iClass), c.getString(iProf),
					c.getString(iEmail), c.getInt(iColor));
			c.close();
			return retVal;
		} else {
			c.close();
			return null;
		}
	}
	
	public String getSubjectName(int id) {
		Cursor c = mDb.rawQuery("SELECT name FROM subject WHERE _id = '" + id
				+ "'", null);
		String name = null;

		if (c.getCount() > 0) {
			c.moveToFirst();
			name = c.getString(0);
		} 
		c.close();
		return name;
	}

	public Cursor getSubjectCursor() {
		Cursor cursor = mDb.rawQuery("SELECT * FROM subject ORDER BY name asc",
				null);
		return cursor;
	}

	public Cursor getSubjectCursor(String subject) {
		Cursor cursor = mDb.rawQuery("SELECT * FROM subject WHERE name = '"
				+ subject + "'", null);
		cursor.moveToFirst();
		return cursor;
	}
	
	public Cursor getSubjectCursor(int id) {
		Cursor cursor = mDb.rawQuery("SELECT * FROM subject WHERE _id = '"
				+ id + "'", null);
		cursor.moveToFirst();
		return cursor;
	}

	public boolean updateSubject(int _id, Subject updater, String oldName) {
		ContentValues row = new ContentValues();

		row.put("name", updater.getName());
		row.put("classroom", updater.getClassRoom());
		row.put("professor", updater.getProfessor());
		row.put("email", updater.getEmail());
		row.put("color", updater.getColor());
		
		Cursor tempC = mDb.rawQuery("select _id, name from subject where name = '" + updater.getName() + "'", null);
		if (tempC.getCount() > 0)
		{
			tempC.moveToFirst();
			String name = tempC.getString(tempC.getColumnIndex("name"));
			tempC.close();
			if (name.compareTo(updater.getName()) != 0)
				return false;
		}

		mDb.update("subject", row, "_id = '" + _id + "'", null);

		mDb.delete("times", "subject = '" + oldName + "'", null);

		
		ArrayList<ClassTime> times = updater.getTime();
		for (int i = 0; i < times.size(); i++) {
			this.addTime(updater.getName(), times.get(i));
		}

		if (updater.getName().compareTo(oldName) != 0) {
			mDb.execSQL("UPDATE tasks SET subject = '" + updater.getName()
					+ "' WHERE subject = '" + oldName + "'");
		}
		
		return true;
	}

	public Cursor getTimeCursor(String subject) {
		Cursor cursor = mDb.rawQuery("select * from times where subject = '"
				+ subject + "' ORDER BY day asc, starttime asc", null);
		return cursor;
	}

	public Subject[] getAllSubjects() {
		Cursor c = mDb
				.rawQuery("SELECT * FROM subject ORDER BY name asc", null);
		Subject[] retVal = null;
		if (c.getCount() > 0) {
			retVal = new Subject[c.getCount()];

			int i = 0;
			while (c.moveToNext()) {
				int iName = c.getColumnIndex("name");
				int iClass = c.getColumnIndex("classroom");
				int iProf = c.getColumnIndex("professor");
				int iEmail = c.getColumnIndex("email");
				int iColor = c.getColumnIndex("color");
				retVal[i] = new Subject(c.getString(iName),
						c.getString(iClass), c.getString(iProf),
						c.getString(iEmail), c.getInt(iColor));
				i++;
			}
		}
		c.close();
		return retVal;
	}

	public ClassTime[] getTimes(String subject) {
		Cursor c = mDb.rawQuery("SELECT * FROM times" + " WHERE subject = '"
				+ subject + "' ORDER BY day asc, starttime asc", null);

		int iDay = c.getColumnIndex("day");
		int iStart = c.getColumnIndex("starttime");
		int iEnd = c.getColumnIndex("endtime");

		if (c.getCount() > 0) {
			ClassTime[] retVal = new ClassTime[c.getCount()];
			int i = 0;
			while (c.moveToNext()) {
				retVal[i] = new ClassTime(c.getInt(iDay), new Time(
						c.getInt(iStart)), new Time(c.getInt(iEnd)));
				retVal[i].setSubject(subject);
				i++;
			}
			c.close();
			return retVal;
		} else {
			c.close();
			return null;
		}
	}

	public void deleteSubject(String subject) {
		mDb.delete("tasks", "subject = '" + subject + "'", null);
		mDb.delete("times", "subject = '" + subject + "'", null);
		mDb.delete("subject", "name = '" + subject + "'", null);
	}

	public boolean isValid(ClassTime time) {
		int day = time.getDay();

		int startMin = time.getStartTime().toMinute();
		int endMin = time.getEndTime().toMinute();

		Cursor cursor = mDb.rawQuery("SELECT * FROM times WHERE day = '" + day
				+ "' AND (startTime < '" + endMin + "' AND endTime > '"
				+ startMin + "')", null);

		if (cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			String subject = cursor.getString(cursor.getColumnIndex("subject"));
			if (subject.compareTo(time.getSubject()) == 0)
				return true;
			else return false;
		}
		else
			return true;

	}

	public void init() {
		open();
		mDb.execSQL("DROP TABLE IF EXISTS subject");
		mDb.execSQL("DROP TABLE IF EXISTS times");
		mDb.execSQL("DROP TABLE IF EXISTS tasks");
		mDb.execSQL("DROP TABLE IF EXISTS stations");

		mDbHelper.onCreate(mDb);
		close();
	}

	public Cursor getSelectedTimes(int startDay, int endDay, int startMin,
			int endMin) {
		Cursor cursor;

		cursor = mDb.rawQuery("SELECT * FROM times WHERE (day >= '" + startDay
				+ "' AND day <= '" + endDay + "') AND (startTime < '" + endMin
				+ "' AND endTime > '" + startMin + "')", null);

		return cursor;
	}

	public Cursor getTaskCursor() {
		Cursor cursor;

		cursor = mDb.rawQuery("SELECT * FROM tasks", null);

		return cursor;
	}

	public Cursor getTaskCursor(String subject) {
		Cursor cursor;

		cursor = mDb.rawQuery("SELECT * FROM tasks WHERE subject = '" + subject
				+ "' ORDER BY taskdate desc", null);

		return cursor;
	}

	public void addTask(String subject, int type, String title, String desc,
			Calendar datetime, boolean useTime) {
		ContentValues row = new ContentValues();

		row.put("subject", subject);
		row.put("type", type);
		row.put("title", title);
		row.put("desc", desc);
		SimpleDateFormat format = new SimpleDateFormat(mCtx.getResources()
				.getString(R.string.dateformat), Locale.US);
		String date = format.format(datetime.getTime());
		row.put("taskdate", date);

		if (useTime)
			row.put("usetime", 1);
		else
			row.put("usetime", 0);

		mDb.insert("tasks", null, row);
		
		Intent updater = new Intent();
		updater.setAction(AlarmService.UPDATE_ACTION);
		mCtx.sendBroadcast(updater);
	}

	public void updateTask(int _id, String subject, int type, String title,
			String desc, Calendar datetime, boolean useTime) {
		ContentValues row = new ContentValues();

		row.put("subject", subject);
		row.put("type", type);
		row.put("title", title);
		row.put("desc", desc);
		SimpleDateFormat format = new SimpleDateFormat(mCtx.getResources()
				.getString(R.string.dateformat), Locale.US);
		String date = format.format(datetime.getTime());
		row.put("taskdate", date);

		if (useTime)
			row.put("usetime", 1);
		else
			row.put("usetime", 0);

		mDb.update("tasks", row, "_id = '" + _id + "'", null);
		
		Intent updater = new Intent();
		updater.setAction(AlarmService.UPDATE_ACTION);
		mCtx.sendBroadcast(updater);
	}

	public Cursor getTask(int _id) {
		Cursor cursor = mDb.rawQuery("SELECT * FROM tasks WHERE _id = '" + _id
				+ "'", null);
		cursor.moveToFirst();
		return cursor;
	}

	public void deleteTask(int _id) {
		mDb.delete("tasks", "_id = '" + _id + "'", null);

	}

	public void deleteTime(int _id) {
		mDb.delete("times", "_id = '" + _id + "'", null);
	}

	public boolean isOpen() {
		return mDb.isOpen();
	}

	public static long distance(Date task, Date now) {
		return task.getTime() - now.getTime();
	}

	public Time getFirstClassTime(int day) {
		Cursor c = mDb.rawQuery("SELECT * FROM times WHERE day = '" + day + "'"
				+ " ORDER BY starttime asc", null);
		int iStart = c.getColumnIndex("starttime");
		Time t = null;
		if (c.moveToFirst()) {
			int time = c.getInt(iStart);
			t = new Time(time);
		}
		c.close();
		return t;
	}

	public Task getTaskTime() {
		Task task = null;
		Calendar curTime = Calendar.getInstance();
		curTime.add(Calendar.HOUR_OF_DAY, 1);
		Date date = curTime.getTime();
		SimpleDateFormat format = new SimpleDateFormat(mCtx.getResources().getString(R.string.dateformat), Locale.US);
		
		String currentTime = format.format(date);
		
		Cursor tcursor = mDb.rawQuery("SELECT * FROM tasks WHERE taskdate > '"
				+ currentTime + "' AND usetime = 1 ORDER BY taskdate asc", null);
		int itaskDate = tcursor.getColumnIndex("taskdate");
		int itaskTitle = tcursor.getColumnIndex("title");
		int itaskType = tcursor.getColumnIndex("type");
		int iSubject = tcursor.getColumnIndex("subject");

		if (tcursor.moveToFirst()) {
			task = new Task(tcursor.getString(iSubject), tcursor.getString(itaskTitle), tcursor.getString(itaskDate), tcursor.getInt(itaskType));
		}
		tcursor.close();
		
		return task;
	}

	public ArrayList<Task> getNextDayTasks() {
		Calendar date = Calendar.getInstance();
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.add(Calendar.DAY_OF_MONTH, 1);

		Date today = date.getTime();

		date.add(Calendar.DAY_OF_MONTH, 1);
		Date nextday = date.getTime();

		SimpleDateFormat format = new SimpleDateFormat(mCtx.getResources()
				.getString(R.string.dateformat), Locale.US);
		Cursor ycursor = mDb.rawQuery(
				"SELECT * FROM tasks WHERE taskdate >= '"
						+ format.format(today) + "' AND taskdate < '"
						+ format.format(nextday) + "' ORDER BY taskdate asc",
				null);

		ArrayList<Task> list = new ArrayList<Task>();

		int itaskDate = ycursor.getColumnIndex("taskdate");
		int itaskType = ycursor.getColumnIndex("type");
		int itaskTitle = ycursor.getColumnIndex("title");
		int iSubject = ycursor.getColumnIndex("subject");

		while (ycursor.moveToNext()) {
			Task temp = new Task(ycursor.getString(iSubject), ycursor.getString(itaskTitle), ycursor.getString(itaskDate), ycursor.getInt(itaskType));

			list.add(temp);
		}
		ycursor.close();

		return list;
	}

	public AlarmTime getNextClassTime(int day, Time beforeTime) {
		int beforeTimeMinute = beforeTime.toMinute();
		boolean today = true;
		Cursor c = mDb.rawQuery("SELECT * FROM times WHERE (day = '" + day
				+ "'" + " AND starttime > '" + beforeTimeMinute
				+ "') OR day > '" + day + "' ORDER BY day asc, starttime asc",
				null);
		if (c.getCount() == 0) // 아무것도 찾지 못했을 경우 이전 요일에서도 찾음
		{
			c.close();
			c = mDb.rawQuery(
					"SELECT * FROM times ORDER BY day asc, starttime asc", null);

			if (c.getCount() == 0) // 여기서도 찾지 못하면 null
				return null;
			else
				today = false;
		}

		int iSubject = c.getColumnIndex("subject");
		int iDay = c.getColumnIndex("day");
		int iStart = c.getColumnIndex("starttime");
		AlarmTime t = null;
		if (c.moveToFirst()) {
			t = new AlarmTime(c.getInt(iDay), c.getInt(iStart));
			t.setIsToday(today);
			t.setSubject(c.getString(iSubject));
		}
		c.close();
		return t;
	}
	
	public boolean isNowClassTime(int day, Time t, Time restTime)
	{
		int tMin = t.toMinute();
		String sql = "SELECT * FROM times WHERE day = '" + day + "' AND starttime < '" + tMin + "' AND endtime > '" + (tMin + restTime.toMinute()) + "'";
		Cursor c = mDb.rawQuery(sql, null);
		
		if (c.getCount() > 0)
			return true;
		else return false;
	}
	
	public int getIdFromTitle(String title)
	{
		int id;
		String sql = "SELECT _id FROM subject WHERE name = '" + title + "'";
		Cursor c = mDb.rawQuery(sql, null);
		
		if (c.getCount() > 0)
		{
			c.moveToFirst();
			
			id = c.getInt(0);
		}
		else
			id = -1;
		
		c.close();
		return id;
	}
	
	
	public static final String SEP = "\n";
	public void backup(String filename, SQLiteDatabase db)
	{
		String dir = "/sdcard/SmartTimeTable";
		File dirFile = new File(dir);
		if (!dirFile.exists())
		{
			dirFile.mkdirs();
		}
		
		//String file = dir + "/backup" + System.currentTimeMillis() + ".txt";
		String file = dir + "/" + filename;
		File output = new File(file);
		
		try {
			FileOutputStream fos = new FileOutputStream(output);
			
			
			// 과목 정보 입력
			Cursor subjectCursor = db.rawQuery("SELECT * FROM subject", null);
			
			int iId = subjectCursor.getColumnIndex("_id");
			int iName = subjectCursor.getColumnIndex("name");
			int iClassRoom = subjectCursor.getColumnIndex("classroom");
			int iProf = subjectCursor.getColumnIndex("professor");
			int iEmail = subjectCursor.getColumnIndex("email");
			int iColor = subjectCursor.getColumnIndex("color");
			
			fos.write(new String(subjectCursor.getCount() + "_SUBJECTS").getBytes());
			
			String data = SEP;
			while(subjectCursor.moveToNext())
			{
				int _id = subjectCursor.getInt(iId);
				String name = subjectCursor.getString(iName);
				String classroom = subjectCursor.getString(iClassRoom);
				String prof = subjectCursor.getString(iProf);
				String email = subjectCursor.getString(iEmail);
				int color = subjectCursor.getInt(iColor);
				
				data += _id + SEP + name + SEP + classroom + SEP + prof
						+ SEP + email + SEP + color;
				
				fos.write(data.getBytes());
				
				data = SEP;
			}
			subjectCursor.close();

			Cursor timeCursor = db.rawQuery("SELECT * FROM times", null);
			
			iId = timeCursor.getColumnIndex("_id");
			int iSubject = timeCursor.getColumnIndex("subject");
			int iDay = timeCursor.getColumnIndex("day");
			int iStart = timeCursor.getColumnIndex("starttime");
			int iEnd = timeCursor.getColumnIndex("endtime");
			
			fos.write((SEP + timeCursor.getCount() + "_TIMES").getBytes());
			data = SEP;
			while(timeCursor.moveToNext())
			{
				int _id = timeCursor.getInt(iId); 
				String subject = timeCursor.getString(iSubject);
				int day = timeCursor.getInt(iDay);
				int start = timeCursor.getInt(iStart);
				int end = timeCursor.getInt(iEnd);
				
				data += _id + SEP + subject + SEP + day + SEP + start + SEP + end;
				fos.write(data.getBytes());
				data=SEP;
			}
			timeCursor.close();

			Cursor taskCursor = db.rawQuery("SELECT * FROM tasks", null);
			
			iId = taskCursor.getColumnIndex("_id");
			iSubject = taskCursor.getColumnIndex("subject");
			int iType = taskCursor.getColumnIndex("type");
			int iTitle = taskCursor.getColumnIndex("title");
			int iDesc = taskCursor.getColumnIndex("desc");
			int iTaskdate = taskCursor.getColumnIndex("taskdate");
			int iUsetime = taskCursor.getColumnIndex("usetime");
			
			
			fos.write((SEP + taskCursor.getCount() + "_TASKS").getBytes());
			data = SEP;
			while(taskCursor.moveToNext())
			{
				int _id = taskCursor.getInt(iId); 
				String subject = taskCursor.getString(iSubject);
				int type = taskCursor.getInt(iType);
				String title = taskCursor.getString(iTitle);
				String desc = taskCursor.getString(iDesc);
				desc = desc.replace("\n", "\t");
				String taskdate = taskCursor.getString(iTaskdate);
				int usetime = taskCursor.getInt(iUsetime);
				
				data += _id + SEP + subject + SEP + type + SEP + title + SEP + desc + SEP + taskdate + SEP + usetime;
				fos.write(data.getBytes());
				data=SEP;
			}
			taskCursor.close();
			fos.flush();
			fos.close();
			
			//restore(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void backup(String filename)
	{
		open();
		backup(filename, mDb);
		close();
	}
	
	public void restore(String filename, SQLiteDatabase db)
	{
		mDb = db;
		File file = new File("/sdcard/SmartTimeTable/" + filename);
		try {
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[8192];
			int n;
			String data = "";
			while ((n=fis.read(buffer)) != -1)
			{
				data += new String(buffer, 0, n);
			}
			fis.close();
			
			String[] parsedData = data.split(SEP);
			int cnt=0;
			
			String[] temp = parsedData[cnt].split("_");
			int subCount = Integer.parseInt(temp[0]);
			
			cnt++;
			for (int j=0; j<subCount; j++)
			{
				int _id;
				String name;
				String classroom;
				String prof;
				String email;
				int color;
				
				_id = Integer.parseInt(parsedData[cnt++]);
				name = parsedData[cnt++];
				classroom = parsedData[cnt++];
				prof = parsedData[cnt++];
				email = parsedData[cnt++];
				color = Integer.parseInt(parsedData[cnt++]);
				
				this.addSubject(name, classroom, prof, email, color);
				Log.d("SmartTimeTable", "Restore Subject:" + _id + "," + name + "," + classroom + "," + prof + "," + email + "," + color);
			}
			
			
			temp = parsedData[cnt].split("_");
			int timeCount = Integer.parseInt(temp[0]);
			
			cnt++;
			
			for (int j=0; j<timeCount; j++)
			{
				int _id;
				String subject;
				int day;
				int starttime;
				int endtime;
				
				_id = Integer.parseInt(parsedData[cnt++]);
				subject = parsedData[cnt++];
				day = Integer.parseInt(parsedData[cnt++]);
				starttime = Integer.parseInt(parsedData[cnt++]);
				endtime = Integer.parseInt(parsedData[cnt++]);
				
				this.addTime(subject, new ClassTime(day, new Time(starttime), new Time(endtime)));
				Log.d("SmartTimeTable", "Restore Time:" + _id + "," + subject + "," + day + "," + starttime + "," + endtime);
			}
			
			temp = parsedData[cnt].split("_");
			int taskCount = Integer.parseInt(temp[0]);
			
			cnt++;
			for (int j=0; j<taskCount; j++)
			{
				try
				{
					int _id = Integer.parseInt(parsedData[cnt++]);
					String subject = parsedData[cnt++];
					int type = Integer.parseInt(parsedData[cnt++]);
					String title = parsedData[cnt++];
					String desc = parsedData[cnt++];
					desc = desc.replace("\t", "\n");
					String taskdate = parsedData[cnt++];
					int usetime = Integer.parseInt(parsedData[cnt++]);
					
					boolean bUsetime;
					if (usetime == 1)
						bUsetime = true;
					else
						bUsetime = false;
					
					SimpleDateFormat format = new SimpleDateFormat(mCtx.getResources().getString(R.string.dateformat), Locale.US);
					Date date = format.parse(taskdate);
					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					this.addTask(subject, type, title, desc, cal, bUsetime);
					Log.d("SmartTimeTable", "Restore Task:" + _id + "," + subject + "," + type + "," + title + "," + desc + "," + taskdate + "," + usetime);
				} catch (NumberFormatException e)
				{
					e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void restore(String filename)
	{
		init();
		open();
		restore(filename, mDb);
		close();
	}
}
