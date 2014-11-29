package jp.dip.utakatanet;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger{
	public static PrintWriter p, ep;
	public PrintWriter pw, epw;

	// Class Method
	public static void setLogName(String filename){
		if(p != null) p.close();
		
		File dir = new File("log");
		dir.mkdir();

		try {
			File file = new File("log/" + filename + ".log");
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			p = new PrintWriter(bw);
			
			File efile = new File("log/" + filename + ".err");
			FileWriter efw = new FileWriter(efile);
			BufferedWriter ebw = new BufferedWriter(efw);
			ep = new PrintWriter(ebw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void sPrint(String str){ p.print(str); p.flush(); }
	public static void sPrintln(String str){ p.println(str); p.flush();}
	public static void sClose(){ p.close(); ep.close(); ep.flush(); }
	public static void sError(String str){ ep.print(str); ep.flush(); }
	public static void sErrorln(String str){ ep.println(str); ep.flush(); }
	
	
	
	// Instance Method
	public Logger(String filename){
		File dir = new File("log");
		dir.mkdir();

		try {
			File file = new File("log/" + filename + ".log");
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			
			File efile = new File("log/" + filename + ".err");
			FileWriter efw = new FileWriter(efile);
			BufferedWriter ebw = new BufferedWriter(efw);
			epw = new PrintWriter(ebw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void println(String str){ pw.println(str); }
	public void print(String str){ pw.print(str); }
	public void errorln(String str){ epw.println(str); }
	public void error(String str){ epw.print(str); }
	public void close(){ pw.close(); epw.close(); }
}