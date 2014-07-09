import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TokyoTechOCW extends OCW{
	public final boolean DEBUG = true;
	public int count = 1; // FIXME: debug code
	TokyoTechOCW(){
		domain = "www.ocw.titech.ac.jp";
	}
	
	public void getCourseLists(){
		System.out.println("Get Course Lists...");
		// ディレクトリを作成して
		File domaindir = new File("data/" + domain);
		domaindir.mkdir();
		// キューを初期化して
		urlQueue = new ArrayDeque<String>();
		
		// 講義一覧のページをゲット
		getCourseList("http://www.ocw.titech.ac.jp/index.php?module=General&action=TopAccess");
	}
	
	
	@Override
	public void getCourseList(String courselist_url_str){
		System.out.println("Get Course List");
		try{
			// ふつうにGET
			String line = new String();
			String http_result = get(courselist_url_str)[0];
			http_result = http_result.replaceAll("\n", "");
			
			
			// 講義ページのURLを取ってくる まずは普通にaタグをとる。 
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
				
				// if(DEBUG) System.out.println(url_str);
				// 講義ページである条件。vid=03が含まれているかどうか
				if(url_str.indexOf("vid=03") >= 0){
					if(DEBUG) System.out.println("Find Lecture Path:\t" + url_str);
					
					// vid=03 を 05 に置き換えると講義スライドページ
					url_str = url_str.replaceAll("vid=03", "vid=05");
					// URLデコード
					url_str = url_str.replaceAll("&amp;", "&");
					if(DEBUG) System.out.println("Replace:\t" + url_str);
					
					// フルパスに変換
					url_str = "http://" + domain + url_str;
					if(DEBUG) System.out.println("Full Path:\t" + url_str);
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
			
			// まずは講義ページのhtmlを取得
			String url_str = urlQueue.poll();
			System.out.println(url_str);
			String http_results[] = get(url_str);
			if(http_results == null) continue;
			http_results[0] = http_results[0].replaceAll("\n", "");
			http_results[0] = http_results[0].replaceAll("\t", "");
			
			// 講義名を取得 <h1><a >講義名</a></h1>
			Pattern pattern = Pattern.compile("<h1>.*?<a ?.*?>(.+?)</a>.*?</h1>");
			Matcher matcher = pattern.matcher(http_results[0]);
			lecturenum++;
			String lecture_name = "lecture" + lecturenum;
			if(matcher.find()){
				lecture_name = matcher.group(1);
				lecture_name = lecture_name.replaceAll("[ 　\t]", "");
			}
			System.out.println("講義名:\t" + lecture_name);
			
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
	
	@Override
	public ArrayList<String> retrievePDF(String str){
		System.out.println("Retrieving PDF URLs");
		Pattern pattern = Pattern.compile("<a .*?href *?= *?\"([^\"]+?)\".*?>");
		Matcher matcher = pattern.matcher(str);
		ArrayList<String> result = new ArrayList<String>();
		while(matcher.find()){
			String url_str = matcher.group(1);
			url_str = url_str.replaceAll("&amp;", "&");
			if(url_str.indexOf(".pdf") == -1) continue;
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
}