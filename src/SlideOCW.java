import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




class SlideOCW{
	public String domain;
	public HashSet<String> history;
	public static final boolean DEBUG = false;
	public Queue<String> urlQueue;
	
	SlideOCW(String d){
		domain = d;
		history = new HashSet<String>();
		urlQueue = new ArrayDeque<String>();
	}
	
	public void startCrawling(){
		File domaindir = new File(domain);
		domaindir.mkdir();
		crawl(domain, "");
	}
	
	public void crawl(String domain, String path){
		String url_str = "http://" + domain + "/" + path;
		crawl(url_str);
	}
	
	public String[] parseURL(String url_str){
		String result[] = new String[4];
		Pattern pattern = Pattern.compile("(https?://)([^/]+?)(/.*/?)([^/]*)$");
		Matcher matcher = pattern.matcher(url_str);
		
		if(matcher.find()){
			System.out.println(matcher.group(1));
			System.out.println(matcher.group(2));
			System.out.println(matcher.group(3));
			System.out.println(matcher.group(4));
		}
		return result;
	}
	
	public void crawl(String url_str){
		history.add(url_str);
		
		Pattern pattern = Pattern.compile("[^/]+?\\.pdf$");
		Matcher matcher = pattern.matcher(url_str);
		
		// pdfだった場合
		if(matcher.find()){
			getPDF(url_str, matcher.group(0));
			return;
		}

		String http_result = get(url_str);
		HashSet<String> urls = getURLs(http_result);
		for(String next_url : urls){
			// 次
			if(history.contains(next_url)) continue;
			// wait 1000 msec
			try{
				Thread.sleep(1000);
			}catch(InterruptedException e){ System.err.println(e); }
			// GET Again
			crawl(next_url);
		}
	}
	
	private HashSet<String> getURLs(String str){
		HashSet<String> urls = new HashSet<String>();
		Pattern pattern = Pattern.compile("<a .*?href *?= *?\"(.+?)\".*?>");
		Matcher matcher = pattern.matcher(str);
		
		while(matcher.find()){
			String url_str = matcher.group(1);
			// System.out.println(url_str);
			
			// remove after #
			int sharp_index = url_str.indexOf("#");
			if(sharp_index >= 0)
				url_str = url_str.substring(0, sharp_index);
			
			if(url_str.indexOf("https?://" + domain) == 0){
				// full URL in kyoto-u domain
				urls.add(url_str);
			}else if(url_str.charAt(0) == '/'){
				urls.add("http://" + domain + url_str);
			}else{
				
			}
		}
		return urls;
	}
	
	private void getPDF(String url_str, String name){
		try {
			URL url = new URL(url_str);
			URLConnection url_connection = url.openConnection();
			InputStream is = url.openStream();
			
			FileOutputStream fos = new FileOutputStream(domain + "/" + name);
			byte b[] = new byte[1024];
			int blength;
			while((blength = is.read(b)) != -1){
				fos.write(b, 0, blength);
			}
			fos.flush();
			fos.close();
		} catch (IOException e) {
			System.out.println("DL失敗: " + url_str);
		}
	}
	
	private String get(String url_str){
		try{
			URL url = new URL(url_str);
			System.out.println("GET " + url);
			
			HttpURLConnection http_connection = (HttpURLConnection)url.openConnection();
			http_connection.connect();
			InputStreamReader isr = new InputStreamReader(http_connection.getInputStream(), "UTF-8");
			BufferedReader br = new BufferedReader(isr);

			String line = new String();
			String http_result = new String();
			while((line = br.readLine()) != null){
				http_result += line + "\n";
			}
			if(DEBUG) System.out.println(http_result);
			return http_result;
			
		} catch (Exception e) {
			System.err.println(e);
			return "";
		}
	}
	
	private void printArray(ArrayList<String> array){
		for(String str : array){
			System.out.println(str);
		}
	}
}