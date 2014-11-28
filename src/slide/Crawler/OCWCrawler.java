package slide.Crawler;
import jp.dip.utakatanet.Logger;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class OCWCrawler{
	public String domain;
	public String courselistUrl;
	ArrayList<LectureCrawler> lclist;
	
	OCWCrawler(String d, String c){
		domain = d;
		courselistUrl = c;
		lclist = new ArrayList<LectureCrawler>();
	}
	
	public void crawl(){
		getCourses();
		Logger.sPrintln("各講義について見ていく");
		for(LectureCrawler lc : lclist){
			lc.crawl();
		}
	}
	
	public void getCourses(){
		Logger.sPrintln("get Courses コースリストからURLを抜いてきます");
		String ourl = "http://" + domain + "/" + courselistUrl;
		String responses[] = Web.get(ourl);
		String rbody = responses[0];
		
		// <a>タグのMatcher
		Pattern pattern = Pattern.compile("<a .*?href *?= *?\"([^\"]+?)\".*?>(.+?)</a>");
		Matcher matcher = pattern.matcher(rbody);

		// aタグが見つかった場合
		while(matcher.find()){
			String url = matcher.group(1);
			String name = matcher.group(2);

			// URL整形
			url = Web.urlFormatter(domain, url);
			if(url == null || url.length() == 0) continue;

			// System.out.println("Find URL: " + url);
			
			// lectureの重複は無いと仮定
			LectureCrawler lc = new LectureCrawler(domain, name, url);
			lclist.add(lc);
		}
		
		Logger.sPrintln("取得されたURL");
		for(LectureCrawler lc : lclist){
			Logger.sPrintln(lc.name + " " + lc.rurl);
		}
	}
}