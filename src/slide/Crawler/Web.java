package slide.Crawler;
import jp.dip.utakatanet.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Web{
	public static boolean DEBUG = false;
	public static String prev = "";
	
	public static String[] get(String url_str){
		try{
			String response_strs[] = new String[2];
			URL url = new URL(url_str);
			System.out.println("GET " + url);
			
			if(DEBUG){
				System.out.println("URL getFile: " + url.getFile());
				System.out.println("URL getPath: " + url.getPath());
			}
			prev = url_str;
			
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
				response_strs[0] += line; //  + "\n";
			}
			response_strs[0] = response_strs[0].replaceAll("\t", " ");
			if(DEBUG) System.out.println(response_strs[0]);

			return response_strs;
		} catch (Exception e) {
			// TODO: System.out.println(e);
			// e.printStackTrace();
			System.out.println("Not Found ? : " + e);
			return null;
		}
	}

	// URLから # 以下を除去し、相対パスはちゃんとしたURLに
	public static String urlFormatter(String domain, String url){
		Logger.sPrintln("URL整形: " + url);
		Pattern pattern = Pattern.compile("https?://" + domain + "/(.*)/");
		Matcher matcher = pattern.matcher(prev);
		String pwd = "";
		if(matcher.find()){
			pwd = matcher.group(1) + "/";
		}
		
		// #以降は除去する
		int sharp_index = url.indexOf("#");
		if(sharp_index >= 0)
			url = url.substring(0, sharp_index);
		
		Logger.sPrintln("#除去後: " + url);
		
		if(url.length() == 0) return null; // #から始まるURLだったら終了
		
		if(url.indexOf("http://") == 0){
			Logger.sPrintln("httpから始まる");
			// 同じドメインかどうか
			if(url.indexOf("http://" + domain) != 0) return null;
			// full URL in kyoto-u domain
			Logger.sPrintln("同じドメインだった");

			// httpから始まらない絶対パスの場合
		}else if(url.charAt(0) == '/'){
			url = "http://" + domain + url;

			// 相対パスの場合
		}else{
			// TODO: とりあえず domain にくっつける対処的療法
			url = "http://" + domain + "/" + pwd + url;
		}
		return url;
	}

	// HTMLからaタグを拾ってくる
	public static ArrayList<String> getLinksFromHtml(String domain, String html){
		ArrayList<String> url_list = new ArrayList<String>();
		
		// <a>タグのMatcher
		Pattern pattern = Pattern.compile("<a .*?href *?= *?\"([^\";]+?)\".*?>");
		Matcher matcher = pattern.matcher(html);
		
		// aタグが見つかった場合
		while(matcher.find()){
			String url = matcher.group(1);
			// System.out.println("Find URL: " + url);
			
			// 整形して
			url = urlFormatter(domain, url);
			if(url == null || url.length() == 0) continue;
			
			// listに追加
			if(!url_list.contains(url))
				url_list.add(url);
		}
		return url_list;
	}

	public static void getBinary(String url_str, String dir){
		try {
			// ユーザーエージェントの偽装
			// System.setProperty("http.agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X; ja-JP-mac; rv:1.8.1.6) Gecko/20070725 Firefox/2.0.0.6");
			
			// URL からファイル名を取得
			Pattern p = Pattern.compile("([^/]+)$");
			Matcher m = p.matcher(url_str);
			m.find();
			String name = m.group(1);
			
			URL url = new URL(url_str);
			URLConnection url_connection = url.openConnection();
			InputStream is = url.openStream();
			
			File file = new File(dir);
			file.mkdirs();
			
			FileOutputStream fos = new FileOutputStream(dir + "/" + name);
			byte b[] = new byte[1024];
			int blength;
			while((blength = is.read(b)) != -1){
				fos.write(b, 0, blength);
			}
			fos.flush();
			fos.close();
			
			System.out.println("Save at: " + dir + "/" + name);
		} catch (Exception e) {
			System.out.println("DL失敗: " + url_str);
		}
	}
}