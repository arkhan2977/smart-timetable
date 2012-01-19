package com.ajouroid.timetable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class TimeTable extends View {

	DBAdapter dbA;

	// 가로세로길이
	int width;
	int height;

	// 여백
	int topmost = 10;
	int leftmost = 10;

	int bottommargin = 10;
	int rightmargin = 50;

	// 한 칸의 길이
	int boxwidth;
	int boxheight;

	// 요일과 시간 표시줄의 너비
	int dayheight;
	int timewidth;

	// 시간 슬롯의 갯수
	int maxslot;

	// 요일 갯수
	int day;

	// 선택한 영역의 좌표값
	int[] position = { -1, -1 };

	// 시작시간, 종료시간, 시간단위
	Time startTime;
	Time endTime;
	Time baseTime;

	// 배경
	Bitmap bgBitmap;
	int bgColor;
	int mainBgColor;

	// 선택줄 색
	int rowColor;
	int columnColor;

	// 선택영역 색
	int selectionColor;

	// 요일, 시간 배경
	// int dayColor;
	int timeColor;

	int alphaValue;

	// 선 색, 타임라인 색
	int lineColor;
	int tableLineColor;
	int timeLineColor;

	int shadowColor;

	// 글꼴 색
	int timeFontColor;
	int subFontColor;

	// 누르고 있는지 여부
	boolean tapDown = false;

	// 선택 영역의 기준점
	int selectionCenterX;
	int selectionCenterY;

	// 선택 영역의 좌표
	int selectionStartX;
	int selectionStartY;
	int selectionEndX;
	int selectionEndY;

	ArrayList<Subject> subjectList;

	// 과목 추가 모드
	boolean addingMode = false;
	String selectedSubject;

	// 요일 문자열
	final String[] days = getResources().getStringArray(R.array.days);

	Context context;
	Resources r;

	public TimeTable(Context context) {
		super(context);
		this.context = context;
		init();
	}

	public TimeTable(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		this.context = context;
		init();
	}

	public TimeTable(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		init();
	}

	// 초기화
	public void init() {
		// Preference 불러옴
		SharedPreferences prefs = this.getContext().getSharedPreferences(
				"com.ajouroid.timetable_preferences", 0);

		r = context.getResources();
		// DB 초기화
		dbA = new DBAdapter(this.getContext());

		// Preference 정보 불러옴
		day = Integer.parseInt(prefs.getString("weekend", "5"));
		startTime = new Time(prefs.getString("start", "9:00"));
		endTime = new Time(prefs.getString("end", "18:00"));
		baseTime = new Time(prefs.getString("base", "1:30"));

		int theme = Integer.parseInt(prefs.getString("theme", "0"));

		taskTable = new HashMap<String, Integer>();

		// 배경그림
		bgBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.background1);

		// 테마별로 색 적용
		switch (theme) {
		// Black Theme
		case 0:
			bgColor = 0x000000;
			mainBgColor = 0x333333;

			rowColor = 0x0000FF;
			columnColor = 0x0000FF;

			selectionColor = 0xFFFF00;

			// dayColor = 0x000000;
			timeColor = 0x000000;

			lineColor = 0xBBBBBB;
			tableLineColor = 0x555555;
			timeLineColor = 0x000000;

			timeFontColor = 0xFFFFFF;
			subFontColor = 0x000000;

			alphaValue = 0xFF;
			shadowColor = 0xFFFFFF;
			break;

		// White Theme
		case 1:
			bgColor = 0xFFFFFF;
			mainBgColor = 0xFFFFFF;

			rowColor = 0xFFFFFF;
			columnColor = 0xFFFFFF;

			selectionColor = 0x909090;

			// dayColor = 0xFFFF00;
			timeColor = 0xFFFFFF;

			lineColor = 0x000000;
			tableLineColor = 0xDDDDDD;
			timeLineColor = 0x000000;

			timeFontColor = 0x000000;
			subFontColor = 0x000000;

			alphaValue = 0xFF;
			shadowColor = 0;
			break;

		// Skyblue Theme
		case 2:
			bgColor = 0xFFFFFF;
			mainBgColor = 0xdefffe;

			rowColor = 0x0000FF;
			columnColor = 0x0000FF;

			selectionColor = 0xFF0000;

			// dayColor = 0xFFFF00;
			timeColor = 0xb1ecff;

			lineColor = 0x000000;
			tableLineColor = 0xAAAAAA;
			timeLineColor = 0x000000;

			timeFontColor = 0x000000;
			subFontColor = 0x000000;

			alphaValue = 0xFF;
			shadowColor = 0;
			break;

		// Pink Theme
		case 3:
			bgColor = 0xFFFFFF;
			mainBgColor = 0xfff3f5;

			rowColor = 0xFFFFFF;
			columnColor = 0xFFFFFF;

			selectionColor = 0x909090;

			// dayColor = 0xFFFF00;
			timeColor = 0xffd6dc;

			lineColor = 0x000000;
			tableLineColor = 0xDDDDDD;
			timeLineColor = 0x000000;

			timeFontColor = 0x000000;
			subFontColor = 0x000000;

			alphaValue = 0xFF;
			shadowColor = 0;
			break;

		// Transparent Theme
		case 4:
			bgColor = 0x000000;
			mainBgColor = 0xFFFFFF;

			rowColor = 0xFFFFFF;
			columnColor = 0xFFFFFF;

			selectionColor = 0x909090;

			// dayColor = 0xFFFF00;
			timeColor = 0xFFFFFF;

			lineColor = 0xBBBBBB;
			tableLineColor = 0x555555;
			timeLineColor = 0xABABAB;

			timeFontColor = 0xFFFFFF;
			subFontColor = 0xFFFFFF;

			alphaValue = 0x00;
			shadowColor = 0xFFFFFF;
			break;
		}

		// 시간과 요일 표시부분의 너비
		dayheight = 60;
		timewidth = 80;

		update();
	}

	// 위젯의 크기 측정
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		width = measure(widthMeasureSpec);
		height = measure(heightMeasureSpec);

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

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);

		Paint fill = new Paint();
		fill.setColor(bgColor);
		fill.setAlpha(0xFF);
		canvas.drawRect(0, 0, width, height, fill);

		// 시간표 이미지를 가져옴
		Bitmap bitmap = getImage(width, height, topmost, leftmost, rightmargin,
				bottommargin);

		Paint p = new Paint();
		canvas.drawBitmap(bitmap, 0, 0, p);

		/*
		 * ====================== 선택영역 그림 ======================
		 */
		if (tapDown) {
			int timeleft = leftmost + timewidth;
			int timetop = topmost + dayheight;
			p.setColor(selectionColor);
			p.setAlpha(0x55);
			int sX = timeleft + selectionStartX * boxwidth;
			int sY = timetop + selectionStartY * boxheight;
			int eX = timeleft + (selectionEndX + 1) * boxwidth;
			int eY = timetop + (selectionEndY + 1) * boxheight;
			canvas.drawRect(new Rect(sX, sY, eX, eY), p);
		}

		/*
		 * ====================== 추가중 메시지 출력 ======================
		 */
		p.setTextAlign(Align.CENTER);
		p.setColor(timeFontColor);
		p.setAlpha(0xFF);
		p.setTextSize(30);
		if (addingMode) {
			canvas.drawText("[" + selectedSubject + "] " + r.getString(R.string.table_adding), width / 2,
					height / 2, p);
			canvas.drawText(getResources().getString(R.string.pressBackBtn),
					width / 2, height / 2 - p.ascent() + p.descent(), p);
		}
	}

	// 문자열을 길이에 맞게 여러줄로 나눔
	public String[] seperateLines(String text, int width, Paint font) {
		int lineLength = font.breakText(text, true, boxwidth, null);
		String printable = text.substring(0, lineLength);

		while (true) {
			int startPointer = lineLength;
			text = text.substring(startPointer);
			if (text.length() == 0)
				break;
			lineLength = font.breakText(text, true, boxwidth, null);
			printable += "\n" + text.substring(0, lineLength);
		}

		String[] strLines = printable.split("\n");

		return strLines;
	}

	// 시간표를 클릭했을 때
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		int action = event.getAction();

		boolean valid = getPosition(x, y);

		switch (action) {

		// 선택 시작
		case MotionEvent.ACTION_DOWN:
			if (valid) {
				selectionStartX = position[0];
				selectionStartY = position[1];
				selectionEndX = position[0];
				selectionEndY = position[1];
				selectionCenterX = position[0];
				selectionCenterY = position[1];
				tapDown = true;
			}
			break;

		// 선택중
		case MotionEvent.ACTION_MOVE:
			if (tapDown) {
				if (valid) {
					if (position[0] > selectionCenterX) {
						selectionStartX = selectionCenterX;
						selectionEndX = position[0];
					} else if (position[0] < selectionCenterX) {
						selectionEndX = selectionCenterX;
						selectionStartX = position[0];
					} else {
						selectionStartX = selectionCenterX;
						selectionEndX = selectionCenterX;
					}

					if (position[1] > selectionCenterY) {
						selectionStartY = selectionCenterY;
						selectionEndY = position[1];
					} else if (position[1] < selectionCenterY) {
						selectionEndY = selectionCenterY;
						selectionStartY = position[1];
					} else {
						selectionStartY = selectionCenterY;
						selectionEndY = selectionCenterY;
					}
				}
			}
			break;

		// 선택 완료
		case MotionEvent.ACTION_UP:
			if (tapDown) {
				tapDown = false;

				Time start = new Time(startTime.toString());
				start.addTime(selectionStartY, baseTime);

				Time end = new Time(startTime.toString());
				end.addTime(selectionEndY + 1, baseTime);

				if (addingMode) {
					dbA.open();
					for (int i = selectionStartX; i < selectionEndX + 1; i++) {
						ClassTime newTime = new ClassTime(i, start, end);
						dbA.addTime(selectedSubject, newTime);
					}
					dbA.close();

					update();
				}

				else {
					// 선택영역의 과목 선택
					dbA.open();

					Cursor times = dbA.getSelectedTimes(selectionStartX,
							selectionEndX, start.toMinute(), end.toMinute());

					ArrayList<String> selSub = new ArrayList<String>();

					int iName = times.getColumnIndex("subject");

					while (times.moveToNext()) {
						boolean duplicate = false;
						String sub = times.getString(iName);
						for (int i = 0; i < selSub.size(); i++) {
							if (selSub.get(i).compareTo(sub) == 0) {
								duplicate = true;
								break;
							}
						}
						if (!duplicate)
							selSub.add(sub);
					}
					times.close();
					dbA.close();

					if (selSub.size() > 0) {
						Intent intent = new Intent();
						intent.setAction("com.ajouroid.timetable.ADD_TIME");
						intent.putStringArrayListExtra("subject", selSub);
						context.sendBroadcast(intent);
					}
				}
			}
			break;

		}

		invalidate();
		return true;
	}

	public void setDay(int d) {
		day = d;
	}

	public void setSlot(int s) {
		maxslot = s;
	}

	public void setStartTime(String _time) {
		startTime = new Time(_time);
	}

	public void setEndTime(String _time) {
		endTime = new Time(_time);
	}

	public void setBaseTime(String _time) {
		baseTime = new Time(_time);
	}

	public void calculateSlots() {
		Time duration = startTime.duration(endTime);

		maxslot = duration.divide(baseTime);
	}

	boolean getPosition(float x, float y) {
		if (boxwidth == 0 || boxheight == 0)
			return false;
		int baseX = leftmost + timewidth;
		int baseY = topmost + dayheight;

		if (x < baseX) {
			position[0] = -1;
		} else {
			int realX = (int) x - baseX;
			int slotX = realX / boxwidth;

			if (slotX > day - 1)
				position[0] = -1;
			else
				position[0] = slotX;
		}

		if (y < baseY) {
			position[1] = -1;
		} else {
			int realY = (int) y - baseY;
			int slotY = realY / boxheight;

			if (slotY > maxslot)
				position[1] = -1;
			else
				position[1] = slotY;
		}

		if (position[0] > -1 && position[1] > -1)
			return true;
		else
			return false;
	}

	public void selectAdder(String subject) {
		addingMode = true;
		selectedSubject = subject;
		Toast.makeText(context, R.string.table_selectTime, Toast.LENGTH_SHORT)
				.show();
		invalidate();
	}

	public boolean isAddingMode() {
		return addingMode;
	}

	public void endAddingMode() {
		addingMode = false;
		invalidate();
	}

	public String toBitmap(String filename) {

		Bitmap bitmap = getImage(width, height, 5, 5, 5, 5);

		Canvas c = new Canvas(bitmap);
		c.clipRect(new Rect(0, 0, width - rightmargin, height - bottommargin));

		File file = new File("/sdcard/SmartTimeTable");
		if (!file.exists())
			file.mkdirs();
		String path = (new StringBuilder("/sdcard/SmartTimeTable/")
				.append(filename)).toString();
		File imgFile = new File(path);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(imgFile);

			if (fos != null)
				bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,
						100, fos);

			/*
			 * context.sendBroadcast( new
			 * Intent("android.intent.action.MEDIA_MOUNTED", Uri .parse((new
			 * StringBuilder("file://")).append(
			 * Environment.getExternalStorageDirectory()) .toString())));
			 */

			Toast.makeText(
					context,
					(new StringBuilder(getResources().getString(
							R.string.exportcomplete))).append(path).toString(),
					Toast.LENGTH_SHORT).show();
			fos.close();
			return path;
		} catch (Exception e) {
			Log.d("TimeTableWidget", e.toString());
		}
		return null;
	}

	public String toBitmap() {
		String filename = (new StringBuilder("Timetable_"))
				.append(System.currentTimeMillis()).append(".png").toString();

		return toBitmap(filename);
	}

	public Bitmap getBitmap() {
		// 현재 영역을 가져옴
		this.setDrawingCacheEnabled(true);
		this.buildDrawingCache();
		return this.getDrawingCache();
	}

	public boolean share() {
		String name = toBitmap("share.png");

		if (name != null)
			return true;
		else
			return false;
	}

	// 시간표를 그림
	public Bitmap getImage(int width, int height, int topMargin,
			int leftMargin, int rightMargin, int bottomMargin) {
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);

		calculateSlots();

		// 현재시간 계산
		Date now = new Date(System.currentTimeMillis());
		Time nowT = new Time(now.getHours(), now.getMinutes());

		int nowDay = now.getDay() - 1;
		if (nowDay < 0)
			nowDay = 6;

		float totalTime = endTime.toMinute() - startTime.toMinute();

		// 시작 좌표 지정
		int timetop = dayheight + topMargin;
		int timeleft = timewidth + leftMargin;

		int bottom = height - bottommargin;
		int rightside = width - rightMargin;

		// 한 칸의 크기
		boxwidth = (width - timewidth - leftMargin - rightMargin) / day;
		boxheight = (int) ((baseTime.toMinute() / totalTime) * (bottom - timetop));

		/*
		 * 선 페인트 객체
		 */
		Paint linePaint = new Paint();
		linePaint.setColor(lineColor);
		linePaint.setAlpha(0xFF);
		linePaint.setStyle(Style.STROKE);

		/*
		 * 반투명 채색 페인트
		 * 
		 * Paint alphaPaint = new Paint(); alphaPaint.setStyle(Style.FILL);
		 */

		/*
		 * 채우기 페인트
		 */
		Paint fillPaint = new Paint();
		fillPaint.setStyle(Style.FILL);

		/*
		 * 그림자 페인트
		 */
		Paint shadowPaint = null;

		/*
		 * 글꼴 페인트
		 */
		Paint font = new Paint();
		font.setColor(timeFontColor);
		font.setAlpha(0xFF);
		font.setAntiAlias(true);

		/*
		 * ====================== 전체배경 그림 ======================
		 */
		fillPaint.setColor(bgColor);

		fillPaint.setStyle(Style.FILL);

		// canvas.drawRect(new Rect(0,0,width,height), fillPaint);

		fillPaint.setAlpha(alphaValue);

		RectF bgRect = new RectF(leftMargin / 2, topMargin / 2, rightside
				+ leftMargin / 2, bottom + topMargin / 2);

		// 배경색
		canvas.drawRoundRect(bgRect, 10, 10, fillPaint);
		canvas.drawRoundRect(bgRect, 10, 10, linePaint);

		// 그림
		// canvas.drawBitmap(bgBitmap, 0, 0, new Paint());

		/*
		 * ====================== 시간표부분 배경 ======================
		 */
		fillPaint.setColor(mainBgColor);
		fillPaint.setAlpha(alphaValue);
		fillPaint.setStyle(Style.FILL);
		canvas.drawRect(new Rect(timeleft, timetop, rightside, bottom),
				fillPaint);

		linePaint.setColor(tableLineColor);
		linePaint.setAlpha(0xFF);

		/*
		 * ====================== 세로줄 | | | | | ======================
		 */
		for (int i = 0; i < day + 1; i++) {
			canvas.drawLine(timeleft + boxwidth * i, timetop, timeleft
					+ boxwidth * i, bottom, linePaint);
		}
		/*
		 * ====================== 가로줄 === ======================
		 */
		Time lineTime = new Time(startTime.toMinute());
		for (int i = 0; i < maxslot + 1; i++) {
			
			float topF = (((lineTime.toMinute() - startTime
					.toMinute()) / totalTime)
					* (bottom - timetop)
					+ timetop);
			canvas.drawLine(timeleft, topF, rightside, topF, linePaint);
			lineTime.addTime(baseTime);
		}

		/*
		 * ====================== 요일 배경 ======================
		 */
		fillPaint.setColor(timeColor);
		fillPaint.setAlpha(alphaValue);
		canvas.drawRect(new Rect(leftMargin, topMargin, rightside+1, timetop),
				fillPaint);
		canvas.drawRect(new Rect(leftMargin, timetop, timeleft, bottom+1),
				fillPaint);

		/*
		 * ====================== 요일 글자 ======================
		 */
		font.setTextSize(18);
		font.setTextAlign(Align.CENTER);
		for (int i = 0; i < day; i++) {
			canvas.drawText(days[i], timeleft + boxwidth * i + boxwidth / 2,
					timetop / 2 + 9, font);
		}

		/*
		 * ====================== 시간 배경 ======================
		 * 
		 * fillPaint.setColor(timeColor); fillPaint.setAlpha(0x33);
		 * canvas.drawRect(new Rect(leftMargin, timetop, timeleft, bottom),
		 * fillPaint);
		 */

		/*
		 * ====================== 시간 글자 ======================
		 */
		font.setTextSize(15);
		font.setTextAlign(Align.RIGHT);
		Time curTime = new Time(startTime.toString());

		boolean isAM = true;

		int slot = 0;
		String timeText = "AM ";
		Time noon = new Time("11:59");
		while (curTime.before(endTime) && slot <= maxslot) {
			if (curTime.before(noon))
				timeText += curTime.toString();
			else if (isAM) {
				isAM = false;
				timeText = curTime.to12Hour();
			} else {
				timeText = curTime.to12HourWithoutLetters();
			}
			int top = timetop
					+ (int) (slot * ((baseTime.toMinute() / totalTime) * (bottom - timetop)));
			if (top < bottom)
				canvas.drawText(timeText, timeleft - 5, top + 7, font);
			curTime.addTime(baseTime);
			slot++;
			timeText = "";
		}

		/*
		 * ====================== 과목 출력 ======================
		 */
		Paint subjectPaint = new Paint();
		subjectPaint.setStrokeWidth(1);
		subjectPaint.setAlpha(0xFF);

		if (alphaValue == 0) {
			subjectPaint.setStyle(Style.STROKE);
		} else {
			subjectPaint.setStyle(Style.FILL);
		}

		// 과목이름 출력용 폰트
		Paint subjectFont = new Paint();
		subjectFont.setColor(subFontColor);
		subjectFont.setAlpha(0xFF);
		subjectFont.setTextSize(18);
		subjectFont.setTextAlign(Align.CENTER);
		subjectFont.setAntiAlias(true);

		// 강의실 출력용 폰트
		Paint classFont = new Paint();
		classFont.setColor(subFontColor);
		classFont.setAlpha(0xFF);
		classFont.setTextSize(16);
		classFont.setTextAlign(Align.CENTER);
		classFont.setAntiAlias(true);

		Paint taskPaint = new Paint();
		taskPaint.setStyle(Style.FILL);

		int taskRadius = boxwidth / 20;

		// 현재 수업 출력용
		Subject nowSubject = null;
		RectF nowRect = null;
		RectF shadowRect = null;
		float sX = 0, sY = 0, eX = 0, eY = 0;
		float nSX = 0, nSY = 0, nEX = 0, nEY = 0;

		// 과목 출력 투명도

		final int listSize = subjectList.size();

		for (int i = 0; i < listSize; i++) {
			Subject subject = subjectList.get(i);

			int priority = checkTask(subject.getName());
			switch (priority) {
			case TaskAlert.HIGH:
				taskPaint.setColor(0xFFFF0000);
				break;
			case TaskAlert.MEDIUM:
				taskPaint.setColor(0xFFFF6000);
				break;
			case TaskAlert.LOW:
				taskPaint.setColor(0xFFAAAAAA);
				break;
			default:
				taskPaint.setColor(0);
			}

			// 해당 과목의 색으로 설정
			subjectPaint.setColor(subject.getColor());

			ArrayList<ClassTime> timeList = subject.getTime();
			final int timeSize = timeList.size();
			for (int j = 0; j < timeSize; j++) {
				ClassTime selectedTime = timeList.get(j);

				if (selectedTime.getDay() > day - 1)
					continue;
				// 시작시간과 종료시간
				Time classStartTime = selectedTime.getStartTime();
				Time classEndTime = selectedTime.getEndTime();

				if (startTime.toMinute() > classStartTime.toMinute()) {
					if (classEndTime.toMinute() < startTime.toMinute())
						continue;
					else
						classStartTime = startTime;
				}

				if (endTime.toMinute() < classEndTime.toMinute()) {
					if (classStartTime.toMinute() > endTime.toMinute())
						continue;
					else
						classEndTime = endTime;
				}
				// 그릴 좌표
				sX = timeleft + selectedTime.getDay() * boxwidth + 1;
				sY = (((classStartTime.toMinute() - startTime
						.toMinute()) / totalTime)
						* (bottom - timetop)
						+ timetop);
				eX = sX + boxwidth - 1;
				eY = (((classEndTime.toMinute() - startTime
						.toMinute()) / totalTime) * (bottom - timetop) + timetop) - 1;

				// int timeWidth = eX - sX;
				float timeHeight = eY - sY;

				if (selectedTime.isNow(nowDay, nowT)) {
					nowSubject = new Subject(subject.getName(),
							subject.getClassRoom(), subject.getProfessor(),
							subject.getEmail(), subject.getColor());
					nSX = sX - 5;
					nSY = sY - 5;
					nEX = eX;
					nEY = eY;
					nowRect = new RectF(nSX, nSY, nEX, nEY);
					shadowRect = new RectF(nSX + 5, nSY + 5, nEX + 5, nEY + 5);
					shadowPaint = new Paint();
					shadowPaint.setColor(shadowColor);
					shadowPaint.setAlpha(127);
					continue;
				}

				RectF subjectRect = new RectF(sX, sY, eX, eY);

				canvas.drawRoundRect(subjectRect, 5, 5, subjectPaint);

				if (priority > 0) {
					float cX = sX + taskRadius * 2;
					float cY = sY + taskRadius * 2;
					canvas.drawCircle(cX, cY, taskRadius, taskPaint);
				}

				Bitmap textBitmap = Bitmap.createBitmap(boxwidth, (int)timeHeight,
						Bitmap.Config.ARGB_8888);
				Canvas textCanvas = new Canvas(textBitmap);

				// 텍스트를 여러줄로 나눔
				String name = subject.getName();
				String[] subjectLines = seperateLines(name, boxwidth,
						subjectFont);
				int subjectLineCnt = subjectLines.length;

				String classroom = subject.getClassRoom();
				String[] classLines = seperateLines(classroom, boxwidth,
						classFont);
				int classLineCnt = classLines.length;

				int subHeight = (int) ((subjectFont.descent() - subjectFont
						.ascent()) * (subjectLineCnt)) + 5;
				int classHeight = (int) ((classFont.descent() - classFont
						.ascent()) * (classLineCnt) - classFont.ascent());

				// 영역 안에 들어가는 경우
				if (subHeight < (timeHeight/2 - taskRadius*3) && classHeight < timeHeight / 2) {
					for (int l = 0; l < subjectLineCnt; l++) {
						textCanvas.drawText(
								subjectLines[l],
								boxwidth / 2,
								timeHeight / 2
										- ((subjectFont.descent() - subjectFont
												.ascent()) * (subjectLineCnt
												- l - 1)) - 5, subjectFont);
					}

					for (int l = 0; l < classLineCnt; l++) {
						textCanvas.drawText(
								classLines[l],
								boxwidth / 2,
								timeHeight / 2
										+ ((classFont.descent() - classFont
												.ascent()) * (l + 1)),
								classFont);
					}
				}

				// 들어가지 못하는 경우 위에서부터 출력
				else {
					for (int l = 0; l < subjectLineCnt; l++) {
						textCanvas
								.drawText(
										subjectLines[l],
										boxwidth / 2,
										taskRadius * 3 + (((subjectFont.descent() - subjectFont
												.ascent())) * l)
												- subjectFont.ascent(),
										subjectFont);
					}

					int classTop = (int) ((subjectFont.descent() - subjectFont
							.ascent()) * (subjectLineCnt + 1));

					for (int l = 0; l < classLineCnt; l++) {
						textCanvas.drawText(
								classLines[l],
								boxwidth / 2,
								taskRadius * 3 + classTop
										+ (classFont.descent() - classFont
												.ascent()) * l, classFont);
					}
				}

				canvas.drawBitmap(textBitmap, null, new RectF(sX, sY, eX, eY), null);
			}
		}

		if (nowSubject != null) {
			float timeHeight = nEY - nSY;
			float timeWidth = nEX - nSX;

			subjectPaint.setColor(nowSubject.getColor());

			if (alphaValue == 0) {
				subjectPaint.setStyle(Style.FILL);

				subjectFont.setColor(0);
				subjectFont.setAlpha(0xFF);
				classFont.setColor(0);
				classFont.setAlpha(0xFF);
			}

			canvas.drawRoundRect(shadowRect, 5, 5, shadowPaint);

			canvas.drawRoundRect(nowRect, 5, 5, subjectPaint);

			if (alphaValue > 0) {
				canvas.drawRoundRect(nowRect, 5, 5, linePaint);
			}

			int priority = checkTask(nowSubject.getName());
			switch (priority) {
			case TaskAlert.HIGH:
				taskPaint.setColor(0xFFFF0000);
				break;
			case TaskAlert.MEDIUM:
				taskPaint.setColor(0xFFFF6000);
				break;
			case TaskAlert.LOW:
				taskPaint.setColor(0xFFAAAAAA);
				break;
			default:
				taskPaint.setColor(0);
			}

			if (priority > 0) {
				float cX = nSX + taskRadius * 2;
				float cY = nSY + taskRadius * 2;
				canvas.drawCircle(cX, cY, taskRadius, taskPaint);
			}

			Bitmap textBitmap = Bitmap.createBitmap((int)timeWidth, (int)timeHeight,
					Bitmap.Config.ARGB_8888);
			Canvas textCanvas = new Canvas(textBitmap);

			String name = nowSubject.getName();
			String[] subjectLines = seperateLines(name, boxwidth, subjectFont);
			int subjectLineCnt = subjectLines.length;

			String classroom = nowSubject.getClassRoom();
			String[] classLines = seperateLines(classroom, boxwidth, classFont);
			int classLineCnt = classLines.length;

			int subHeight = (int) ((subjectFont.descent()
					- subjectFont.ascent() + 1) * (subjectLineCnt)) + 5;
			int classHeight = (int) ((classFont.descent() - classFont.ascent())
					* (classLineCnt) - classFont.ascent());

			// 영역 안에 들어가는 경우
			if (subHeight < timeHeight / 2 && classHeight < timeHeight / 2) {
				for (int l = 0; l < subjectLineCnt; l++) {
					textCanvas
							.drawText(
									subjectLines[l],
									boxwidth / 2,
									timeHeight
											/ 2
											- ((subjectFont.descent() - subjectFont
													.ascent()) * (subjectLineCnt
													- l - 1)) - 5, subjectFont);
				}

				for (int l = 0; l < classLineCnt; l++) {
					textCanvas.drawText(
							classLines[l],
							boxwidth / 2,
							timeHeight
									/ 2
									+ ((classFont.descent() - classFont
											.ascent()) * (l + 1)), classFont);
				}
			}

			// 들어가지 못하는 경우 위에서부터 출력
			else {
				for (int l = 0; l < subjectLineCnt; l++) {
					textCanvas
							.drawText(
									subjectLines[l],
									boxwidth / 2,
									(((1 + subjectFont.descent() - subjectFont
											.ascent())) * l)
											- subjectFont.ascent(), subjectFont);
				}

				int classTop = (int) ((1 + subjectFont.descent() - subjectFont
						.ascent()) * (subjectLineCnt + 1)) + 5;

				for (int l = 0; l < classLineCnt; l++) {
					textCanvas.drawText(classLines[l], boxwidth / 2, classTop
							+ (1 + classFont.descent() - classFont.ascent())
							* l, classFont);
				}
			}

			canvas.drawBitmap(textBitmap, null, new RectF(nSX, nSY + taskRadius
					* 2, nEX, nEY + taskRadius * 2), null);
		}

		/*
		 * Paint timeLine = new Paint(); timeLine.setColor(timeLineColor);
		 * timeLine.setAlpha(0x3F); timeLine.setStrokeWidth(3);
		 * 
		 * if (!(nowT.before(startTime) || endTime.before(nowT))) { float
		 * nowTime = nowT.toMinute() - startTime.toMinute();
		 * 
		 * float timeRate = nowTime / totalTime;
		 * 
		 * int timeY = (int) (timeRate * (bottom - timetop)) + timetop;
		 * 
		 * canvas.drawLine(leftMargin, timeY, rightside, timeY, timeLine); }
		 */

		return bitmap;
	}

	public void getWidgetImage(int width, int height) {
		Bitmap bitmap = getImage(width, height, 10, 10, 10, 10);

		SharedPreferences prefs = this.getContext().getSharedPreferences(
				"com.ajouroid.timetable_preferences", 0);

		double opacity = Double.parseDouble(prefs.getString("opacity", "100"));

		double op = opacity / 100;

		if (op < 1) {
			Bitmap transBitmap = Bitmap.createBitmap(width, height,
					Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(transBitmap);
			Paint tP = new Paint();
			int alpha = (int) (255 * op);
			tP.setAlpha(alpha);
			c.drawBitmap(bitmap, 0, 0, tP);

			bitmap = transBitmap;
		}

		// 파일 저장
		String abspath = "/sdcard/SmartTimeTable/widget";
		File file = new File(abspath);
		if (!file.exists()) {
			file.mkdirs();
			try {
				File nomedia = new File(abspath + "/.nomedia");
				nomedia.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		String path = abspath + "/widget.png";

		File imgFile = new File(path);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(imgFile);

			if (fos != null)
				bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,
						100, fos);

			fos.close();

		} catch (Exception e) {
			Log.d("TimeTableWidget", e.toString());
		}
	}

	public void update() {
		subjectList = new ArrayList<Subject>();
		dbA.open();
		Cursor c = dbA.getSubjectCursor();

		int iName = c.getColumnIndex("name");
		int iClass = c.getColumnIndex("classroom");
		int iProf = c.getColumnIndex("professor");
		int iEmail = c.getColumnIndex("email");
		int iColor = c.getColumnIndex("color");

		while (c.moveToNext()) {
			Subject newSubject = new Subject(c.getString(iName),
					c.getString(iClass), c.getString(iProf),
					c.getString(iEmail), c.getInt(iColor));

			Cursor timeCursor = dbA.getTimeCursor(c.getString(iName));
			int iDay = timeCursor.getColumnIndex("day");
			int iStart = timeCursor.getColumnIndex("starttime");
			int iEnd = timeCursor.getColumnIndex("endtime");
			while (timeCursor.moveToNext()) {
				ClassTime newTime = new ClassTime(timeCursor.getInt(iDay),
						new Time(timeCursor.getInt(iStart)), new Time(
								timeCursor.getInt(iEnd)));
				newSubject.addTime(newTime);
			}
			timeCursor.close();
			subjectList.add(newSubject);
		}

		c.close();
		dbA.close();

		invalidate();
	}

	HashMap<String, Integer> taskTable;

	public void notifyTask(TaskAlert task) {
		taskTable.put(task.getSubject(), task.getPriority());
	}

	public int checkTask(String subjectName) {
		// 해당과목의 작업을 불러옴
		dbA.open();
		Cursor taskCursor = dbA.getTaskCursor(subjectName);
		long remain = 0;

		// 현재 시간
		Date now = new Date(System.currentTimeMillis());

		int iDate = taskCursor.getColumnIndex("taskdate");
		int iUseTime = taskCursor.getColumnIndex("usetime");

		boolean useTime = false;

		SimpleDateFormat format = new SimpleDateFormat(getResources()
				.getString(R.string.dateformat), Locale.US);

		while (taskCursor.moveToNext()) {
			String dateStr = taskCursor.getString(iDate);
			try {
				// DB에서 가져온 날짜를 Date로 parse
				Date selectedDate = format.parse(dateStr);
				if (selectedDate.before(now))
					continue;

				long between = DBAdapter.distance(selectedDate, now);

				// 처음 나왔거나, 기존의 일정보다 먼저 오는 일정인 경우
				if (between < remain || remain == 0) {
					remain = between;
					// 시간 사용 여부
					if (taskCursor.getInt(iUseTime) > 0) {
						useTime = true;
					} else
						useTime = false;
				}

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		taskCursor.close();

		// 초 단위로 변경
		remain = remain / 1000;

		int priority = 0;
		// 다가오는 일정이 있다면
		if (remain > 0) {
			priority = TaskAlert.LOW;
			// 일 단위 (하루 : 86400초)
			if (remain > 86400) {
				// 남은 일수를 계산
				remain = remain / 86400;
			}
			// 시간을 지정했다면 시간 단위 표시
			else if (useTime) {
				// 시간 단위 (1시간 : 3600초)
				if (remain > 3600) {
					remain = (remain + 1800) / (3600);
					priority = TaskAlert.MEDIUM;
				}
				// 분 단위 (1분 : 60초)
				else if (remain > 60) {
					remain = (remain + 30) / 60;
					priority = TaskAlert.HIGH;
				}
				// 초 단위
				else {
					priority = TaskAlert.HIGH;
				}
			}
			// 시간을 지정하지 않았다면 오늘로 표시
			else {
				priority = TaskAlert.HIGH;
			}
		}
		dbA.close();
		return priority;
	}
}
