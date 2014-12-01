package slide.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SlideModel extends Model{
	public String name, lectureName, ocw;
	public int page, segmentCount = 0, byteSize;

	
	
	public void update() throws SQLException{
		if(exist()){
			p("存在していた");
			String sql = "update slide set page = ?, segmentCount = ?, byte = ? where name = ? and lectureName = ? and ocw = ?";
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, page);
			pstmt.setInt(2, segmentCount);
			pstmt.setInt(3, byteSize);
			pstmt.setString(4, name);
			pstmt.setString(5, lectureName);
			pstmt.setString(6, ocw);
			pstmt.executeUpdate();
			pstmt.close();
		}else{
			p("存在していなかった");
			insert();
		}
	}
	
	public void insert() throws SQLException{
		String sql = "insert into slide values (?, ?, ?, ?, ?, ?);";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, name);
		pstmt.setString(2, lectureName);
		pstmt.setString(3, ocw);
		pstmt.setInt(4, page);
		pstmt.setInt(5, segmentCount);
		pstmt.setInt(6, byteSize);
		
		pstmt.executeUpdate();
		pstmt.close();
	}
	
	public boolean exist(){
		try{
			String sql = "select * from slide where name = ? and lectureName = ? and ocw = ?;";
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, name);
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
}
