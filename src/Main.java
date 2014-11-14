import java.io.*;
import java.util.*;

import jp.dip.utakatanet.*;
import slide.*;
import slide.XMLConverter.*;
import slide.analyzer.*;
import slide.lectureSim.*;

class Main extends Base{
	
	/*
	 * OCW一覧: http://edu.k-forte.net/2013/06/ocw-japan-university/
	 * ocw.kyoto-u.ac.jp
	 * ocw.u-tokyo.ac.jp
	 * ocw.nagoya-u.jp
	 */
	public static String root = "/Users/admin/ocwslidedata";
	public static void main(String args[]){
		String domains[] = {
			"ocw.kyoto-u.ac.jp",
			"ocw.u-tokyo.ac.jp",
			"ocw.nagoya-u.jp"
		};
		
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Input number");
		switch(sc.nextInt()){
		case 0: // Crawling?
			break;
		case 1: // slide/pdf to XML
			slideToXML();
			break;
		case 2: // slide/pdf to Image
			slideAnalyze();
			break;
		case 3: // calc Lecture Similarity
			LectureSim.calc();
			break;
		}
		
		// SlideData d = new SlideData("/Users/admin/ocwslidedata");
	}
	
	public static void slideToXML(){
		Logger.setLogName("ConvertXML");
		
		for(File ocwfile : listDirs(root)){
			// 各 OCW のディレクトリについて
			Logger.sPrintln("OCW: " + ocwfile.getName());
			
			for(File lecfile : listDirs(ocwfile.getAbsolutePath())){
				// 各講義のディレクトリについて
				Logger.sPrintln("Lecture: " + lecfile.getName());
				
				for(File sfile : listDirs(lecfile.getAbsolutePath())){
					// 各スライドのファイルに対して
					Logger.sPrintln("Slide: " + sfile.getName());
					
					SlideXMLConverter.convert(sfile.getAbsolutePath());
				}
			}
		}
		Logger.sClose();
	}
	
	public static void slideAnalyze(){
		Logger.setLogName("ConvertXML");
		
		for(File ocwfile : listDirs(root)){
			// 各 OCW のディレクトリについて
			Logger.sPrintln("OCW: " + ocwfile.getName());
			
			for(File lecfile : listDirs(ocwfile.getAbsolutePath())){
				// 各講義のディレクトリについて
				Logger.sPrintln("Lecture: " + lecfile.getName());
				
				for(File sfile : listDirs(lecfile.getAbsolutePath())){
					// 各スライドのファイルに対して
					Logger.sPrintln("Slide: " + sfile.getName());
					// xmlのパスを取得
					String xml_path = sfile.getAbsolutePath() + "/slide.xml";
					System.out.println(xml_path);
					
					// ファイルが存在しているか確認してAnalyze
					File xml_file = new File(xml_path);
					if(xml_file.exists()){
						SlideAnalyzer.analyze(sfile.getAbsolutePath());
					}else{
						Logger.sErrorln("ファイルが見つかりませんでした: " + xml_path);
					}
					System.gc();
				}
			}
		}
		Logger.sClose();
	}

}