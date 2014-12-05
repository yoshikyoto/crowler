package slide.database;

import java.sql.*;

public class LectureModel extends Model{
	public String name, ocw;
	public int slideCount;
	public double imageScore, wordScore, imageDegree;
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
				imageScore = rs.getDouble("image_score");
				wordScore = rs.getDouble("wrod_score");
				imageDegree = rs.getDouble("image_degree");
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
		String sql = "insert into lecture values (?, ?, ?, ?, ?, ?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, name);
		pstmt.setString(2, ocw);
		pstmt.setInt(3, slideCount);
		pstmt.setDouble(4, imageDegree);
		pstmt.setDouble(5, imageScore);
		pstmt.setDouble(6, wordScore);
		
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
			String sql = "update lecture set slide_count = ?, image_degree = ?, image_score = ?, word_score = ? where name = ? and ocw = ?";
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, slideCount);
			pstmt.setDouble(2, imageDegree);
			pstmt.setDouble(3, imageScore);
			pstmt.setDouble(4, wordScore);
			pstmt.setString(5, name);
			pstmt.setString(6, ocw);
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
			imageDegree = allrs.getDouble("image_degree");
			imageScore = allrs.getDouble("image_score");
			wordScore = allrs.getDouble("word_score");
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
			
			double image_score = 0, word_score = 0, image_degree = 1; // 各スコア
			int slide_count = 0; // 講義に所属するスライドの数
			
			p("講義: " + lecture_model.name);
			
			// スライドをどんどん見ていく
			while(slide_model.next()){
				p("  スライド: " + slide_model.name);
				
				if(slide_model.page == 0) continue;
				slide_count++;
				int avg_image = slide_model.imageCount / slide_model.page;
				int avg_word = slide_model.allWordCount / slide_model.page;
				image_score += avg_image;
				word_score += avg_word;
				
				p("  image: " + avg_image + "  word: " + avg_word);
			}
			
			// スライド数で正規化
			// 0割しないように、スライド数が0以外の時のみ
			if(slide_count != 0){
				image_score /= slide_count;
				word_score /= slide_count;
				if(word_score != 0) image_degree = image_score / word_score;
			}
			
			// データベースをupdate
			lecture_model.imageDegree = image_degree;
			lecture_model.wordScore = word_score;
			lecture_model.imageScore = image_score;
			lecture_model.update();
		}
	}
}