package jp.dip.utakatanet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Base {
	/**************************************************
	 * 入出力関係
	 * @return 
	 **************************************************/
	
	public static void println(String str){
		System.out.println(str);
	}
	
	public static void print(String str){
		System.out.print(str);
	}
	
	public static PrintWriter getPrintWriter(String file) throws IOException{
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);
		return pw;
	}
	
	public static BufferedReader getFileBr(String file) throws FileNotFoundException{
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		return br;
	}
	
	public static BufferedReader getBufferedReader(){
		return new BufferedReader(new InputStreamReader(System.in));
	}
	
	public static String getStringFromFile(String file) throws IOException{
		BufferedReader br = getFileBr(file);
		String line, result = "";
		while((line = br.readLine()) != null)
			result += line + "\n";
		br.close();
		return result;
	}
	
	public static ArrayList<File> listFiles(String dirStr){
		File dir = new File(dirStr);
		ArrayList<File> files = new ArrayList<File>();
		for(File file : dir.listFiles()){
			if(file.getName().indexOf(".") == 0) continue;
			files.add(file);
		}
		return files;
	}
	
	public static ArrayList<File> listDirs(String dirStr){
		File dir = new File(dirStr);
		ArrayList<File> files = new ArrayList<File>();
		for(File file : dir.listFiles()){
			if(file.getName().indexOf(".") == 0) continue;
			if(file.isDirectory()) files.add(file);
		}
		return files;
	}
	

	/**************************************************
	 * Date
	 **************************************************/
	
	public static String getDateString(){
		return getDateString("yyyyMMddhhmmss");
	}
	
	public static String getDateString(String format){
	    Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String date = sdf.format(cal.getTime());
        return date;
	}
	
	/**************************************************
	 * HTTP
	 * @throws IOException 
	 **************************************************/
	public static String httpGet(String url_str) throws IOException{
		URL url = new URL(url_str);
		HttpURLConnection http_connection = (HttpURLConnection)url.openConnection();
		http_connection.connect();
		InputStreamReader isr = new InputStreamReader(http_connection.getInputStream(), "SHIFT_JIS");
		BufferedReader br = new BufferedReader(isr);

		String str = "";
		String line = "";
		while((line = br.readLine()) != null)
			str += line;
		str = str.replaceAll("\t", "");
		return str;
	}
}
