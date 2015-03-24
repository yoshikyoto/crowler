package slide.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class WikipediaModel {

	public static Connection con;
	
	/**
	 * wikipediaの目次に含まれるかどうか調べる
	 */
	public static boolean hasPage(String title){
		if(con == null){
			try{
				Class.forName("com.mysql.jdbc.Driver");
				String url = "jdbc:mysql://localhost/Wikipedia";
				con = DriverManager.getConnection(url, "sakamoto", "Sakam0toSlide");
				return exist(title);
			}catch(Exception e){
				e.printStackTrace();
				return false;
			}
		}
		return exist(title);
	}
	
	
	static boolean exist(String title){
		try{
			String sql = "select * from page where page_title = ?;";
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, title);
			ResultSet rs = pstmt.executeQuery();
			boolean result = rs.next();
			
			pstmt.close();
			return result;
		}catch(Exception e){
			return false;
		}
	}
}
