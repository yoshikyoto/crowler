import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TokyoOCW extends OCW {
	public final boolean DEBUG = true;
	public int count = 1; // FIXME: debug code
	TokyoOCW(){
		domain = "ocw.u-tokyo.ac.jp";
	}
	
	public void getCourseLists(){
		// ディレクトリを作成して
		File domaindir = new File(domain);
		domaindir.mkdir();
		// キューを初期化して
		urlQueue = new ArrayDeque<String>();
		for(int p = 1; p <= 13; p++)
			getCourseList("http://ocw.u-tokyo.ac.jp/result?page=" + p);
	}
	
	@Override
	public void getCourseList(String courselist_url_str){
		try{
			// GET
			String line = new String();
			String http_result = get(courselist_url_str)[0];
			http_result = http_result.replaceAll("\n", "");
			if(DEBUG){
				try{
					FileWriter fw = new FileWriter("DEUBUG_" + domain + "_" + count + ".txt");
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter pw = new PrintWriter(bw);
					pw.println(http_result);
					pw.close();
					count++;
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			// Retrieve URLs
			Pattern pattern = Pattern.compile("<a .*?href *?= *?\"(.+?)\".*?>");
			Matcher matcher = pattern.matcher(http_result);
			while(matcher.find()){
				String url_str = matcher.group(1);
				// if(DEBUG) System.out.println("Find URL: " + url_str);
				
				// remove after #
				int sharp_index = url_str.indexOf("#");
				if(sharp_index >= 0)
					url_str = url_str.substring(0, sharp_index);
				
				if(url_str.length() == 0) continue;
				
				// if(DEBUG) System.out.println(url_str);
				// lectureから始まる場合は追加
				if(url_str.indexOf("lecture") == 0){
					if(DEBUG) System.out.println("Find Path contain 'lecture':\t" + url_str);
					url_str = "http://ocw.u-tokyo.ac.jp/" + url_str;
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
}