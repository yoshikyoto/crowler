package slide.database;

import slide.Main.*;

import java.sql.*;

public class Model extends SlideMain{
	public Connection con;
	public static String root = "/home/sakamoto/OCWData";
	
	/**
	 * Connection の初期化なんかを行う
	 */
	Model(){
		try{
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://localhost/Slide";
			con = DriverManager.getConnection(url, "sakamoto", "Sakam0toSlide");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Connection などの close を行う。
	 * 例外は throw されず、内部でキャッチする。
	 */
	public void close(){
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}