package com.ajouroid.timetable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

public class YahooAddress {
	//<latitude>37.131055495722</latitude><longitude>127.08099699644</longitude><name>오산자이APT</name><street/><city>청호동</city><county>오산시</county><state>경기도</state><country>South Korea</country>
	double latitude;
	double longitude;
	String name;
	String street;
	String city;
	String county;
	String state;
	
	public YahooAddress()
	{
		name = "";
		street = "";
		city = "";
		county = "";
		state = "";
	}
	
	public synchronized ArrayList<YahooAddress> findAddress(String keyword) throws XmlPullParserException, IOException
	{
		ArrayList<YahooAddress> addressList = new ArrayList<YahooAddress>();
		YahooAddress adder = new YahooAddress();
		XmlPullParserFactory baseparser = XmlPullParserFactory.newInstance();
		baseparser.setNamespaceAware(true);
		XmlPullParser xpp = baseparser.newPullParser();
		
		String apiKey=URLEncoder.encode(Keyring.YAHOO_KEY);
		
		String encodedKeyword = URLEncoder.encode(keyword);

		String url = String.format("http://kr.open.gugi.yahoo.com/service/poi.php?appid=%s&q=%s&encoding=euc-kr&output=xml&results=50", apiKey, encodedKeyword);

		URL requestURL = new URL(url);
		InputStream input = requestURL.openStream();
		xpp.setInput(input,"UTF-8");

		int parserEvent = xpp.getEventType();
		parserEvent=xpp.next();//파싱한  자료에서 다음 라인으로 이동 

		while(parserEvent != XmlPullParser.END_DOCUMENT){
			switch(parserEvent) {
			case XmlPullParser.END_TAG: //xml의 </> 이부분을 만나면 실행되게 됩니다.					
				break;
			case XmlPullParser.START_TAG: //xml의 <> 부분을 만나게 되면 실행되게 됩니다.
				if (xpp.getName().compareTo("locations") == 0)
				{
					parserEvent = xpp.next();
					while(true)
					{
						if (parserEvent == XmlPullParser.START_TAG)
						{
							String tag = xpp.getName();
							if (tag.compareTo("item") == 0)
							{
								adder = new YahooAddress();
							}
							else if (tag.compareTo("latitude") == 0)
							{
								xpp.next();
								adder.setLatitude(Double.parseDouble(xpp.getText()));
							}
							else if (tag.compareTo("longitude") == 0)
							{
								xpp.next();
								adder.setLongitude(Double.parseDouble(xpp.getText()));
							}
							
							else if (tag.compareTo("name") == 0)
							{
								xpp.next();
								adder.setName(xpp.getText());
							}
							
							else if (tag.compareTo("street") == 0)
							{
								xpp.next();
								adder.setStreet(xpp.getText());
							}
							
							else if (tag.compareTo("city") == 0)
							{
								xpp.next();
								adder.setCity(xpp.getText());
							}
							
							else if (tag.compareTo("county") == 0)
							{
								xpp.next();
								adder.setCounty(xpp.getText());
							}
							
							else if (tag.compareTo("state") == 0)
							{
								xpp.next();
								adder.setState(xpp.getText());
							}
						}
						else if (parserEvent == XmlPullParser.END_TAG)
						{
							if (xpp.getName().compareTo("item") == 0)
							{
								addressList.add(adder);
							}
							if (xpp.getName().compareTo("locations") == 0)
							{
								break;
							}
						}
						
						parserEvent = xpp.next();
					}
				}
			}
			parserEvent = xpp.next(); //다음 태그를 읽어 들입니다.
		}
		Log.d("YahooAddress", addressList.size() + " addresses found.");
		return addressList;
	}
	
	public String getAddress()
	{
		String str = "";
		
		if (state != null)
		{
			str += state;
		}
		if (county != null)
		{
			str += " " + county;
		}
		if (city != null)
		{
			str += " " + city;
		}
		if (street != null)
		{
			str += " " + street;
		}
		if (name != null)
		{
			str += " " + name;
		}
		
		return str;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
	
}
