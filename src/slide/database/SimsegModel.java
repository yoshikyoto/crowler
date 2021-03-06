package slide.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SimsegModel extends Model{
	public double score;
	public SlideModel s1, s2;
	public int segnum1, segnum2;
	public static boolean DEBUG = true;
	
	/**
	 * カラムが存在している場合はupdate、存在していない場合はinsertを行う
	 * @return
	 * @throws SQLException 
	 */
	public void insertUpdate() throws SQLException{
		if(exist()){
			update();
		}else{
			insert();
		}
	}
	
	/**
	 * SQLのupdateを行う
	 * @throws SQLException
	 */
	public void update() throws SQLException{
		String sql = "update simseg set score = ? where ocw1 = ? and lecture_name1 = ? and slide_name1 = ? and segment1 = ?"
					+ " and ocw2 = ? and lecture_name2 = ? and slide_name2 = ? and segment2 = ?";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(2, s1.ocw);
		pstmt.setString(3, s1.lectureName);
		pstmt.setString(4, s1.name);
		pstmt.setInt(5, segnum1);
		pstmt.setString(6, s2.ocw);
		pstmt.setString(7, s2.lectureName);
		pstmt.setString(8, s2.name);
		pstmt.setInt(9, segnum2);
		pstmt.setDouble(1, score);
		System.out.println(pstmt.toString());
		pstmt.executeUpdate();
		pstmt.close();
	}
	
	/**
	 * 単純なinsertを行う。既に存在している場合は SQLException が投げられる。
	 * @throws SQLException
	 */
	public void insert() throws SQLException{
		String sql = "insert into simseg values (?, ?, ?, ?, ?, ?, ?, ?, ?);";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, s1.ocw);
		pstmt.setString(2, s1.lectureName);
		pstmt.setString(3, s1.name);
		pstmt.setInt(4, segnum1);
		pstmt.setString(5, s2.ocw);
		pstmt.setString(6, s2.lectureName);
		pstmt.setString(7, s2.name);
		pstmt.setInt(8, segnum2);
		pstmt.setDouble(9, score);
		
		pstmt.executeUpdate();
		pstmt.close();
	}
	
	/**
	 * カラムが1つ以上存在している場合は true を返す。
	 * @return
	 */
	public boolean exist(){
		try{
			String sql = "select * from simseg where ocw1 = ? and lecture_name1 = ? and slide_name1 = ? and segment1 = ?"
					+ " and ocw2 = ? and lecture_name2 = ? and slide_name2 = ? and segment2 = ?";
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, s1.ocw);
			pstmt.setString(2, s1.lectureName);
			pstmt.setString(3, s1.name);
			pstmt.setInt(4, segnum1);
			
			pstmt.setString(5, s2.ocw);
			pstmt.setString(6, s2.lectureName);
			pstmt.setString(7, s2.name);
			pstmt.setInt(8, segnum2);
			ResultSet rs = pstmt.executeQuery();
			boolean result = rs.next();
			
			pstmt.close();
			return result;
		}catch(Exception e){
			return false;
		}
	}
	
	public ResultSet toprs;
	
	/**
	 * slide_modelのsegment_numに対応するsimlec_modelのセグメントを取得する。（スコアが最も高いものを取得する）
	 * @param slide_model
	 * @param segment_num
	 * @param lecture_name
	 * @return
	 * @throws SQLException
	 */
	public boolean getTop(SlideModel slide_model, int segment_num, SimlecModel simlec_model) throws SQLException{
		s1 = slide_model;
		segnum1 = segment_num;
		
		String sql = "select * from simseg where ocw1 = ? and lecture_name1 = ? and slide_name1 = ? and segment1 = ? and ocw2 = ? and lecture_name2 = ? order by score desc limit 5";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, slide_model.ocw);
		pstmt.setString(2, slide_model.lectureName);
		pstmt.setString(3, slide_model.name);
		pstmt.setInt(4, segment_num);
		pstmt.setString(5, simlec_model.ocw2);
		pstmt.setString(6, simlec_model.name2);
		
		if(DEBUG) p(pstmt.toString());
		
		toprs = pstmt.executeQuery();
		boolean flag = toprs.next();
		if(flag){
			s2 = new SlideModel();
			s2.ocw = toprs.getString("ocw2");
			s2.lectureName = toprs.getString("lecture_name2");
			s2.name = toprs.getString("slide_name2");
			segnum2 = toprs.getInt("segment2");
			score = toprs.getDouble("score");
		}
		return flag;
	}
	
}