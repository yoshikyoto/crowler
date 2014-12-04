package slide.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SlideModel extends Model{
	public String name, lectureName, ocw;
	public int page, segmentCount = 0, byteSize, imageCount = 0, allWordCount = 0;

	
	/**
	 * カラムが存在している場合はupdate、そうでない場合はinsertを行う。
	 * @throws SQLException
	 */
	public void update() throws SQLException{
		if(exist()){
			p("存在していた");
			String sql = "update slide set page = ?, segmentCount = ?, byte = ?, image_count = ?, all_word_count = ? where name = ? and lectureName = ? and ocw = ?";
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, page);
			pstmt.setInt(2, segmentCount);
			pstmt.setInt(3, byteSize);
			pstmt.setInt(4, imageCount);
			pstmt.setInt(5, allWordCount);
			pstmt.setString(6, name);
			pstmt.setString(7, lectureName);
			pstmt.setString(8, ocw);
			System.out.println(pstmt.toString());
			pstmt.executeUpdate();
			pstmt.close();
		}else{
			p("存在していなかった");
			insert();
		}
	}
	
	/**
	 * 単純なinsertを行う。既に存在している場合は SQLException が投げられる。
	 * @throws SQLException
	 */
	public void insert() throws SQLException{
		String sql = "insert into slide values (?, ?, ?, ?, ?, ?, ?, ?);";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, name);
		pstmt.setString(2, lectureName);
		pstmt.setString(3, ocw);
		pstmt.setInt(4, page);
		pstmt.setInt(5, segmentCount);
		pstmt.setInt(6, byteSize);
		pstmt.setInt(7, imageCount);
		pstmt.setInt(8, allWordCount);
		
		pstmt.executeUpdate();
		pstmt.close();
	}
	
	/**
	 * カラムが1つ以上存在している場合は true を返す。
	 * @return
	 */
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
	
	/**
	 * name, lectureName, ocw から検索を行う。
	 * @return
	 */
	public boolean query(){
		try{
			String sql = "select * from slide where name = ? and lectureName = ? and ocw = ?;";
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setString(2, lectureName);
			pstmt.setString(3, ocw);
			ResultSet rs = pstmt.executeQuery();
			boolean result = rs.next();
			
			if(result){
				page = rs.getInt("page");
				segmentCount = rs.getInt("segmentCount");
				byteSize = rs.getInt("byte");
				imageCount = rs.getInt("image_count");
				allWordCount = rs.getInt("all_word_count");
			}
			pstmt.close();
			return result;
		}catch(Exception e){
			return false;
		}
	}
	
	/**
	 * すべての講義を取得する。
	 * next を使って次々に値を取得できる。
	 */
	private ResultSet allrs;
	public void getAll() throws SQLException{
		String sql = "select * from slide;";
		Statement stmt = con.createStatement();
		allrs = stmt.executeQuery(sql);
	}
	
	public boolean next() throws SQLException{
		boolean result = allrs.next();
		if(result){
			// 結果が見つかった場合
			name = allrs.getString("name");
			lectureName = allrs.getString("lectureName");
			ocw = allrs.getString("ocw");
			page = allrs.getInt("page");
			segmentCount = allrs.getInt("segmentCount");
			byteSize = allrs.getInt("byte");
			imageCount = allrs.getInt("image_count");
			allWordCount = allrs.getInt("all_word_count");
		}else{
			allrs.close();
		}
		return result;
	}
	
	/**
	 * ディレクトリを取得できる。ルートとなるディレクトリは親クラスの Model に記述してあるが、上書きも可能。
	 * @return
	 */
	public String getDirName(){
		return root + "/" + ocw + "/" + lectureName + "/" + name;
	}
}
