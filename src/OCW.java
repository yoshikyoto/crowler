import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class OCW {
	String domain;
	public Queue<String> urlQueue;
	public Queue<String> lecturenoteQueue;
	public Queue<String> pdfQueue;
	public Queue<Lecture> lectureQueue;
	public int lecturenum = 0;
	private static final boolean DEBUG = false;
	
	public ArrayList<String> retrievePDF(String str){
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
			System.out.println(e);
			return null;
		}
	}
	
	public void getCourseList(String courselist_url_str){
		try{
			// GET
			String line = new String();
			String http_result = get(courselist_url_str)[0];
			
			// Retrieve URLs
			Pattern pattern = Pattern.compile("<a .*?href *?= *?\"([^\"]+?)\".*?>");
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
	
	public void getPDFs(){
		while(lectureQueue.size() > 0){
			Lecture lecture = lectureQueue.poll();
			lecture.getPDFs();
		}
	}
}
