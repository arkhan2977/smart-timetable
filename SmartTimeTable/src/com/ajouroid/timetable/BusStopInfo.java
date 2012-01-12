package com.ajouroid.timetable;

import java.util.ArrayList;

public class BusStopInfo {

	String stop_id;
	String stop_name;
	double latitude;
	double longitude;
	int number;
	int distance;
	String updown;
	String region;
	
	ArrayList<BusInfo> busList;
	
	public BusStopInfo(String id, String name, String _updown)
	{
		stop_id = id;
		stop_name = name;
		updown = _updown;
	}
	
	public BusStopInfo(String id, String name, String _region, double _lat, double _long, int _number,int _distance)
	{
		stop_id = id;
		stop_name = name;
		region = _region;
		latitude = _lat;
		longitude = _long;
		distance = _distance;
		number = _number;
		
		busList = new ArrayList<BusInfo>();
	}

	public String getStop_id() {
		return stop_id;
	}

	public void setStop_id(String stop_id) {
		this.stop_id = stop_id;
	}

	public String getStop_name() {
		return stop_name;
	}

	public void setStop_name(String stop_name) {
		this.stop_name = stop_name;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}
	
	public int getDistance(){
		return distance;
	}

	public ArrayList<BusInfo> getBusList() {
		return busList;
	}

	public void setBusList(ArrayList<BusInfo> busList) {
		this.busList = busList;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getUpdown() {
		return updown;
	}

	public void setUpdown(String updown) {
		this.updown = updown;
	}
	
	
}
