package slide.html;

import java.io.BufferedReader;

import slide.database.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import jp.dip.utakatanet.Logger;
import slide.Main.*;

public class SlideHtmlMaker extends SlideMain{
	
	/**
	 * データベースに存在しているスライドのhtmlを生成する
	 * @throws SQLException
	 */
	public static void make() throws SQLException{
		Logger.setLogName("MakingHtml");
		LectureModel lecture_model = new LectureModel();
		lecture_model.getAll();
		while(lecture_model.next()){
			// if(!lecture_model.name.equals("06-コンパイラ")) continue;
			SlideModel slide_model = new SlideModel();
			slide_model.getSlides(lecture_model);
			while(slide_model.next()){
				// 講義のhtmlを描画する。
				try{
					makeSlideHtml(slide_model);
				}catch(Exception e){
					Logger.sError(e.toString());
					e.printStackTrace();
				}
			}
		}
		Logger.sClose();
	}
	
	/**
	 * 一つのスライドページを生成する
	 * @param slide_model
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void makeSlideHtml(SlideModel slide_model) throws IOException, SQLException{
		PrintWriter pw = getPrintWriter(slide_model.getDirName(), "index.html");
		
		// segstrを読む
		String segstr = getLineFromFile(slide_model.getDirName() + "/segment.txt");
		
		pw.println("<html>");
		pw.println("<head>");
		pw.println("<title>" + slide_model.name + "</title>");
		pw.println("<meta charset=\"utf-8\">");
		pw.println("<script async type=\"application/dart\" src=\"../../../assets/slide.dart\"></script>");
		pw.println("<link rel=\"stylesheet\" href=\"../../../assets/style.css\" type=\"text/css\"></link>");
		pw.println("</head>");
		

		pw.println("<body>");
		// FIXME: 実際は Absolute path ではない
		pw.println("<div id=\"meta\" dir=\"./\" page=\"" + slide_model.page + "\"");
		pw.println(" segstr=\"" + segstr + "\">");
		pw.println("<div id=\"app\"></div>");
		

		
		// 類似講義の情報を出していく

		pw.println("<section id=\"info\">");
		pw.println("<h1>" + slide_model.name + "</h1>");
		pw.println("<h3>類似講義</h3>");
		pw.println("<div><ul>");
		
		// mappingのある講義を取得して、各講義について見ていく。
		MappingModel.open();
		ArrayList<LectureModel> lectures = MappingModel.getLecturees(slide_model);
		int i = 0;
		for(LectureModel lecture_model : lectures){
			// pw.print(lecture_model.name + " " + lecture_model.imageDegree);
			pw.println("<li id=\"simlec" + i + "\" dir=\"" + lecture_model.getRPath() + "\">");
			pw.println(lecture_model.name + " (" + lecture_model.ocw + ")");
			
			// その中でも,mappingのあるスライドを取得する
			pw.println("<ul>");
			ArrayList<SlideModel> slides = MappingModel.getSlides(slide_model, lecture_model);
			for(SlideModel sim_slide_model : slides){
				sim_slide_model.query(); // データベースから情報を取得
				
				// セグメント文字列を取得
				String sim_segstr = getLineFromFile(sim_slide_model.getDirName() + "/segment.txt");
				
				pw.println("<li id=\"mapinfo\""
						+ " segstr=\"" + sim_segstr + "\""
						+ " page=\"" + sim_slide_model.page + "\""
						+ " simmap=\"" + MappingModel.getMapping(slide_model, sim_slide_model) + "\""
						+ " sname=\"" + sim_slide_model.name + "\">");
				pw.println(sim_slide_model.name);
				pw.println("</li>");
			}
			pw.println("</ul>");
			
			pw.println("</li>");
			i++;
		}

		pw.println("</ul></div>");
		pw.println("</section>");
		
		pw.println("</body>");
		pw.println("</html>");
		pw.close();
	}
	
	public static void old_make(){
		for(File ocwfile : listDirs(root)){ 
			// FIXME: とりあえず京大について
			if(ocwfile.getName().indexOf("kyoto") == -1) continue;
			for(File lecfile : listDirs(ocwfile.getAbsolutePath())){
				// 講義ディレクトリ
				for(File sfile : listDirs(lecfile.getAbsolutePath())){
					// スライドディレクトリ
					try {
						makeSlideHtml(sfile);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * 講義の中の1スライドファイルからhtmlを生成(古い方)
	 * @param sfile
	 * @throws IOException
	 */
	public static void makeSlideHtml(File sfile) throws IOException{
		String title = sfile.getName();
		String segstr = getLineFromFile(sfile.getAbsolutePath() + "/segment.txt");
		int page = getPageNumForSegstr(segstr);
		
		// DEBUG CODE
		p("title: " + title + "\tpage: " + page);
		
		PrintWriter pw = getPrintWriter(sfile.getAbsolutePath(), "index.html");
		pw.println("<html>");
		pw.println("<head>");
		pw.println("<title>" + title + "</title>");
		pw.println("<meta charset=\"utf-8\">");
		pw.println("<script async type=\"application/dart\" src=\"../../../assets/slide.dart\"></script>");
		pw.println("<link rel=\"stylesheet\" href=\"../../../assets/style.css\" type=\"text/css\"></link>");
		pw.println("</head>");
		
		pw.println("<body>");
		// FIXME: 実際は Absolute path ではない
		pw.println("<div id=\"meta\" dir=\"" + sfile.getAbsolutePath() + "\" page=\"" + page + "\"");
		pw.println(" segstr=\"" + segstr + "\">");
		pw.println("<div id=\"app\"></div>");
		
		// 類似講義の情報を出していく

		pw.println("<section id=\"info\">");
		pw.println("<h1>" + title + "</h1>");
		pw.println("<h3>類似講義</h3>");
		pw.println("<div><ul>");
		int i = 0;
		for(File simsfile : listFiles(sfile.getAbsolutePath())){
			// simmap から始まるテキストのみ
			if(simsfile.getName().indexOf("simmap.txt") == 0) continue;
			if(simsfile.getName().indexOf("simmap") != 0) continue;
			BufferedReader br = getFileBr(simsfile.getAbsolutePath());
			File simlecfile = new File(br.readLine());
			pw.println("<li id=\"simlec" + i + "\" dir=\"" + simlecfile.getAbsolutePath() + "\">");
			pw.println(simlecfile.getName());
			String line;
			pw.println("<ul>");
			while((line = br.readLine()) != null){
				String strs[] = line.split("\t");
				// セグメント情報が書いてあるファイルの中身を取得
				String simlec_segstr = getLineFromFile(simlecfile.getAbsolutePath() + "/" + strs[0] + "/segment.txt");
				int sim_slide_page = getPageNumForSegstr(simlec_segstr);
				pw.println("<li id=\"mapinfo\" sname=\"" + strs[0] + "\" segstr=\"" + simlec_segstr + "\" simmap=\"" + strs[1] + "\" page=\"" + sim_slide_page + "\">"+ strs[0] + "</li>");
			}
			pw.println("</ul>");
			pw.println("</li>");
			br.close();
			i++;
		}
		pw.println("</ul></div>");
		pw.println("</section>");
		
		pw.println("</body>");
		pw.println("</html>");
		pw.close();
	}
	
	/**
	 * segment.txt からページ数を取得
	 * @param sfile File
	 * @return
	 * @throws IOException
	 */
	public static int getPageNum(File sfile) throws IOException{
		String segstr = getLineFromFile(sfile.getAbsolutePath() + "/segment.txt");
		return getPageNumForSegstr(segstr);
	}
	
	/**
	 * segment.txt の中身からページ数を取得してくれる
	 * @param segstr e.g. "0 1 2 0 3 4 5 0 6 7 8 9 0 "
	 * @return
	 */
	public static int getPageNumForSegstr(String segstr){
		System.out.println("page num for segstr: " + segstr);
		String strs[] = segstr.split(" ");
		int page = Integer.parseInt(strs[strs.length - 2]);
		return page;
	}
	
	public static void makeImageDegreeRanking(ArrayList<LectureModel> lectures, String name, int span, boolean image_flag) throws SQLException, IOException{
		
		// 結果を出力する場所
		PrintWriter pw = getPrintWriter(root, name);

		pw.println("<html>");
		pw.println("<head>");
		pw.println("<meta charset=\"utf-8\">");
		pw.println("</head>");
		pw.println("<body>");
		
		for(int i = 0; i < lectures.size(); i += span){
			LectureModel lecture_model = lectures.get(i);
			pw.println("<h2><a href=\"" + lecture_model.getRPath() + "\">" + lecture_model.name + "</a>(" + lecture_model.ocw + ")</h2>");
			pw.println("<div> image_degree: " + lecture_model.imageDegree + "</div>");
			
			if(!image_flag) continue;
			// 適当に画像を取得する
			SlideModel slide_model = new SlideModel();
			slide_model.getSlides(lecture_model);
			if(!slide_model.next()) continue;
			pw.println("<nobr>");
			int image_height = 200;
			pw.println("<img src=\"" + slide_model.getRPath() + "/2.png\" height=\"" + image_height + "\">");
			pw.println("<img src=\"" + slide_model.getRPath() + "/3.png\" height=\"" + image_height + "\">");
			pw.println("<img src=\"" + slide_model.getRPath() + "/4.png\" height=\"" + image_height + "\">");
			pw.println("<img src=\"" + slide_model.getRPath() + "/5.png\" height=\"" + image_height + "\">");
			pw.println("<img src=\"" + slide_model.getRPath() + "/6.png\" height=\"" + image_height + "\">");
			pw.println("</nobr>");
		}
		
		pw.println("</body>");
		pw.println("</html>");
		pw.close();
	}
}