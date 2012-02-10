import java.io.File;

public class Main {

	public static void main(String[] args)
	{
		String[] fileName = {"gen\\0.txt", "gen\\1.txt", "gen\\2.txt", "gen\\3.txt" };
		
		File dir = new File("gen\\");
		if (!dir.exists())
		{
			dir.mkdirs();
		}
		DBDownloader dbD = new DBDownloader();
		DBCreator dbC = new DBCreator();
		
		int version = dbD.checkVersion();
		
		System.out.println("Current Version: " + version);

		dbC.dbDownloader(fileName);
		
		System.out.println("Complete.");
	}
}
