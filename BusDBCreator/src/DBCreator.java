import java.sql.Statement;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.zip.*;

import org.sqlite.*;

public class DBCreator {

	public void dbDownloader(String[] fileName) {
		final int bufsize = 8192;

		String query = null;

		try {
			String dbPath = "gen\\timetable_bus.db";

			File dbFile = new File(dbPath);
			if (dbFile.exists()) {
				dbFile.delete();
			}
			Class.forName("org.sqlite.JDBC");

			SQLiteConfig config = new SQLiteConfig();
			config.enableLoadExtension(true);

			Connection dbCon = DriverManager.getConnection("jdbc:sqlite:"
					+ dbPath, config.toProperties());

			Statement dbStmt = dbCon.createStatement();
			char[] buf = new char[bufsize];
			query = "CREATE TABLE android_metadata (locale TEXT);";
			dbStmt.executeUpdate(query);
			query = "INSERT INTO android_metadata VALUES('en_US');";
			dbStmt.executeUpdate(query);		
			

			for (int i = 0; i < fileName.length; i++) {
				try {
					switch(i)
					{
					case 0:
						query = "CREATE TABLE area ("
								+ "CENTER_ID INTEGER PRIMARY KEY,"
								+ "AREA_ID INTEGER,"
								+ "AREA_NAME VARCHAR(32));\r\n";
						break;
					case 1:
						query = "CREATE TABLE route ("
								+ "ROUTE_ID INTEGER PRIMARY KEY,"
								+ "ROUTE_NM VARCHAR(8),"
								//+ "ROUTE_TP INTEGER,"
								//+ "ST_STA_ID INTEGER,"
								//+ "ST_STA_NM TEXT,"
								//+ "ST_STA_NO INTEGER,"
								//+ "ED_STA_ID INTEGER,"
								//+ "ED_STA_NM TEXT,"
								//+ "ED_STA_NO INTEGER,"
								+ "UP_FIRST_TIME VARCHAR(5),"
								+ "UP_LAST_TIME VARCHAR(5),"
								+ "DOWN_FIRST_TIME VARCHAR(5),"
								+ "DOWN_LAST_TIME VARCHAR(5),"
								+ "PEEK_ALLOC INTEGER,"
								+ "NPEEK_ALLOC INTEGER,"
								//+ "COMPANY_ID INTEGER,"
								//+ "COMPANY_NM VARCHAR(32),"
								//+ "TEL_NO VARCHAR(16),"
								+ "REGION_NAME TEXT"
								//+ "DISTRICT_CD INTEGER
								+ ");\r\n";
						break;
					case 2:
						query = "CREATE TABLE station ("
								+ "STATION_ID INTEGER PRIMARY KEY,"
								+ "STATION_NM TEXT,"
								//+ "CENTER_ID INTEGER,"
								//+ "CENTER_YN CHAR,"
								+ "LNG DOUBLE,"
								+ "LAT DOUBLE,"
								+ "REGION_NAME TEXT,"
								+ "STATION_NO INTEGER"
								//+ "DISTRICT_CD INTEGER"
								+ ");\r\n";
						break;
					case 3:
						query = "CREATE TABLE routestation ("
								+ "_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
								+ "ROUTE_ID INTEGER,"
								+ "STATION_ID INTEGER,"
								+ "UPDOWN VARCHAR(2),"
								+ "STA_ORDER INTEGER,"
								+ "ROUTE_NM VARCHAR(8),"
								+ "STATION_NM TEXT,"
								+ "FOREIGN KEY(STATION_ID) REFERENCES station(STATION_ID) ON DELETE CASCADE ON UPDATE CASCADE, "
								+ "FOREIGN KEY(ROUTE_ID) REFERENCES route(ROUTE_ID) ON DELETE CASCADE ON UPDATE CASCADE)\r\n";
						break;
					}
					dbStmt.executeUpdate(query);
					
					File f = new File(fileName[i]);

					long size = f.length();

					System.out.println(fileName[i] + " size: " + size);
					StringBuffer strBuffer = new StringBuffer((int) size);

					InputStreamReader ir = new InputStreamReader(
							new FileInputStream(fileName[i]), "EUC-KR");
					BufferedReader br = new BufferedReader(ir);

					int n;

					while ((n = br.read(buf)) != -1) {
						strBuffer.append(buf);
						buf = new char[bufsize];
					}

					br.close();

					String result = strBuffer.toString();
					String[] lines = result.split("\\^");

					dbStmt.executeUpdate("BEGIN TRANSACTION;\r\n");

					for (int j = 1; j < lines.length; j++) {
						String[] parts = lines[j].split("\\|");
						query = "insert into ";
						switch (i) {
						case 0:
							query += "area(CENTER_ID, AREA_ID, AREA_NAME) \r\n";
							query += "values (";
							if (parts.length != 3)
							{
								System.out.println("Error in file " + i + ": " + lines[j].trim());
								continue;
							}
							for (int k = 0; k < parts.length; k++) {
								if (k > 0)
									query += ", ";
								query += "'" + parts[k].trim() + "'";
							}
							query += ");\r\n";
							break;
						case 1:
							if (parts.length != 20)
							{
								System.out.println("Error in file " + i + ": " + lines[j].trim());
								continue;
							}
							query += "route(ROUTE_ID, ROUTE_NM, " +
									//"ROUTE_TP, " + 
									//ST_STA_ID, ST_STA_NM, ST_STA_NO, ED_STA_ID, ED_STA_NM, ED_STA_NO, " +
									"UP_FIRST_TIME, UP_LAST_TIME, DOWN_FIRST_TIME, DOWN_LAST_TIME, PEEK_ALLOC, NPEEK_ALLOC, " +
									//"COMPANY_ID, COMPANY_NM, TEL_NO, " +
									"REGION_NAME" +
									//",DISTRICT_CD " +
									") \r\n";
							query += "values (" + 
									"'" + parts[0] + "', '" + parts[1] + "', '" + //parts[2] + "', '" +
									parts[9] + "', '" + parts[10] + "', '" + parts[11] + "', '" + parts[12] + "', '" + 
									parts[13] + "', '" + parts[14] + "', '" + 
									parts[18] + "'";
							/*
							for (int k = 0; k < parts.length; k++) {
								if (k > 0)
									query += ", ";
								query += "'" + parts[k].trim() + "'";
							}*/
							query += ");\r\n";
							break;
						case 2:
							if (parts.length != 9)
							{
								System.out.println("Error in file " + i + ": " + lines[j].trim());
								continue;
							}
							query += "station(STATION_ID, STATION_NM, " +
									//"CENTER_ID, CENTER_YN, " +
									"LNG, LAT, REGION_NAME, STATION_NO" +
									//", DISTRICT_CD" +
									") \r\n";
							query += "values (" +
									"'" + parts[0] + "', '" + parts[1] + "', '" +
									parts[4] + "', '" + parts[5] + "', '" + parts[6] + "', '" + parts[7] + "'";
							
							
							/*
							for (int k = 0; k < parts.length; k++) {
								if (k > 0)
									query += ", ";
								query += "'" + parts[k].trim() + "'";
							}*/
							query += ");\r\n";
							break;

						case 3:
							query += "routestation(ROUTE_ID, STATION_ID, UPDOWN, STA_ORDER, ROUTE_NM, STATION_NM) \r\n";
							query += "values (";
							if (parts.length != 6)
							{
								System.out.println("Error in file " + i + ": " + lines[j].trim());
								continue;
							}
							for (int k = 0; k < parts.length; k++) {
								if (k > 0)
									query += ", ";
								query += "'" + parts[k].trim() + "'";
							}
							query += ");\r\n";
							break;
						}
						dbStmt.executeUpdate(query);
						// System.out.println(query);
						
						f.delete();
					}
				} catch (SQLException e) {
					System.out.println("In Query: " + query);
					e.printStackTrace();
				}
				finally
				{
					dbStmt.executeUpdate("COMMIT;");
				}
				
				ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("gen\\timetable_bus.zip"));
				
				FileInputStream in = new FileInputStream("gen\\timetable_bus.db");
				
				byte[] byteBuf = new byte[bufsize];
				zos.putNextEntry(new ZipEntry("gen\\timetable_bus.db"));
				int n;
				while((n=in.read(byteBuf)) > 0)
				{
					zos.write(byteBuf,0,n);
				}
				in.close();
				zos.closeEntry();
				zos.close();

			}
		} catch (Exception e) {
			System.out.println("In Query: " + query);
			e.printStackTrace();
		}
	}
}
