package slide.html;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import slide.Main.*;

public class SlideHtmlMaker extends SlideMain{
	public static void make(){
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
	 * 講義の中の1スライドファイルからhtmlを生成
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
}