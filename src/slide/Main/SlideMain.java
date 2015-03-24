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
import slide.converter.Pdf2Image;
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
		

		// Pdf2Image.convert("ocw.hokudai.ac.jp", "グラフ理論（2006年度）","GraphTheory-2006-Slide-01");

		
		/*
		Logger.setLogName("log");
		SlideModel sm = new SlideModel();
		sm.ocw = "ocw.kyoto-u.ac.jp";
		sm.lectureName = "02-(〜2010年度)社会健康医学基礎スキルI：文献検索・評価法";
		sm.name = "02";
		sm.query();
		PdfXMLConverter c = new PdfXMLConverter();
		c.convert(sm.getDirName() + ".pdf");
		*/

		
/*
		// LectureModelのimage_degreeを計算する
		LectureModel.open();
		LectureModel.calcImageDegree();
		
		// image degreeによってソートしてみる
		LectureModel.open();
		ArrayList<LectureModel> lectures = LectureModel.getImageDegreeRanking();
		SlideHtmlMaker.makeImageDegreeRanking(lectures, "image_degree.html", 3, true);
		SlideHtmlMaker.makeImageDegreeRanking(lectures, "image_degree_textonly.html", 1, false);
		
		lectures = LectureModel.getAllImageDegreeRanking();
		SlideHtmlMaker.makeImageDegreeRanking(lectures, "image_degree_all.html", 5, true);
		SlideHtmlMaker.makeImageDegreeRanking(lectures, "image_degree_all_textonly.html", 1, false);

		lectures = LectureModel.getWordRanking();
		SlideHtmlMaker.makeImageDegreeRanking(lectures, "word.html", 1, true);
		
		lectures = LectureModel.getWordRankingAc();
		SlideHtmlMaker.makeImageDegreeRanking(lectures, "word_ac.html", 1, true);

		lectures = LectureModel.getImageRanking();
		SlideHtmlMaker.makeImageDegreeRanking(lectures, "image.html", 1, true);

		lectures = LectureModel.getImageRankingAc();
		SlideHtmlMaker.makeImageDegreeRanking(lectures, "image_ac.html", 1, true);
*/
		Scanner sc = new Scanner(System.in);
		
		p("Input Number");
		p("0. Crawling (under construction)");
		p("1. slide/pdf to XML");
		p("2. Calc segment");
		p("3. calc Lecture Similarity");
		p("4. calc Segment similarity");
		p("5. Segment Mapping");
		p("6. Make HTML");
		p("7. Convert Slide to Image");
		p("8. for Evaluation");
		p("9. check tf-idf border");
		
		switch(sc.nextInt()){
		case 0: // Crawling?
			Crawler.start();
			break;
		case 1: // slide/pdf to XML
			newSlideToXML();
			break;
		case 2: // Calc segment
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
		case 7:
			convertSlide2Image();
			break;
		case 8: // 評価実験用
			Slide.tfidf_border = 0.07;
			slideAnalyzeForEval();
			break;
		case 9:
			checkTfidfBorder();
			break;
		case 10:
			//Slide.tfidf_border = 0.1;
			checkTBorder();
			break;
		}
		sc.close();
		// SlideData d = new SlideData("/Users/admin/ocwslidedata");
		
		p("終了: " + getDateString("MM/dd HH:mm:ss"));
	}
	
	public static void newSlideToXML() throws SQLException{
		Logger.setLogName("ConvertXML");
		Pdf2Image pdf = new Pdf2Image();
		pdf.getAllLecture();
		while(pdf.next()){
			File lecfile = new File(pdf.getLectureDir());
			Logger.sPrintln("Lecture: " + lecfile.getName());
			int slide_count = 0;
			
			for(File sfile : listFiles(lecfile.getAbsolutePath())){
				// pdfファイルについてのみ解析する。
				Pattern p = Pattern.compile(".pdf$");
				Matcher m = p.matcher(sfile.getName());
				if(!m.find()) continue;
				
				// 各スライドのファイルに対して
				Logger.sPrintln("Slide: " + sfile.getName());
				

				// TODO: nameWithout Ext のデバッグ
				// slide_model.name = sfile.getName(); これだと拡張子まで含まれてしまうのでだめ
				SlideModel slide_model = new SlideModel();
				slide_model.lectureName = lecfile.getName();
				slide_model.ocw = pdf.ocw;
				
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
					slide_model.imageCount = sxc.imageCount;
					slide_model.name = sxc.nameWithoutExt;
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
				LectureModel lecture_model = new LectureModel();
				lecture_model.name = lecfile.getName();
				lecture_model.ocw = pdf.ocw;
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
		
		Logger.sClose();
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
				if(!lecfile.getName().equals("グラフ理論（2005年度）")) continue; 
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
					

					// TODO: nameWithout Ext のデバッグ
					// slide_model.name = sfile.getName(); これだと拡張子まで含まれてしまうのでだめ
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
						slide_model.imageCount = sxc.imageCount;
						slide_model.name = sxc.nameWithoutExt;
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
	
	public static void slideAnalyze() throws SQLException{
		Logger.setLogName("ConvertXML");
		
		SlideModel.root = root;
		SlideModel slide_model = new SlideModel();
		slide_model.getAll();
		while(slide_model.next()){
			if(!slide_model.name.equals("compiler_02")) continue;
			String xml_path = slide_model.getDirName() + "/slide.xml";
			File xml_file = new File(xml_path);
			if(xml_file.exists()){
				SlideAnalyzer.analyze(slide_model);
			}
		}
		
		/*
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
						SlideAnalyzer.analyze(ocwfile.getName(), lecfile.getName(), sfile.getName());
					}else{
						Logger.sErrorln("ファイルが見つかりませんでした: " + xml_path);
					}
					System.gc();
				}
			}
		}
		*/
		Logger.sClose();
	}

	public static void checkTBorder() throws SQLException{
		double[] borders = {0.1, 0.05, 0.01, 0.00000001};
		for(double border : borders){
			Slide.t_border = border;
			slideAnalyzeForEval();
		}
	}
	
	public static void checkTfidfBorder() throws SQLException{
		double[] borders = {0.1, 0.09, 0.08, 0.07, 0.06, 0.05, 0.04, 0.03, 0.02, 0.01};
		for(double border : borders){
			Slide.tfidf_border = border;
			slideAnalyzeForEval();
		}
	}
	
	public static void slideAnalyzeForEval() throws SQLException{
		
		SlideModel.root = root;
		SlideModel slide_model = new SlideModel();
		slide_model.getAll();
		while(slide_model.next()){
			// System.out.println(slide_model.lectureName);
			boolean flag =
					slide_model.lectureName.equals("06-コンパイラ") || 
					slide_model.lectureName.equals("コンピュータアーキテクチャI") || 
					slide_model.lectureName.equals("05-画像処理論") || 
					slide_model.lectureName.equals("知的画像処理") || 
					slide_model.lectureName.equals("16-計算機ソフトウェア") || 
					slide_model.lectureName.indexOf("グラフ理論") >= 0 ||
					slide_model.lectureName.equals("計算理論");
					 
			if(!flag) continue;
			String xml_path = slide_model.getDirName() + "/slide.xml";
			System.out.println(xml_path);
			File xml_file = new File(xml_path);
			if(xml_file.exists()){
				SlideAnalyzer.analyze(slide_model);
			}
		}
	}
	
	
	
	public static void convertSlide2Image() throws SQLException{
		Logger.setLogName("Pdf2Image");
		
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
				
				for(File sfile : listFiles(lecfile.getAbsolutePath())){
					Pattern p = Pattern.compile("(.+)\\.pdf$");
					Matcher m = p.matcher(sfile.getName());
					if(m.find()){
						try{
							Pdf2Image.convert(ocwfile.getName(), lecfile.getName(), m.group(1));
						}catch(Exception e){
							Logger.sErrorln(e.toString());
							e.printStackTrace();
						}
					}
				}
			}
		}
		/*
		SlideModel slide_model = new SlideModel();
		slide_model.getAll();
		while(slide_model.next()){
			try{
				Pdf2Image.convert(slide_model);
			}catch(Exception e){
				e.printStackTrace();
				Logger.sError(e.toString());
			}
		}
		*/
		Logger.sClose();
	}
	
	public static boolean wp(String title){return WikipediaModel.hasPage(title);}
}