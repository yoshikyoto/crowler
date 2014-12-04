package slide.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SimlecModel extends Model{
	public boolean debug = true;
	public String name1, name2, ocw1, ocw2;
	public double score;
	
	public void insertUpdate() throws SQLException{
		if(exist()){
			update();
		}else{
			insert();
		}
	}
	
	public void update() throws SQLException{
		String sql = "update simlec set score = ? where name1 = ? and ocw1 = ? and name2 = ? and ocw2 = ?;";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setDouble(1, score);
		pstmt.setString(2, name1);
		pstmt.setString(3, ocw1);
		pstmt.setString(4, name2);
		pstmt.setString(5, ocw2);
		if(debug) System.out.println(pstmt.toString());
		pstmt.executeUpdate();
		pstmt.close();
	}
	
	public void insert() throws SQLException{
		String sql = "insert into simlec values (?, ?, ?, ?, ?);";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, name1);
		pstmt.setString(2, ocw1);
		pstmt.setString(3, name2);
		pstmt.setString(4, ocw2);
		pstmt.setDouble(5, score);
		if(debug) System.out.println(pstmt.toString());
		pstmt.executeUpdate();
		pstmt.close();
	}
	
	public boolean exist(){
		try{
			String sql = "select * from simlec where name1 = ? and ocw1 = ? and name2 = ? and ocw2 = ?;";
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, name1);
			pstmt.setString(2, ocw1);
			pstmt.setString(3, name2);
			pstmt.setString(4, ocw2);
			if(debug) System.out.println(pstmt.toString());
			ResultSet rs = pstmt.executeQuery();
			boolean result = rs.next();
			pstmt.close();
			return result;
		}catch(Exception e){
			return false;
		}
	}
}