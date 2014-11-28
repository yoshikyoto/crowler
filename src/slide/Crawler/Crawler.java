package slide.Crawler;

import java.util.HashMap;

import slide.Main.*;
import jp.dip.utakatanet.*;

public class Crawler extends SlideMain{
	public static HashMap<String, String> clMap = new HashMap<String, String>();
	
	public static void start(){
		clMap.put("ocw.kyushu-u.ac.jp", "courselist.html");
		clMap.put("ocw.nagoya-u.jp", "courselist.php?lang=ja&mode=l&page_type=all");
		clMap.put("ocw.hokudai.ac.jp", "Course/Faculty/index.php?lang=ja");
		clMap.put("ocw.osaka-u.ac.jp", "courselist-jp");
		
		System.setProperty("proxySet", "true");
		System.setProperty("proxyHost", "proxy.kuins.net");
		System.setProperty("proxyPort", "8080");
		
		Logger.setLogName("Crawler");
		for(String ocwDomain : clMap.keySet()){
			String clname = clMap.get(ocwDomain);
			try{
				OCWCrawler clawler = new OCWCrawler(ocwDomain, clname);
				clawler.crawl();
			}catch(Exception e){
				Logger.sErrorln(e.toString());
				e.printStackTrace();
			}
		}
		Logger.sClose();
	}
}