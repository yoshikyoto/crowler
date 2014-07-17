import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Lecture{
	public String name;
	public String ocwDomain;
	public ArrayList<String> PDFURLs;
	public String html;
	public final int INTERVAL = 100; // ms
	public ArrayList<Slide> slides;
	public ArrayList<SlideCalc> slideCalcs;
	public HashMap<String, Integer> tfMap;
	public HashMap<String, Integer> dfMap;
	File dir;
	
	// TODO: ここでスライドを取得
	Lecture(String n, String d){
		name = n;
		dir = new File(d);

		System.out.println("initializing Lecture:\t" + dir.toString());
		PPTFiles pf = new PPTFiles(dir.toString());
		String slide_files[] = pf.getPresentaitonPathArray();
		
		slides = new ArrayList<Slide>();
		for(String slide_file : slide_files){
			Slide slide = new Slide(slide_file);
			slides.add(slide);
		}
		
		slideCalcs = new ArrayList<SlideCalc>();
		for(Slide slide : slides){
			try{
				SlideCalc sc = new SlideCalc(slide);
				sc.clustering();
				sc.labeling();
			}catch(Exception e){
				// TODO: Lecture<init> SlideCalc 計算中のエラー 多分文字コードのせい
				System.err.println("Lecture<init> SlideCalc 計算中のエラー 多分文字コードのせい");
				e.printStackTrace();
			}
		}
		
		// TF,DF 計算
		tfMap = new HashMap<String, Integer>();
		dfMap = new HashMap<String, Integer>();
		for(Slide slide : slides){
			if(slide.tfMap != null && slide.dfMap != null){
				Set<String> wordSet = slide.tfMap.keySet(); // 単語(key)のSetを取得
				for(String word : wordSet){
					int tf = slide.tfMap.get(word);
					if(tfMap.containsKey(word)) tf += tfMap.get(word);
					tfMap.put(word, tf);
					
					int df = 1;
					if(dfMap.containsKey(word)) df += dfMap.get(word);
					dfMap.put(word, df);
				}
				saveWordMap();
			}
		}
	}
	
	void saveWordMap(){
		try{
			File file = new File(dir.toString() + "/wordMap.txt");	
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);	
			PrintWriter pw = new PrintWriter(bw);

			Set<String> wordSet = tfMap.keySet(); // 単語(key)のSetを取得
			for(String word : wordSet){
				pw.print(word + "\t");
				pw.print(tfMap.get(word) + "\t");
				pw.print(dfMap.get(word) + "\n");
			}
			
			pw.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
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