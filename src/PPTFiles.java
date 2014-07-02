
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PPTFiles{
	String dir_path;	// 入力されたディレクトリのパス
	File dir;	// 入力されたファイル
	File[] all_files;	// ファイルが入力された場合は，その直下全てのファイル
	ArrayList<String> ppt_path_arrlist;	// パワポファイルのリスト

	// イニシャライザ
	PPTFiles(String path){
		// 各種初期化
		ppt_path_arrlist = new ArrayList<String>();
		dir_path = path;
		dir = new File(dir_path);

		// 入力されたpathが.pptxの場合は，リストに追加して終了．
		Pattern p = Pattern.compile(".+pptx\\Z");
		Matcher m = p.matcher(dir_path);
		if(m.find()){
			// .pptxの場合，配列に追加して終わり
			ppt_path_arrlist.add(dir.getName());
		}else{
			// .pptxでない場合は，ディレクトリであると判断
			all_files = dir.listFiles();
			// ファイルを一つ一つ見て，pptxならリストに追加
			for(int i = 0; i < all_files.length; i++){
				String filename = all_files[i].getName();
				Pattern pattern = Pattern.compile("\\A[A-Za-z0-9_\\-].+pptx\\Z");
				Matcher matcher = pattern.matcher(filename);
				if(matcher.find()){
					ppt_path_arrlist.add(all_files[i].getPath());
					System.out.println("find pptx file: " + filename);
				}
			}
		}
	}

	public String[] getPPTPathArray(){
		return (String[])ppt_path_arrlist.toArray(new String[0]);
	}
}