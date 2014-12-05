package jp.dip.utakatanet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * 主にエイリアスを含んだクラスです。
 * 継承して使ったりすると便利かと思います。
 * @author @yoshiki_utakata
 * 
 */
public class Base{
	// 入出力関係 --------------------------------------------------
	/**
	 * System.out.println(String) のエイリアスです。
	 * @param str 入力文字列
	 */
	public static void p(String str){System.out.println(str);}
	public static void p(int i){System.out.println(i);}
	public static void p(double d){System.out.println(d);}
	
	

	/**
	 * System.out.println(String) のエイリアスです。
	 * @param str 入力文字列
	 */
	public static void println(String str){System.out.println(str);}

	
	/**
	 * System.out.print(String) のエイリアスです。
	 * @param str 入力文字列
	 */
	public static void print(String str){System.out.print(str);}
	
	
	/**
	 * System.out.print(String) のエイリアスです。
	 * @param str 入力文字列
	 */
	public static BufferedReader getBufferedReader(){return new BufferedReader(new InputStreamReader(System.in));}


	// ファイル関係 --------------------------------------------------
	/**
	 * ディレクトリが存在していない場合はそれを作成した上でファイルを作成し、
	 * そのファイルへの PrintWriter を返します。
	 * @param dir ファイルのディレクトリ(String, 絶対パス, 最後の"/"不要)です。存在しない場合は作成します。
	 * @param file 作成するファイル名(String, dirに対する相対パス)です。
	 * @throws IOException 
	 */
	public static PrintWriter getPrintWriter(String dir, String file) throws IOException{
		File dfile = new File(dir);
		dfile.mkdirs();
		FileWriter fw = new FileWriter(dir + "/" + file);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);
		return pw;
	}
	
	/**
	 * 単純に入力されたファイルへのPrintWriterを作成します。
	 * @param file ファイル名(String, 絶対パス)です
	 * @throws IOException ディレクトリが存在しない場合など
	 */
	public static PrintWriter getPrintWriter(String file) throws IOException{
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);
		return pw;
	}
	
	/**
	 * ファイルの BufferedReader を返します。
	 * @param file 読みたいファイル名(String, 絶対パス)
	 * @return　file への BufferedReader
	 * @throws FileNotFoundException ファイルが見つからない場合
	 */
	public static BufferedReader getFileBr(String file) throws FileNotFoundException{
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		return br;
	}
	
	/**
	 * 入力されたファイル名(String)の中身を読んで返します。改行も保持されます。
	 * @param file 対象ファイル名(String, 絶対パス)
	 * @return ファイルの内容 (String)
	 * @throws IOException ファイルが存在しない場合など
	 */
	public static String getStringFromFile(String file) throws IOException{
		BufferedReader br = getFileBr(file);
		String line, result = "";
		while((line = br.readLine()) != null)
			result += line + "\n";
		br.close();
		return result;
	}
	
	/**
	 * 入力されたファイル名(String)の中身を読んで返しますが、こちらは改行が含まれません。
	 * @param file 対象ファイル名(String, 絶対パス)
	 * @return ファイルの内容 (String)
	 * @throws IOException ファイルが存在しない場合など
	 */
	public static String getLineFromFile(String file) throws IOException{
		BufferedReader br = getFileBr(file);
		String line, result = "";
		while((line = br.readLine()) != null)
			result += line;
		br.close();
		return result;
	}
	
	/**
	 * 隠しファイルを除いたファイル一覧を File の ArrayList 形式で返します。<br>
	 * 隠しファイルとは、"."で始まるファイルです。
	 * @param dirStr 対象ディレクトリ (String, 絶対パス)
	 * @return ArrayList 隠しファイルを除いたFile一覧
	 */
	public static ArrayList<File> listFiles(String dirStr){
		File dir = new File(dirStr);
		ArrayList<File> files = new ArrayList<File>();
		for(File file : dir.listFiles()){
			if(file.getName().indexOf(".") == 0) continue;
			files.add(file);
		}
		return files;
	}

	/**
	 * 隠しファイルを除いた"ディレクトリ"一覧を File の ArrayList 形式で返します。<br>
	 * memo: 内部で listFiles は利用していません。<br>
	 * 隠しファイルとは、"."で始まるファイルです。
	 * @param dirStr dirStr 対象ディレクトリ (String, 絶対パス)
	 * @return ArrayList 隠しファイルを除いたディレクトリのFile一覧
	 */
	public static ArrayList<File> listDirs(String dirStr){
		File dir = new File(dirStr);
		ArrayList<File> files = new ArrayList<File>();
		for(File file : dir.listFiles()){
			if(file.getName().indexOf(".") == 0) continue;
			if(file.isDirectory()) files.add(file);
		}
		return files;
	}
	
	// ログ関係 --------------------------------------------------
	/**
	 * Logger のログの名前を指定しています。
	 * @param str ログファイル名。str.log と str.err が出力されることになる。
	 */
	public static void logName(String str){Logger.setLogName(str);}
	
	/**
	 * Logger のログを出力します。
	 * @param str String
	 */
	public static void printLog(String str){Logger.sPrintln(str);}
	
	/**
	 * Logger のエラーログを出力します。
	 * @param str String
	 */
	public static void errorLog(String str){Logger.sErrorln(str);}
	
	/**
	 * errorLog(String) の引数Exceptionバージョン
	 * @param e Exception
	 */
	public static void errorLog(Exception e){errorLog(e.toString());}
	
	/**
	 * スレッドをスリープする
	 */
	public static void sleep(int ms){try{Thread.sleep(ms);}catch(Exception e){}}
	
	/**
	 * スレッドをスリープする。引数なしの場合は1秒スリープ
	 */
	public static void sleep(){sleep(1000);}

	// Date関係 --------------------------------------------------
	/**
	 * yyyyMMddhhmmss 形式で現在の時刻(String)を返します。
	 * @return String yyyyMMddhhmmss形式の現在時刻
	 */
	public static String getDateString(){
		return getDateString("yyyyMMddhhmmss");
	}
	
	/**
	 * 入力されたフォーマットで現在時刻(String)を返します。
	 * @param format :String e.g. yyyyMMddhhmmss
	 * @return String date
	 */
	public static String getDateString(String format){
	    Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String date = sdf.format(cal.getTime());
        return date;
	}
	
	/**
	 * URL(文字列)を渡すとGETを投げて結果を返します。
	 * タブは半角スペースに置き換えられます。改行は残ります。<br>
	 * エンコード処理等はしてないので、現状日本語URLとかでは動きません。
	 * @param url_str URL(String)
	 * @return GETの結果返ってきた文字列。
	 * @throws IOException
	 */
	public static String httpGet(String url_str) throws IOException{
		URL url = new URL(url_str);
		HttpURLConnection http_connection = (HttpURLConnection)url.openConnection();
		http_connection.connect();
		InputStreamReader isr = new InputStreamReader(http_connection.getInputStream(), "SHIFT_JIS");
		BufferedReader br = new BufferedReader(isr);

		String str = "";
		String line = "";
		while((line = br.readLine()) != null)
			str += line + "\n";
		str = str.replaceAll("\t", " ");
		return str;
	}
}
