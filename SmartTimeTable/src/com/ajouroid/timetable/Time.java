package com.ajouroid.timetable;

import java.io.Serializable;

public class Time implements Serializable {

	private static final long serialVersionUID = -1768365194837619405L;

	private int hour;
	private int minute;

	public static final int LESS = 0;
	public static final int EQUAL = 1;
	public static final int BIGGER = 2;

	public Time() {

	}

	public Time(String _time) {
		String[] times = _time.split(":");

		hour = Integer.parseInt(times[0].trim());
		minute = Integer.parseInt(times[1].trim());
	}

	public Time(int _minute)
	{
		hour = _minute / 60;
		minute = _minute % 60;
	}
	public Time(int _hour, int _minute) {
		hour = _hour;
		minute = _minute;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int _hour) {
		hour = _hour;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int _minute) {
		minute = _minute;
	}

	public void addTime(Time adder) {
		hour += adder.getHour();

		minute += adder.getMinute();

		if (minute >= 60) {
			hour++;
			minute -= 60;
		}
	}

	public int toMinute() {
		return hour * 60 + minute;
	}

	public void addTime(int times, Time base) {
		int newHour;

		int newMinute = base.getMinute() * times;

		newHour = newMinute / 60;
		newMinute %= 60;

		newHour += base.getHour() * times;

		hour += newHour;
		minute += newMinute;
	}

	public boolean before(Time rTime) {
		if (hour < rTime.getHour())
			return true;
		else if (hour == rTime.getHour()) {
			if (minute <= rTime.getMinute())
				return true;
		}
		return false;
	}

	public Time duration(Time end) {
		int newHour = end.getHour() - hour;
		int newMinute = end.getMinute() - minute;

		if (newMinute < 0) {
			newMinute *= -1;
			newHour--;
		}

		Time rTime = new Time(newHour, newMinute);

		return rTime;
	}

	public int divide(Time divider) {
		int thisTime = hour * 60 + minute;

		int divTime = divider.getHour() * 60 + divider.getMinute();

		return thisTime / divTime;
	}

	public Time subTime(Time sub) {
		int newHour = hour - sub.getHour();
		int newMinute = minute - sub.getMinute();

		if (newMinute < 0) {
			newHour--;
			newMinute = 60+newMinute;
		}

		return new Time(newHour, newMinute);
	}

	@Override
	public String toString() {
		String rTime = String.format("%d:%02d", hour, minute);

		return rTime;
	}

	public String to12Hour() {
		String ampm;
		int newHour = hour;
		if (hour < 12) {
			ampm = "AM";
		} else {
			if (hour > 12)
				newHour = hour - 12;
			ampm = "PM";
		}

		return String.format("%s %d:%02d", ampm, newHour, minute);
	}
	
	public String to12HourWithoutLetters()
	{
		int newHour = hour;
		if (hour > 12)
			newHour = hour - 12;

		return String.format("%d : %02d", newHour, minute);
	}

	public int compare(Time rTime) {
		int minute = this.toMinute();
		int targetMinute = rTime.toMinute();

		if (minute < targetMinute)
			return LESS;
		else if (minute == targetMinute) {
			return EQUAL;
		}

		else {
			return BIGGER;
		}
	}
}
