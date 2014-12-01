package slide.database;

import java.sql.*;

public class LectureModel extends Model{
	public String name, ocw;
	public int slideCount;
	
	public void insert() throws SQLException{
		String sql = "insert into lecture values (?, ?, ?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, name);
		pstmt.setString(2, ocw);
		pstmt.setInt(3, slideCount);
		
		pstmt.executeUpdate();
		pstmt.close();
	}
	
	public void update() throws SQLException{
		if(exist()){
			String sql = "update lecture set slideCount = ? where name = ? and ocw = ?";
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
	
	public boolean exist(){
		try{
			String sql = "select * from lecture where name = ? and ocw = ?;";
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setString(2, ocw);
			ResultSet rs = pstmt.executeQuery();
			pstmt.close();
			return rs.next();
		}catch(Exception e){
			return false;
		}
	}
}