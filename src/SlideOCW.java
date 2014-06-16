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
		
		// スタートページを Queue に突っ込む
		String start_url = "http://" + domain + "/";
		urlQueue.add(start_url);
		
		// Queueが空になるまでCrawl
		while(urlQueue.size() != 0){
			crawl();
			try{
				Thread.sleep(1000);
			}catch(Exception e){ }
			System.out.println("Queue Size: " + urlQueue.size());
		}
	}
	
	public void crawl(){
		// キューからURLを取り出してキューの中身は削除
		String url_str = urlQueue.poll();
		// 履歴にURLを追加（一回のクローリングで一度行ったページは2度訪問しない）
		history.add(url_str);
		
		// pdf か ppt(x) かをチェックし、そうだった場合はローカルに保存
		Pattern pattern = Pattern.compile("[^/]+?\\.(pdf|pptx?)$");
		Matcher matcher = pattern.matcher(url_str);
		if(matcher.find()){
			if(DEBUG) System.out.println("pdf/ppt(x) file");
			if(DEBUG) System.out.println("Binary file name:" + matcher.group());
			getBinary(url_str, matcher.group());
			return;
		}
		
		// 普通のWebページの場合
		String response_strs[] = get(url_str);
		if(response_strs == null) return; // エラーの場合はnullが帰ってくる

		// htmlだった場合、リンクを見つけてキューにpush
		if(response_strs[1].indexOf("html") != -1){
			retrieveURLs(response_strs[0], url_str);
			
		// ppt,pdfの場合保存
		}else if(response_strs[1].indexOf("pdf") != -1 || response_strs[1].indexOf("ppt") != -1){
			pattern = Pattern.compile("[^/]+?\\.(pdf|pptx?)");
			matcher = pattern.matcher(url_str);
			if(matcher.find()){
				if(DEBUG) System.out.println("pdf/ppt(x) file");
				if(DEBUG) System.out.println("Binary file name:" + matcher.group());
				getBinary(url_str, matcher.group());
				return;
			}
		}
		System.out.println();
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
			// if(DEBUG) System.out.println(response_strs[0]);
			return response_strs;
			
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}
	
	private void retrieveURLs(String str, String current_url_str){
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
				// TODO: 
				if(!history.contains(url_str) && !urlQueue.contains(url_str)){
					if(DEBUG) System.out.println("Add to urlQueue: " + url_str);
					urlQueue.add(url_str);
				}
			// httpから始まらない場合
			}else if(url_str.charAt(0) == '/'){
				// 絶対パスの場合
				url_str = "http://" + domain + url_str;
				if(!history.contains(url_str) && !urlQueue.contains(url_str)){
					if(DEBUG) System.out.println("Find Absolute Path URL");
					if(DEBUG) System.out.println("Add to urlQueue: " + url_str);
					urlQueue.add(url_str);
				}
			}else{
				// 相対パスであろう場合
				if(DEBUG) System.out.println("Find Relatevily Path URL");
				Pattern currentpath_pattern = Pattern.compile("(.+/)([^/]+?$)");
				Matcher currentpath_matcher = currentpath_pattern.matcher(current_url_str);
				if(currentpath_matcher.find()){
					url_str = currentpath_matcher.group(1) + url_str;
					if(DEBUG) System.out.println("Add to urlQueue: " + url_str);
					urlQueue.add(url_str);
				}
			}
		}
	}

	private void getBinary(String url_str, String name){
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
			
			System.out.println("Save at: " + domain + "/" + name);
		} catch (IOException e) {
			System.out.println("DL失敗: " + url_str);
		}
	}
}