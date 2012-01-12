package com.ajouroid.timetable;

import java.io.Serializable;
import java.util.ArrayList;

public class Subject implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8023547353714569290L;
	
	String name;
	String classRoom;
	int color;
	String professor;
	String email;
	ArrayList<ClassTime> times = new ArrayList<ClassTime>();
	
	// TODO : 더 많은 변수들 추가
	
	public Subject()
	{
		
	}
	
	public Subject(String _name, String _classRoom, String _professor, String _email, int _color)
	{
		name = _name;
		classRoom = _classRoom;
		color = _color;
		professor = _professor;
		email = _email;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String _name)
	{
		name = _name;
	}
	
	public String getClassRoom()
	{
		return classRoom;
	}
	
	public void setClassRoom(String _classRoom)
	{
		classRoom = _classRoom;
	}
	
	public String getProfessor()
	{
		return professor;
	}
	
	public void setProfessor(String _professor)
	{
		professor = _professor;
	}
	
	public String getEmail()
	{
		return email;
	}
	
	public void setEmail(String _email)
	{
		email = _email;
	}
	
	public int getColor()
	{
		return color;
	}
	
	public void setColor(int _color)
	{
		color = _color;
	}
	
	public void addTime(int day, Time startTime, Time endTime)
	{
		times.add(new ClassTime(day, startTime, endTime));
	}
	
	public void addTime(ClassTime time)
	{
		times.add(time);
	}
	
	public void removeTime(ClassTime time)
	{
		times.remove(time);
	}
	
	public ArrayList<ClassTime> getTime()
	{
		return times;
	}
}
