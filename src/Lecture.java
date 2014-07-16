import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Lecture{
	public String name;
	public String ocwDomain;
	public ArrayList<String> PDFURLs;
	public String html;
	public final int INTERVAL = 100; // ms
	File dir;
	
	// TODO: ここでスライドを取得
	Lecture(String n, String d){
		name = n;
		dir = new File(d);
	}
	
	Lecture(String n){
		name = n;
		PDFURLs = new ArrayList<String>();
	}
	
	public void getPDFs(){
		if(PDFURLs.size() == 0) return;
		
		File domaindir = new File("data/" + ocwDomain);
		domaindir.mkdir();
		
		File lecturedir = new File("data/" + ocwDomain + "/" + name);
		lecturedir.mkdir();
		
		for(int i = 0; i < PDFURLs.size(); i++){
			String url_str = PDFURLs.get(i);
			
			// PDFの名前を取得して
			Pattern pattern = Pattern.compile("[^/&]+?\\.(pdf|pptx?)");
			Matcher matcher = pattern.matcher(url_str);
			String pdf_name = "";
			if(matcher.find()){
				pdf_name = matcher.group();
			}else{
				pdf_name = i + ".pdf";
			}
			
			// PDFを保存
			getBinary(url_str, pdf_name, lecturedir.toString());
			
			// スリープ
			try{
				Thread.sleep(INTERVAL);
			}catch(Exception e){ }
		}
	}
	
	private void getBinary(String url_str, String name, String dir){
		try {
			// ユーザーエージェントの偽装
			// System.setProperty("http.agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X; ja-JP-mac; rv:1.8.1.6) Gecko/20070725 Firefox/2.0.0.6");
			URL url = new URL(url_str);
			URLConnection url_connection = url.openConnection();
			InputStream is = url.openStream();
			
			FileOutputStream fos = new FileOutputStream(dir + "/" + name);
			byte b[] = new byte[1024];
			int blength;
			while((blength = is.read(b)) != -1){
				fos.write(b, 0, blength);
			}
			fos.flush();
			fos.close();
			
			System.out.println("Save at: " + dir + "/" + name);
		} catch (IOException e) {
			System.out.println("DL失敗: " + url_str);
		}
	}
}