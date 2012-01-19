package com.ajouroid.timetable;

public class AlarmTime {
	String subject;
	int day;
	Time startTime;
	
	boolean today = false;
	
	public AlarmTime(){
		
	}
	public AlarmTime(int _day, Time _startTime)
	{
		day = _day;
		startTime = _startTime; 
	}
	public AlarmTime(int _day, int _starttime) 
	{
		day = _day;
		startTime = new Time(_starttime);
	}
	
	
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public Time getStartTime() {
		return startTime;
	}
	public void setStartTime(Time startTime) {
		this.startTime = startTime;
	}
	
	public Time subTime(Time sub)
	{
		Time t = new Time();
		t = startTime.subTime(sub);
		
		return t;
	}
	
	public void setIsToday(boolean val)
	{
		today = val;
	}
	
	public boolean isToday()
	{
		return today;
	}
	
}
