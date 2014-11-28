package slide.Crawler;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class OCW {
	String domain;
	String courselist;
	public Queue<String> urlQueue;
	public Queue<String> pdfQueue;
	public Queue<Lecture> lectureQueue;
	public Queue<String> lecturenoteQueue;
	public int lecturenum = 0;
	public int depth = 2;
	private static final boolean DEBUG = true;
	
	OCW(){}
	
	OCW(String d){
		domain = d;
	}
	
	OCW(String d, String c){
		domain = d;
		courselist = c;
	}
	
	public ArrayList<String> retrievePDF(String str){
		System.out.println("Retrieving PDF URLs");
		Pattern pattern = Pattern.compile("<a .*?href *?= *?\"([^\"]+?\\.pdf)\".*?>");
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
				url_str = "http://" + domain + "/" + url_str;
			}
			
			if(url_str.length() != 0 && !result.contains(url_str))
				result.add(url_str);
		}
		return result;
	}
	
	public String[] get(String url_str){
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
			// TODO: System.out.println(e);
			e.printStackTrace();
			return null;
		}
	}
	
	public void getCourseList(String courselist_url_str){
		urlQueue = new ArrayDeque<String>();
		try{
			// GET
			String http_result = get("http://" + domain + "/" + courselist_url_str)[0];
			System.out.println(http_result);
			
			// <a>タグのMatcher
			Pattern pattern = Pattern.compile("<a .*?href *?= *?\"([^\"]+?)\".*?>");
			Matcher matcher = pattern.matcher(http_result);
			
			// aタグが見つかった場合
			while(matcher.find()){
				String url_str = matcher.group(1);
				if(DEBUG) System.out.println("Find URL: " + url_str);
				
				// #以降は除去する
				int sharp_index = url_str.indexOf("#");
				if(sharp_index >= 0)
					url_str = url_str.substring(0, sharp_index);
				
				if(url_str.length() == 0) continue; // #から始まるURLだったら終了
				
				// httpから始まる場合
				if(url_str.indexOf("http://") == 0){
					// 同じドメインかどうか
					if(url_str.indexOf("http://" + domain) != 0) continue;
					// full URL in kyoto-u domain
					if(DEBUG) System.out.println("Find Full Path URL");
					
				// httpから始まらない絶対パスの場合
				}else if(url_str.charAt(0) == '/'){
					url_str = "http://" + domain + url_str;
					
				// 相対パスの場合
				}else{
					// TODO: とりあえず domain にくっつける対処的療法
					url_str = "http://" + domain + "/" + url_str;
				}
				
				if(url_str.length() != 0 && !urlQueue.contains(url_str))
					urlQueue.add(url_str);
			}
		}catch(Exception e){
			// TODO: System.err.println(e);
			e.printStackTrace();
		}
	}
	
	public void getLecture(){
		// urlQueue から lectureQueue を作成
		int lecture_num = 0;
		while(urlQueue.size() > 0){
			String url = urlQueue.poll();
			lecture_num++;

			System.out.println(url);
			String http_result = get(url)[0];
			System.out.println(http_result);
			
			// 講義名を取得
			String lecture_name = "lecture" + lecture_num;
			for(int i = 1; i <= 3; i++){
				Pattern pattern = Pattern.compile("<[hH]" + i + ">(.+?)</[hH]" + i + ">");
				Matcher matcher = pattern.matcher(http_result);
				if(matcher.find()){
					lecture_name = matcher.group(1);
					System.out.println(lecture_name);
					break;
				}
			}
			
			Lecture lecture = new Lecture(lecture_name);
			lecture.ocwDomain = domain;
			lecture.html = http_result;
			lecture.crawl();
		}
	}
	
	public void getLectures(){
		urlQueue = new ArrayDeque<String>();
		pdfQueue = new ArrayDeque<String>();
		lectureQueue = new ArrayDeque<Lecture>();
		while(urlQueue.size() > 0){
			System.out.println("\nSize of urlQueue: " + urlQueue.size());
			
			// まずは講義ページのhtmlを取得
			String url_str = urlQueue.poll();
			System.out.println(url_str);
			String http_results[] = get(url_str);
			if(http_results == null) continue;
			http_results[0] = http_results[0].replaceAll("\n", "");
			http_results[0] = http_results[0].replaceAll("\t", "");
			
			// 講義名を取得
			Pattern pattern = Pattern.compile("<h2.*?>(.+)</h2>");
			Matcher matcher = pattern.matcher(http_results[0]);
			lecturenum++;
			String lecture_name = "lecture" + lecturenum;
			if(matcher.find()){
				lecture_name = matcher.group(1);
				lecture_name = lecture_name.replaceAll("[ 　\t]", "");
			}
			
			// 講義のノートのページのURL取得 FIXME: 必ずしもlecturenoteという名前じゃないようだ
			// http_results = get(url_str + "/lecturenote");
			// if(http_results == null) continue;
			
			ArrayList<String> pdf_urls = retrievePDF(http_results[0]);
			
			Lecture lecture = new Lecture(lecture_name);
			lecture.PDFURLs = pdf_urls;
			lecture.ocwDomain = domain;
			lectureQueue.add(lecture);
			try{
				Thread.sleep(lecture.INTERVAL);
			}catch(Exception e){ }
		}
	}
	
	public void getPDFs(){
		while(lectureQueue.size() > 0){
			Lecture lecture = lectureQueue.poll();
			lecture.getPDFs();
		}
	}
}

class OcwUrl{
	String url;
	int depth;
}