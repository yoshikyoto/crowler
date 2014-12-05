package slide.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MappingModel extends Model{
	public double score;
	public SlideModel s1, s2;
	public int segnum1, segnum2;
	public static boolean DEBUG = true;
	
	/**
	 * 属性が同じなので、simlec_model から値をセットするセッター
	 * @param simseg_model
	 */
	public void set(SimsegModel simseg_model){
		s1 = simseg_model.s1;
		s2 = simseg_model.s2;
		score = simseg_model.score;
		segnum1 = simseg_model.segnum1;
		segnum2 = simseg_model.segnum2;
	}
	
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
		String sql = "update mapping set score = ? where ocw1 = ? and lecture_name1 = ? and slide_name1 = ? and segment1 = ?"
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
		String sql = "insert into mapping values (?, ?, ?, ?, ?, ?, ?, ?, ?);";
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
			String sql = "select * from mapping where ocw1 = ? and lecture_name1 = ? and slide_name1 = ? and segment1 = ?"
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
}