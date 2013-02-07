import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;


public class LogFile {
	
	public static BufferedWriter out;

	public static void openFile(String logFilePath) {
		FileWriter file = null;
		try {
			file = new FileWriter(logFilePath, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		out = new BufferedWriter(file);
	}

	public static void write(String logMessage) {
		try {
			out.write(getHour() + " - " + logMessage + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void closeFile() {
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getHour() {
		String result = "";
		
		Calendar now = Calendar.getInstance();
		
		result += now.get(Calendar.DATE) + "/";
		result += (now.get(Calendar.MONTH) + 1) + "/";
		result +=  now.get(Calendar.YEAR) + " - ";
		
		result += now.get(Calendar.HOUR_OF_DAY) + ":";
		result += now.get(Calendar.MINUTE) + ":";
		result += now.get(Calendar.SECOND) + ":";
		result += now.get(Calendar.MILLISECOND);
		
		return result;
	}
	
}
