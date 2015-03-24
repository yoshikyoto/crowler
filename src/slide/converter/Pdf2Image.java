package slide.converter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import jp.dip.utakatanet.Base;

import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;

import slide.Main.SlideMain;
import slide.database.LectureModel;
import slide.database.SlideModel;

import com.sun.pdfview.decode.PDFDecoder;

public class Pdf2Image extends SlideMain{
	public static void convert(SlideModel slide_model) throws Exception{
		p("デコーダーを準備");
		PdfDecoder pdf_decoder = new PdfDecoder(true);
		
		// PDFを開く(ここでpdfExceptionが出ることがある)
		p("pdfを開く");
		pdf_decoder.openPdfFile(slide_model.getDirName() + ".pdf");
		
		for(int i = 0; i < slide_model.page; i++){
			// 倍率とページ
			pdf_decoder.setPageParameters(1.0f, i+1);
			
			p("画像を取得");
			BufferedImage bi = pdf_decoder.getPageAsImage(i+1);
			
			p("画像を保存 " + slide_model.getDirName() + "/" + i + ".png");
			FileOutputStream os = new FileOutputStream(slide_model.getDirName() + "/" + i + ".png");
			ImageIO.write(bi, "PNG", os);
		}
	}
	
	public static void convert(String ocw, String lectureName, String slideName) throws PdfException, SQLException{
		if(exist(ocw, lectureName, slideName)) return;
		p("デコーダーを準備");
		PdfDecoder pdf_decoder = new PdfDecoder(true);
		
		
		String dir = root + "/" + ocw + "/" + lectureName + "/" + slideName;

		// ディレクトリがない場合は作成
		File slide_dir = new File(dir);
		slide_dir.mkdir();
		
		// PDFを開く(ここでpdfExceptionが出ることがある)
		pdf_decoder.openPdfFile(dir + ".pdf");
		p("pdfを開く");
		int i = 0, width = 0, height = 0;
		try{
			for(i = 0; true; i++){
				// 倍率とページ
				pdf_decoder.setPageParameters(1.0f, i+1);
				
				p("画像を取得");
				BufferedImage bi = pdf_decoder.getPageAsImage(i+1);
				
				// 画像の縦横を取得
				width = bi.getWidth();
				height = bi.getHeight();
				if(height >= width){
					pdf_decoder.closePdfFile();
					return;
				}
				
				p("画像を保存 " + i);
				FileOutputStream os = new FileOutputStream(dir + "/" + i + ".png");
				ImageIO.write(bi, "PNG", os);
			}
		}catch(Exception e){
			// ページが終わった場合ここに来ると思われる
			sql(ocw, lectureName, slideName, width, height, i);
			pdf_decoder.closePdfFile();
		}
	}
	
	/**
	 * 必要ならconnectionをオープンした上でupdateかinsertを実行
	 * @param ocw
	 * @param lectureName
	 * @param slideName
	 * @param width
	 * @param height
	 * @param page
	 * @throws SQLException
	 */
	public static void sql(String ocw, String lectureName, String slideName, int width, int height, int page) throws SQLException{
		if(con == null){
			try{
				Class.forName("com.mysql.jdbc.Driver");
				String url = "jdbc:mysql://localhost/Slide";
				con = DriverManager.getConnection(url, "sakamoto", "Sakam0toSlide");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		update(ocw, lectureName, slideName, width, height, page);
	}

	public static Connection con;
	
	/**
	 * カラムが存在している場合はupdate、そうでない場合はinsertを行う。
	 * @throws SQLException
	 */
	public static void update(String ocw, String lectureName, String slideName, int width, int height, int page) throws SQLException{
		if(exist(ocw, lectureName, slideName)){
			p("存在していた");
			String sql = "update slide_image set page = ?, width = ?, height = ?, where slide_name = ? and lecture_name = ? and ocw = ?";
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, page);
			pstmt.setInt(2, width);
			pstmt.setInt(3, height);
			pstmt.setString(4, slideName);
			pstmt.setString(5, lectureName);
			pstmt.setString(6, ocw);
			System.out.println(pstmt.toString());
			pstmt.executeUpdate();
			pstmt.close();
		}else{
			p("存在していなかった");
			String sql = "insert into slide_image values (?, ?, ?, ?, ?, ?);";
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, ocw);
			pstmt.setString(2, lectureName);
			pstmt.setString(3, slideName);
			pstmt.setInt(4, width);
			pstmt.setInt(5, height);
			pstmt.setInt(6, page);
			
			pstmt.executeUpdate();
			pstmt.close();
		}
	}

	/**
	 * カラムが1つ以上存在している場合は true を返す。
	 * @return
	 */
	public static boolean exist(String ocw, String lectureName, String slideName){
		try{
			String sql = "select * from slide_image where slide_name = ? and lecture_name = ? and ocw = ?;";
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, slideName);
			pstmt.setString(2, lectureName);
			pstmt.setString(3, ocw);
			ResultSet rs = pstmt.executeQuery();
			boolean result = rs.next();
			
			pstmt.close();
			return result;
		}catch(Exception e){
			return false;
		}
	}
	
	public Pdf2Image(){
		if(con == null){
			try {
				Class.forName("com.mysql.jdbc.Driver");
				String url = "jdbc:mysql://localhost/Slide";
				con = DriverManager.getConnection(url, "sakamoto", "Sakam0toSlide");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	public void getAllLecture() throws SQLException{
		String sql = "select * from slide_image group by lecture_name;";
		Statement stmt = con.createStatement();
		allrs = stmt.executeQuery(sql);
	}
	
	ResultSet allrs;
	public void getAll() throws ClassNotFoundException, SQLException{
		String sql = "select * from slide_image;";
		Statement stmt = con.createStatement();
		allrs = stmt.executeQuery(sql);
	}
	
	public String ocw, lectureName, slideName;
	public boolean next() throws SQLException{
		boolean result = allrs.next();
		if(result){
			// 結果が見つかった場合
			slideName = allrs.getString("slide_name");
			ocw = allrs.getString("ocw");
			lectureName = allrs.getString("lecture_name");
		}else{
			allrs.close();
		}
		return result;
	}
	
	public String getDirName(){
		return root + "/" + ocw + "/" + lectureName + "/" + slideName;
	}
	
	public String getLectureDir(){
		return root + "/" + ocw + "/" + lectureName;
	}
}
