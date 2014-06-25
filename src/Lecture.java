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
	
	Lecture(String n){
		name = n;
		PDFURLs = new ArrayList<String>();
	}
	
	public void getPDFs(){
		if(PDFURLs.size() == 0) return;
		
		File domaindir = new File(ocwDomain);
		domaindir.mkdir();
		
		File lecturedir = new File(ocwDomain + "/" + name);
		lecturedir.mkdir();
		
		for(int i = 0; i < PDFURLs.size(); i++){
			String url_str = PDFURLs.get(i);
			
			// PDFの名前を取得して
			Pattern pattern = Pattern.compile("[^/]+?\\.(pdf|pptx?)");
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
				Thread.sleep(100);
			}catch(Exception e){ }
		}
	}
	
	private void getBinary(String url_str, String name, String dir){
		try {
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