package com.ajouroid.timetable;

public class TaskAlert {

	String subject;
	int priority;
	
	public final static int HIGH = 3;
	public final static int MEDIUM = 2;
	public final static int LOW = 1;
	
	public TaskAlert()
	{
		
	}
	
	public TaskAlert(String _subject, int _priority)
	{
		subject = _subject;
		priority = _priority;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
}
