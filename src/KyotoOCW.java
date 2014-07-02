import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class KyotoOCW{
	String domain = "ocw.kyoto-u.ac.jp";
	public Queue<String> urlQueue;
	public Queue<String> lecturenoteQueue;
	public Queue<String> pdfQueue;
	public Queue<Lecture> lectureQueue;
	private static final boolean DEBUG = false;
	public int lecturenum = 0;
	
	public void getCourseLists(){
		// ディレクトリを作成して
		File domaindir = new File(domain);
		domaindir.mkdir();
		// キューを初期化して
		urlQueue = new ArrayDeque<String>();
		getCourseList("http://ocw.kyoto-u.ac.jp/courselist");
		getCourseList("http://ocw.kyoto-u.ac.jp/courselist?b_start:int=20");
		getCourseList("http://ocw.kyoto-u.ac.jp/courselist?b_start:int=40");
	}
	
	public void getCourseList(String courselist_url_str){
		// String courselist_url_str = "http://ocw.kyoto-u.ac.jp/courselist";
		// ディレクトリは作っておく
		// String http_result[] = get(courselist_url);
		// retrieveURLs(http_result[0], courselist_url);
		
		try{
			// GET
			String line = new String();
			String http_result = get(courselist_url_str)[0];
			
			// Retrieve URLs
			Pattern pattern = Pattern.compile("<a .*?href *?= *?\"(.+?)\".*?>");
			Matcher matcher = pattern.matcher(http_result);
			while(matcher.find()){
				String url_str = matcher.group(1);
				if(DEBUG) System.out.println("Find URL: " + url_str);
				
				// remove after #
				int sharp_index = url_str.indexOf("#");
				if(sharp_index >= 0)
					url_str = url_str.substring(0, sharp_index);
				
				if(url_str.length() == 0) continue;
				
				// httpから始まる場合
				if(url_str.indexOf("http://") == 0){
					// 同じドメインかどうか
					if(url_str.indexOf("http://" + domain) != 0) continue;
					// full URL in kyoto-u domain
					if(DEBUG) System.out.println("Find Full Path URL");
				// httpから始まらない場合
				}else if(url_str.charAt(0) == '/'){
					// 絶対パスの場合
					url_str = "http://" + domain + url_str;
				}else{
					url_str = "";
				}
				
				if(url_str.length() != 0 && !urlQueue.contains(url_str))
					urlQueue.add(url_str);
			}
		}catch(Exception e){
			System.err.println(e);
		}
	}
	
	public void getLectures(){
		lecturenoteQueue = new ArrayDeque<String>();
		pdfQueue = new ArrayDeque<String>();
		lectureQueue = new ArrayDeque<Lecture>();
		while(urlQueue.size() > 0){
			System.out.println("\nSize of urlQueue: " + urlQueue.size());
			System.out.println("Size of pdfQueue: " + pdfQueue.size());
			
			// まずは講義ページのhtmlを取得
			String url_str = urlQueue.poll();
			System.out.println(url_str);
			String http_results[] = get(url_str);
			if(http_results == null) continue;
			
			// 講義名を取得
			Pattern pattern = Pattern.compile("<h1[^<>]*?>([^<>]+?)</h1>");
			Matcher matcher = pattern.matcher(http_results[0]);
			lecturenum++;
			String lecture_name = "lecture" + lecturenum;
			if(matcher.find())
				lecture_name = matcher.group(1);
			
			System.out.println("講義名: " + lecture_name);
			
			// 講義のノートのページのURL取得 FIXME: 必ずしもlecturenoteという名前じゃない
			http_results = get(url_str + "/lecturenote");
			if(http_results == null) continue;
			
			ArrayList<String> pdf_urls = retrievePDF(http_results[0]);
			
			Lecture lecture = new Lecture(lecture_name);
			lecture.PDFURLs = pdf_urls;
			lecture.ocwDomain = domain;
			lectureQueue.add(lecture);
			try{
				Thread.sleep(100);
			}catch(Exception e){ }
		}
	}
	
	ArrayList<String> retrievePDF(String str){
		Pattern pattern = Pattern.compile("<a .*?href *?= *?\"(.+?\\.pdf)\".*?>");
		Matcher matcher = pattern.matcher(str);
		ArrayList<String> result = new ArrayList<String>();
		while(matcher.find()){
			String url_str = matcher.group(1);
			if(DEBUG) System.out.println("Find URL: " + url_str);
			
			// remove after #
			int sharp_index = url_str.indexOf("#");
			if(sharp_index >= 0)
				url_str = url_str.substring(0, sharp_index);
			
			if(url_str.length() == 0) continue;
			
			// httpから始まる場合
			if(url_str.indexOf("http://") == 0){
				// 同じドメインかどうか
				if(url_str.indexOf("http://" + domain) != 0) continue;
				// full URL in kyoto-u domain
				if(DEBUG) System.out.println("Find Full Path URL");
			// httpから始まらない場合
			}else if(url_str.charAt(0) == '/'){
				// 絶対パスの場合
				url_str = "http://" + domain + url_str;
			}else{
				url_str = "";
			}
			
			if(url_str.length() != 0 && !result.contains(url_str))
				result.add(url_str);
		}
		return result;
	}
	
	public void getPDFs(){
		while(lectureQueue.size() > 0){
			Lecture lecture = lectureQueue.poll();
			lecture.getPDFs();
		}
	}
	
	private String[] get(String url_str){
		try{
			String response_strs[] = new String[2];
			URL url = new URL(url_str);
			System.out.println("GET " + url);
			
			if(DEBUG){
				System.out.println("URL getFile: " + url.getFile());
				System.out.println("URL getPath: " + url.getPath());
			}
			
			HttpURLConnection http_connection = (HttpURLConnection)url.openConnection();
			http_connection.connect();
			InputStreamReader isr = new InputStreamReader(http_connection.getInputStream(), "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			
		    response_strs[1] = http_connection.getContentType();
		    System.out.println("Content-Type: " + response_strs[1]);
		    if(response_strs[1].indexOf("html") == -1){
		    	response_strs[0] = "";
		    	return response_strs;
		    }

			String line = new String();
			while((line = br.readLine()) != null){
				response_strs[0] += line + "\n";
			}
			if(DEBUG) System.out.println(response_strs[0]);
			return response_strs;
			
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}
	
	private void retrieveURLs(String str, String current_url_str){
		System.out.println("Retrieve URLs");
		Pattern pattern = Pattern.compile("<a .*?href *?= *?\"(.+?)\".*?>");
		Matcher matcher = pattern.matcher(str);
		
		while(matcher.find()){
			String url_str = matcher.group(1);
			if(DEBUG) System.out.println("Find URL: " + url_str);
			
			// remove after #
			int sharp_index = url_str.indexOf("#");
			if(sharp_index >= 0)
				url_str = url_str.substring(0, sharp_index);
			
			if(url_str.length() == 0) continue;
			
			// httpから始まる場合
			if(url_str.indexOf("http://") == 0){
				// 同じドメインかどうか
				if(url_str.indexOf("http://" + domain) != 0) continue;
				// full URL in kyoto-u domain
				if(DEBUG) System.out.println("Find Full Path URL");
				
			// httpから始まらない場合
			}else if(url_str.charAt(0) == '/'){
				// 絶対パスの場合
				url_str = "http://" + domain + url_str;

			}else{
				// 相対パスであろう場合
				/*
				if(DEBUG) System.out.println("Find Relatevily Path URL");
				Pattern currentpath_pattern = Pattern.compile("(.+/)([^/]+?$)");
				Matcher currentpath_matcher = currentpath_pattern.matcher(current_url_str);
				if(currentpath_matcher.find()){
					url_str = currentpath_matcher.group(1) + url_str;
					if(DEBUG) System.out.println("Add to urlQueue: " + url_str);
					// urlQueue.add(url_str);
				}
				*/
			}
			
			if(!urlQueue.contains(url_str)){
				if(DEBUG) System.out.println("Find Absolute Path URL");
				if(DEBUG) System.out.println("Add to urlQueue: " + url_str);
				urlQueue.add(url_str);
			}
		}
	}

}