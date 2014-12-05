package slide.database;

import java.sql.*;

public class LectureModel extends Model{
	public String name, ocw;
	public int slideCount;
	
	/**
	 * nameとocwから検索を行いslideCountを見つけてくる。
	 * 見つからなかった場合はfalseを返し、slideCountの値は変更されない。
	 * @return 見つかった場合はtrue、見つからなかった場合は false;
	 */
	public boolean query(){
		try{
			String sql = "select * from lecture where name = ? and ocw = ?;";
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setString(2, ocw);
			ResultSet rs = pstmt.executeQuery();
			boolean result = rs.next();
			
			if(result){
				slideCount = rs.getInt("slide_count");
			}
			pstmt.close();
			return result;
		}catch(Exception e){
			return false;
		}
	}
	
	/**
	 * SQLのinsertを行う
	 * @throws SQLException 値が既に存在している場合
	 */
	public void insert() throws SQLException{
		String sql = "insert into lecture values (?, ?, ?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, name);
		pstmt.setString(2, ocw);
		pstmt.setInt(3, slideCount);
		
		pstmt.executeUpdate();
		pstmt.close();
	}
	
	/**
	 * 既にカラムが存在している場合はsqlのupdateを行い、
	 * 存在していない場合にはinsertを行う
	 * @throws SQLException SQLの設定が間違っている可能性が高い
	 */
	public void update() throws SQLException{
		if(exist()){
			String sql = "update lecture set slide_count = ? where name = ? and ocw = ?";
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, slideCount);
			pstmt.setString(2, name);
			pstmt.setString(3, ocw);
			pstmt.executeUpdate();
			pstmt.close();
		}else{
			insert();
		}
	}
	
	/**
	 * select文で存在していればexist。値のセットは行われない。
	 * （値は上書きされない）
	 * @return
	 */
	public boolean exist(){
		try{
			String sql = "select * from lecture where name = ? and ocw = ?;";
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setString(2, ocw);
			ResultSet rs = pstmt.executeQuery();
			boolean result = rs.next();
			pstmt.close();
			return result;
		}catch(Exception e){
			return false;
		}
	}
	
	private ResultSet allrs;
	/**
	 * すべての講義を取得する。next() でイテレータを回す。
	 * @throws SQLException
	 */
	public void getAll() throws SQLException{
		String sql = "select * from lecture;";
		Statement stmt = con.createStatement();
		allrs = stmt.executeQuery(sql);
	}
	
	/**
	 * イテレータを回す。値が存在しない場合にはfalseを返す。
	 * 値のセットも行われる。
	 * @return
	 * @throws SQLException
	 */
	public boolean next() throws SQLException{
		boolean result = allrs.next();
		if(result){
			// 結果が見つかった場合
			name = allrs.getString("name");
			ocw = allrs.getString("ocw");
			slideCount = allrs.getInt("slide_count");
		}else{
			allrs.close();
		}
		return result;
	}
	
	public String getDirName(){
		return root + "/" + ocw + "/" + name;
	}
	
	/**
	 * image_degree を計算して挿入する
	 * @throws SQLException 
	 */
	public static void calcImageDegree() throws SQLException{
		LectureModel lecture_model = new LectureModel();
		lecture_model.getAll();
		while(lecture_model.next()){
			SlideModel slide_model = new SlideModel();
			slide_model.getSlides(lecture_model);
			
			int image_score = 0, word_score = 0;
			while(slide_model.next()){
				if(slide_model.page == 0) continue;
				int avg_image = slide_model.imageCount / slide_model.page;
			}
		}
	}
}