
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PPTFiles{
	String pathString;	// 入力されたディレクトリのパス
	File file;	// 入力されたファイル
	File[] files;	// ファイルが入力された場合は，その直下全てのファイル
	ArrayList<String> pptxPathArrayList;	// パワポファイルのリスト
	ArrayList<String> pdfPathArrayList;
	ArrayList<String> presentationPathArrayList;
	public static final boolean DEBUG = false;

	// イニシャライザ
	PPTFiles(String path){
		// 各種初期化
		pptxPathArrayList = new ArrayList<String>();
		pdfPathArrayList = new ArrayList<String>();
		presentationPathArrayList = new ArrayList<String>();
		pathString = path;
		file = new File(pathString);

		// 入力されたpathが.pptxの場合は，リストに追加して終了．
		Pattern pptx_pattern = Pattern.compile(".+\\.pptx\\Z");
		Matcher pptx_matcher = pptx_pattern.matcher(pathString);
		Pattern pdf_pattern = Pattern.compile(".+\\.pdf\\Z");
		Matcher pdf_matcher = pptx_pattern.matcher(pathString);
		
		if(pptx_matcher.find()){
			// .pptxの場合，配列に追加して終わり
			pptxPathArrayList.add(file.getName());
		}else if(pdf_matcher.find()){
			pdfPathArrayList.add(file.getName());
		}else{
			// .pptxでない場合は，ディレクトリであると判断
			files = file.listFiles();
			// ファイルを一つ一つ見て，pptxならリストに追加
			if(files.length == 0) return;
			for(File file : files){
				String filename = file.getName();
				pptx_pattern = Pattern.compile("\\A[A-Za-z0-9_\\-].+\\.pptx\\Z");
				pptx_matcher = pptx_pattern.matcher(filename);
				pdf_pattern = Pattern.compile("\\A[A-Za-z0-9_\\-].+\\.pdf\\Z");
				pdf_matcher = pdf_pattern.matcher(filename);
				if(pptx_matcher.find()){
					pptxPathArrayList.add(file.getPath());
					presentationPathArrayList.add(file.getPath());
					if(DEBUG) System.out.println("Find pptx file: " + filename);
				}else if(pdf_matcher.find()){
					pdfPathArrayList.add(file.getPath());
					presentationPathArrayList.add(file.getPath());
					if(DEBUG) System.out.println("Find pdf file: " + filename);
				}
			}
		}
	}

	public String[] getPPTPathArray(){
		return (String[])pptxPathArrayList.toArray(new String[0]);
	}
	
	public String[] getPDFPathArray(){
		return (String[])pdfPathArrayList.toArray(new String[0]);
	}
	
	public String[] getPresentaitonPathArray(){
		return (String[])presentationPathArrayList.toArray(new String[0]);
	}
	
}