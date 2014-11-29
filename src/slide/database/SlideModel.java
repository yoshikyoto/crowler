package slide.database;

import java.sql.*;

public class SlideModel extends Model{
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
}