package slide.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SimlecModel extends Model{
	public boolean debug = true;
	public String name1, name2, ocw1, ocw2;
	public double score;
	
	/**
	 * カラムが存在している場合は update、存在していない場合は insert を行う
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
	
	/**
	 * SQLのinsertを行う
	 * @throws SQLException
	 */
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
	
	/**
	 * カラムが1つ以上存在している場合には true を返す。
	 * @return
	 */
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
	
	public ResultSet allrs;
	/**
	 * name, ocw に対応する講義に関して、類似度が border 以上のものを取得する。
	 * nextでイテレータを回して値を取得できる。
	 * @param name
	 * @param ocw
	 * @param boder
	 * @throws SQLException 
	 */
	public void getAllSimLec(String name, String ocw, double border) throws SQLException{
		String sql = "select * from simlec where name1 = ? and ocw1 = ? and score >= ?;";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, name);
		pstmt.setString(2, ocw);
		pstmt.setDouble(3, border);
		if(debug) System.out.println(pstmt.toString());
		allrs = pstmt.executeQuery();
	}
	
	/**
	 * イテレータを回して値を取得する
	 * @throws SQLException 
	 */
	public boolean next() throws SQLException{
		boolean result = allrs.next();
		if(result){
			// 結果が見つかった場合
			name1 = allrs.getString("name1");
			name2 = allrs.getString("name2");
			ocw1 = allrs.getString("ocw1");
			ocw2 = allrs.getString("ocw2");
			score = allrs.getDouble("score");
		}else{
			allrs.close();
		}
		return result;
	}
}