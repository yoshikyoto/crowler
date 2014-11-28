package slide.Crawler;
import jp.dip.utakatanet.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LectureCrawler{
	String domain;
	String name;
	String rurl;
	
	ArrayList<String> pdfList;
	Queue<String> urlQueue;
	
	LectureCrawler(String d, String n, String u){
		domain = d;
		name = n;
		rurl = u;
		pdfList = new ArrayList<String>();
		urlQueue = new ArrayDeque<String>();
	}
	
	public void crawl(){
		Logger.sPrintln("Lectureのクローリング: " + name);
		Logger.sPrintln(rurl);
		
		// Step1
		String responses[] = Web.get(rurl);
		if(responses == null) return;
		ArrayList<String> urls = Web.getLinksFromHtml(domain, responses[0]);
		
		Logger.sPrintln("■ステップ1");
		for(String url : urls){
			Logger.sPrintln("リクエスト: " + url);
			url = Web.urlFormatter(domain, url);
			if(url == null) continue;
			
			if(url.indexOf(".pdf") == url.length() - 4){
				pdfList.add(url);
			}else if(!urlQueue.contains(url)){
				urlQueue.add(url);
			}
		}
		
		// Step2
		Logger.sPrintln("■ステップ2");
		while(urlQueue.size() > 0){
			String ourl = urlQueue.poll();
			Logger.sPrintln("リクエスト: " + ourl);
			responses = Web.get(ourl);
			if(responses == null) continue;
			urls = Web.getLinksFromHtml(domain, responses[0]);
			
			for(String url : urls){
				url = Web.urlFormatter(domain, url);
				if(url == null) continue;
				
				Pattern p = Pattern.compile("\\.[pP][dD][fF]$");
				Matcher m = p.matcher(url);
				if(m.find()) pdfList.add(url);
			}
		}
		
		for(String pdf : pdfList){
			System.out.println(pdf);
			Web.getBinary(pdf, "/home/sakamoto/OCWData/" + domain + "/" + name);
		}
	}
}