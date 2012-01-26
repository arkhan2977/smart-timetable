import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.*;

import org.w3c.dom.*;

public class DBDownloader {

	//final String KEY = "O904OQN755V3irRmS5Hmqux3lB/xFZVw6b7Pb7RORO9kElznBFFIj3Kn4xkDCKOuHaT97ceTxM1hchELb6qwyA==";
	final String KEY = "1234567890";
	final String baseurl = "http://openapi.gbis.go.kr/ws/rest/baseinfoservice";
	
	String[] urls;
	
	int version=-1;
	
	public int checkVersion() {
		
		urls = new String[4];
		try {
			String key = URLEncoder.encode(KEY, "UTF-8");
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			
			DocumentBuilder db = dbf.newDocumentBuilder();

			String urlStr = baseurl + "?serviceKey=" + key;
			
			System.out.println("Base Information Check");
			System.out.println("URL: " + urlStr);

			URL requestURL = new URL(urlStr);
			InputStream input = requestURL.openStream();
			
			Document doc = db.parse(input);

			Element root = doc.getDocumentElement();
			NodeList sub = doc.getChildNodes();

			for (int i=0; i<sub.getLength(); i++)
			{
				Node subNode = sub.item(i);
				parse(subNode);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		
		for (int i=0; i<4; i++)
		{
			System.out.println("URL" + (i+1) + " : " + urls[i]);
		}
		
		downloadInfo();
		
		return version;
	}
	
	public void downloadInfo()
	{
		byte[] buffer = new byte[8192];
		
		for (int i=0; i<4; i++)
		{
			try {
				File f = new File("gen\\" + i + ".txt");

				FileOutputStream fos = new FileOutputStream(f);
				
				URL downloadUrl = new URL(urls[i]);
				InputStream is = downloadUrl.openStream();
				
				int n;
				System.out.println("Download Start: " + urls[i]);
				while((n=is.read(buffer)) > 0)
				{
					fos.write(buffer, 0, n);
				}
				System.out.println("Download Complete.");
				is.close();
				fos.flush();
				fos.close();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			File verF = new File("gen\\version.txt");
			FileOutputStream verFos = new FileOutputStream(verF);
			verFos.write(Byte.parseByte(version + ""));
			verFos.flush();
			verFos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void parse(Node node)
	{
		int length = node.getChildNodes().getLength();
		if (length > 0)
		{
			switch(node.getNodeName())
			{
			case "areaDownloadUrl":
				urls[0] = node.getFirstChild().getTextContent();
				break;
			case "routeDownloadUrl":
				urls[1] = node.getFirstChild().getTextContent();
				break;
			case "routeStationDownloadUrl":
				urls[3] = node.getFirstChild().getTextContent();
				break;
			case "stationDownloadUrl":
				urls[2] = node.getFirstChild().getTextContent();
				break;
				
			case "areaVersion":
				version = Integer.parseInt(node.getFirstChild().getTextContent());
				break;
			}
			
			for (int i=0; i<length; i++)
				parse(node.getChildNodes().item(i));
		}
	}
}
