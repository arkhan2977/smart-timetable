package com.ajouroid.timetable;

import java.io.*;

public class Task implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6755868785839366731L;

	String subject;
	String taskDate;
	String name;
	int Type;
	
	int _id;
	
	boolean usetime;
	
	long remain;
	

	public Task(String _subject, String title, String _taskDate, int _Type)
	{
		subject = _subject;
		name = title;
		taskDate = _taskDate;
		Type = _Type;
	}
	public int getType() {
		return Type;
	}
	public void setType(int _type) {
		Type = _type;
	}
	public String getTaskDate() {
		return taskDate;
	}
	public void setTaskDate(String _taskDate) {
		this.taskDate = _taskDate;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public int getId()
	{
		return _id;
	}
	public void setId(int id)
	{
		_id = id;
	}
	public boolean isUsetime() {
		return usetime;
	}
	public void setUsetime(boolean usetime) {
		this.usetime = usetime;
	}
	public long getRemain() {
		return remain;
	}
	public void setRemain(long remain) {
		this.remain = remain;
	}
	
	
	
}
