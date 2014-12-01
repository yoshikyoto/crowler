package slide.Main;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

import jp.dip.utakatanet.*;
import slide.*;
import slide.Crawler.*;
import slide.XMLConverter.*;
import slide.analyzer.*;
import slide.lectureSim.*;
import slide.mapping.Mapping;
import slide.SegmentSim.*;
import slide.html.*;
import slide.database.*;
import java.util.regex.*;

public class SlideMain extends Base{
	/*
	 * OCW一覧: http://edu.k-forte.net/2013/06/ocw-japan-university/
	 * ocw.kyoto-u.ac.jp
	 * ocw.u-tokyo.ac.jp
	 * ocw.nagoya-u.jp
	 */
	
	public static String root = "/home/sakamoto/OCWData";
	public static void main(String args[]) throws Exception{
		String domains[] = {
			"ocw.kyoto-u.ac.jp",
			"ocw.u-tokyo.ac.jp",
			"ocw.nagoya-u.jp"
		};
		
		LectureModel sm = new LectureModel();
		
		Scanner sc = new Scanner(System.in);
		
		p("Input Number");
		p("0. Crawling (under construction)");
		p("1. slide/pdf to XML");
		p("2. slide/pdf to Image (under construction)");
		p("3. calc Lecture Similarity");
		p("4. calc Segment similarity");
		p("5. Segment Mapping");
		p("6. Make HTML");
		
		switch(sc.nextInt()){
		case 0: // Crawling?
			Crawler.start();
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
		case 4: // calc Segment Sim
			SegSimCalculator.calc();
			break;
		case 5: // calc Segment Mapping
			Mapping.calc();
			break;
		case 6: // Make HTML
			SlideHtmlMaker.make();
			break;
		}
		sc.close();
		// SlideData d = new SlideData("/Users/admin/ocwslidedata");
	}

	public static void slideToXML(){
		
		Logger.setLogName("ConvertXML");
		// データベースモデルの初期化
		LectureModel lecture_model = new LectureModel();
		SlideModel slide_model = new SlideModel();
		
		for(File ocwfile : listDirs(root)){
			// CSSとかのアセットファイルは除く
			// TODO: このへんの例外的ディレクトリはどこかにまとめておく
			if(ocwfile.getName().equals("assets")) continue;
			if(ocwfile.getName().equals("bin")) continue;
			// 各 OCW のディレクトリについて
			Logger.sPrintln("OCW: " + ocwfile.getName());
			
			for(File lecfile : listDirs(ocwfile.getAbsolutePath())){
				// 各講義のディレクトリについて
				Logger.sPrintln("Lecture: " + lecfile.getName());
				// スライドの数をカウントしておく
				int slide_count = 0;
				
				for(File sfile : listFiles(lecfile.getAbsolutePath())){
					// pdfファイルについてのみ解析する。
					Pattern p = Pattern.compile(".pdf$");
					Matcher m = p.matcher(sfile.getName());
					if(!m.find()) continue;
					
					// 各スライドのファイルに対して
					Logger.sPrintln("Slide: " + sfile.getName());
					

					slide_model.name = sfile.getName();
					slide_model.lectureName = lecfile.getName();
					slide_model.ocw = ocwfile.getName();
					
					// すでに解析していたファイルの時に解析しなおすかどうか
					/*
					if(slide_model.exist()){
						p("データベースに存在しているファイル");
						continue;
					}*/
					SlideXMLConverter sxc = new SlideXMLConverter();
					if(sxc.convert(sfile.getAbsolutePath())){
						slide_count++;
						slide_model.page = sxc.page;
						slide_model.byteSize = (int)sxc.byteSize;
						try {
							slide_model.update();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							System.err.println("スライドデータの挿入に失敗: " + slide_model.name);
							Logger.sErrorln("スライドデータの挿入に失敗: " + slide_model.name);
							e.printStackTrace();
						}
					}
				}
				
				// スライドがない講義もあるので、スライドがある場合のみDBに追加
				if(slide_count > 0){
					lecture_model.name = lecfile.getName();
					lecture_model.ocw = ocwfile.getName();
					lecture_model.slideCount = slide_count;
					try {
						lecture_model.update();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						System.err.println("講義データの挿入に失敗: " + lecture_model.name);
						Logger.sErrorln("講義データの挿入に失敗: " + lecture_model.name);
						e.printStackTrace();
					}
				}
			}
		}
		
		lecture_model.close();
		slide_model.close();
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