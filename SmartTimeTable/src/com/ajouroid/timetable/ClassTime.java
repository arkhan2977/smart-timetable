package com.ajouroid.timetable;

import java.io.Serializable;

public class ClassTime implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7184687445787806036L;
	
	int day;
	Time startTime;
	Time endTime;
	
	String subject;
	
	public ClassTime()
	{
		
	}
	
	public ClassTime(int _day, Time _startTime, Time _endTime)
	{
		day = _day;
		startTime = _startTime;
		endTime = _endTime;
	}
	
	public int getDay()
	{
		return day;
	}
	
	public void setDay(int _day)
	{
		day = _day;
	}
	
	public Time getStartTime()
	{
		return startTime;
	}
	
	public void setStartTime(Time _startTime)
	{
		startTime = _startTime;
	}
	
	public Time getEndTime()
	{
		return endTime;
	}
	
	public void setEndTime(Time _endTime)
	{
		endTime = _endTime;
	}
	
	public boolean isDuplicatedWith(ClassTime target)
	{
		if (target.getDay() != day)
			return false;
		
		else
		{
			int start = startTime.toMinute();
			int end = endTime.toMinute();
			
			int targetStart = target.getStartTime().toMinute();
			int targetEnd = target.getEndTime().toMinute();
			
			if (start == targetStart)
				return true;
			else if (start < targetStart)
			{
				if (targetStart < end)
				{
					return true;
				}
				else return false;
			}
			else // start > targetStart
			{
				if (start < targetEnd)
				{
					return true;
				}
				else return false;
			}
		}
	}
	
	public boolean isNow(int nowDay, Time nowTime)
	{
		if (day != nowDay)
			return false;
		
		if (startTime.before(nowTime) && nowTime.before(endTime))
			return true;
		else return false;
	}
	
	
	public void setSubject(String name)
	{
		subject = name;
	}
	
	public String getSubject()
	{
		return subject;
	}
}
