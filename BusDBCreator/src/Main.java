
public class Main {

	public static void main(String[] args)
	{
		String[] fileName = {"0.txt", "1.txt", "2.txt", "3.txt" };
		
		DBCreator dbC = new DBCreator();
		
		dbC.dbDownloader(fileName);
	}
}
