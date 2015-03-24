package slide.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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
	
	public static String getMapping(SlideModel sm1, SlideModel sm2) throws SQLException{
		String sql = "select * from mapping"
				+ " where ocw1 = ? and lecture_name1 = ? and slide_name1 = ?"
				+ " and ocw2 = ? and lecture_name2 = ? and slide_name2 = ?";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, sm1.ocw);
		pstmt.setString(2, sm1.lectureName);
		pstmt.setString(3, sm1.name);
		pstmt.setString(4, sm2.ocw);
		pstmt.setString(5, sm2.lectureName);
		pstmt.setString(6, sm2.name);
		
		if(DEBUG) p(pstmt.toString());
		
		ResultSet rs = pstmt.executeQuery();
		String res = "";
		while(rs.next()){
			int segment1 = rs.getInt("segment1");
			int segment2 = rs.getInt("segment2");
			double s = rs.getDouble("score");
			res += segment1 + "," + segment2 + "," + String.format("%.3f", s) + " ";
		}
		return res;
	}
	
	/**
	 * slide_modelに対して、Mappingのある lecture_model の中の SlideModel を返す。
	 * @param slide_model
	 * @param lecture_model
	 * @return
	 * @throws SQLException 
	 */
	public static ArrayList<SlideModel> getSlides(SlideModel slide_model, LectureModel lecture_model) throws SQLException{
		ArrayList<SlideModel> res = new ArrayList<SlideModel>();
		String sql = "select distinct * from mapping"
				+ " where ocw1 = ? and lecture_name1 = ? and slide_name1 = ?"
				+ " and ocw2 = ? and lecture_name2 = ? group by slide_name2";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, slide_model.ocw);
		pstmt.setString(2, slide_model.lectureName);
		pstmt.setString(3, slide_model.name);
		pstmt.setString(4, lecture_model.ocw);
		pstmt.setString(5, lecture_model.name);
		
		if(DEBUG) p(pstmt.toString());
		
		ResultSet rs = pstmt.executeQuery();
		while(rs.next()){
			SlideModel sm = new SlideModel();
			sm.ocw = lecture_model.ocw;
			sm.lectureName = lecture_model.name;
			sm.name = rs.getString("slide_name2");
			res.add(sm);
		}
		return res;
	}
	
	/**
	 * MappingのあるLectureModelをimage_degree順にソートした ArrayList<LectureModel> を返す。
	 * イテレータをまわす仕様ではない点に注意。
	 * @param slide_model
	 * @return
	 * @throws SQLException
	 */
	public static ArrayList<LectureModel> getLecturees(SlideModel slide_model) throws SQLException{
		ArrayList<LectureModel> res = new ArrayList<LectureModel>();
		String sql = "select * from lecture, "
				+ "( select distinct ocw2, lecture_name2"
				+ " from mapping"
				+ " where ocw1 = ? and lecture_name1 = ? and slide_name1 = ? ) as sim "
				+ "where ocw = ocw2 and name = lecture_name2 order by image_degree";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, slide_model.ocw);
		pstmt.setString(2, slide_model.lectureName);
		pstmt.setString(3, slide_model.name);
		
		if(DEBUG) p(pstmt.toString());
		
		ResultSet rs = pstmt.executeQuery();
		while(rs.next()){
			LectureModel lecture_model = new LectureModel();
			lecture_model.ocw = rs.getString("ocw2");
			lecture_model.name = rs.getString("lecture_name2");
			lecture_model.imageDegree = rs.getDouble("image_degree");
			lecture_model.imageScore = rs.getDouble("image_score");
			lecture_model.wordScore = rs.getDouble("word_score");
			res.add(lecture_model);
		}
		return res;
	}
}