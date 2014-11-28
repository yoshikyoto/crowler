package slide.Crawler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Lecture{
	public String name;
	public String ocwDomain;
	public ArrayList<String> PDFURLs;
	public Queue<String> urlQueue;
	public String html;
	public final int INTERVAL = 100; // ms
	File dir;
	
	// TODO: ここでスライドを取得
	Lecture(String n, String d){
		name = n;
		dir = new File(d);
	}
	
	Lecture(String n){
		name = n;
		PDFURLs = new ArrayList<String>();
	}
	
	public void crawl(){
		urlQueue = new ArrayDeque<String>();
		
		// <a>タグのMatcher
		Pattern pattern = Pattern.compile("<a .*?href *?= *?\"([^\"]+?)\".*?>");
		Matcher matcher = pattern.matcher(html);
		
		// aタグが見つかった場合
		while(matcher.find()){
			String url_str = matcher.group(1);
			System.out.println("Find URL: " + url_str);
			
			// #以降は除去する
			int sharp_index = url_str.indexOf("#");
			if(sharp_index >= 0)
				url_str = url_str.substring(0, sharp_index);
			
			if(url_str.length() == 0) continue; // #から始まるURLだったら終了
			
			// httpから始まる場合
			if(url_str.indexOf("http://") == 0){
				// 同じドメインかどうか
				if(url_str.indexOf("http://" + ocwDomain) != 0) continue;
				// full URL in kyoto-u domain
				
			// httpから始まらない絶対パスの場合
			}else if(url_str.charAt(0) == '/'){
				url_str = "http://" + ocwDomain + url_str;
				
			// 相対パスの場合
			}else{
				// TODO: とりあえず domain にくっつける対処的療法
				url_str = "http://" + ocwDomain + "/" + url_str;
			}
			
			if(url_str.indexOf(".pdf") != -1){
				PDFURLs.add(url_str);
				continue;
			}
			
			if(url_str.length() != 0 && !urlQueue.contains(url_str))
				urlQueue.add(url_str);
		}
	}
	
	public void crawl2(){
		ArrayList<String> result = new ArrayList<String>();
		
		while(urlQueue.size() >= 0){
			String url = urlQueue.poll();
			// GET
			String http_result = Web.get(url)[0];
			System.out.println(http_result);
			
			Pattern pattern = Pattern.compile("<a .*?href *?= *?\"([^\"]+?\\.pdf)\".*?>");
			Matcher matcher = pattern.matcher(http_result);
			while(matcher.find()){
				String url_str = matcher.group(1);
				System.out.println("Find URL: " + url_str);
				
				// remove after #
				int sharp_index = url_str.indexOf("#");
				if(sharp_index >= 0)
					url_str = url_str.substring(0, sharp_index);
				
				if(url_str.length() == 0) continue;
				
				// httpから始まる場合
				if(url_str.indexOf("http://") == 0){
					// 同じドメインかどうか
					if(url_str.indexOf("http://" + ocwDomain) != 0) continue;
					// full URL in kyoto-u domain
					
				// httpから始まらない場合
				}else if(url_str.charAt(0) == '/'){
					// 絶対パスの場合
					url_str = "http://" + ocwDomain + url_str;
				}else{
					url_str = "http://" + ocwDomain + "/" + url_str;
				}
				
				if(url_str.length() != 0 && !result.contains(url_str))
					result.add(url_str);
			}
		}
		PDFURLs = result;
		getPDFs();
	}
	
	
	
	public void getPDFs(){
		if(PDFURLs.size() == 0) return;
		
		File domaindir = new File("data/" + ocwDomain);
		domaindir.mkdir();
		
		File lecturedir = new File("data/" + ocwDomain + "/" + name);
		lecturedir.mkdir();
		
		for(int i = 0; i < PDFURLs.size(); i++){
			String url_str = PDFURLs.get(i);
			
			// PDFの名前を取得して
			Pattern pattern = Pattern.compile("[^/&]+?\\.(pdf|pptx?)");
			Matcher matcher = pattern.matcher(url_str);
			String pdf_name = "";
			if(matcher.find()){
				pdf_name = matcher.group();
			}else{
				pdf_name = i + ".pdf";
			}
			
			// PDFを保存
			getBinary(url_str, pdf_name, lecturedir.toString());
			
			// スリープ
			try{
				Thread.sleep(INTERVAL);
			}catch(Exception e){ }
		}
	}
	
	private void getBinary(String url_str, String name, String dir){
		try {
			// ユーザーエージェントの偽装
			// System.setProperty("http.agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X; ja-JP-mac; rv:1.8.1.6) Gecko/20070725 Firefox/2.0.0.6");
			URL url = new URL(url_str);
			URLConnection url_connection = url.openConnection();
			InputStream is = url.openStream();
			
			FileOutputStream fos = new FileOutputStream(dir + "/" + name);
			byte b[] = new byte[1024];
			int blength;
			while((blength = is.read(b)) != -1){
				fos.write(b, 0, blength);
			}
			fos.flush();
			fos.close();
			
			System.out.println("Save at: " + dir + "/" + name);
		} catch (IOException e) {
			System.out.println("DL失敗: " + url_str);
		}
	}
}