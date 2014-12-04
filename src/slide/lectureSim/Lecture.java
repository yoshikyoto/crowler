package slide.lectureSim;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;

import slide.database.LectureModel;
import jp.dip.utakatanet.*;

/**
 * 
 * @author yoshiki_utakata
 * @param dir 講義のディレクトリ
 * @param name 講義名
 * @param simPoint Segment Mapping の時に利用する。講義の類似度。
 */
public class Lecture extends Base{
	public HashMap<String, Integer> tfMap, dfMap;
	public File dir;
	public String name;
	public double simPoint; 
	boolean wordMapExist = false;
	public String ocw;
	
	public Lecture(){}
	
	Lecture(String dir_str){
		dir = new File(dir_str);
		name = dir.getName();
		tfMap = new HashMap<String, Integer>();
		dfMap = new HashMap<String, Integer>();
		
		// 講義資料一覧取得
		for(File slide_dir : dir.listFiles()){
			if(dir.getName().indexOf(".") == 0) continue; // 隠しファイルの場合
			if(!slide_dir.isDirectory()) continue; // ディレクトリでなかった場合はbreak
			Logger.sPrintln("資料: " + slide_dir.getName());
			
			// wordMap のパスを取得
			String word_map_path = slide_dir.getAbsolutePath() + "/wordMap.txt";
			System.out.println(word_map_path);
			
			// ファイルが存在しているか確認してAnalyze
			File map_file = new File(word_map_path);
			if(map_file.exists()){
				wordMapExist = true;
				try{
					FileReader fr = new FileReader(map_file);
					BufferedReader br = new BufferedReader(fr);
					
					// wordMap をパース
					String line = "";
					while((line = br.readLine()) != null){
						String strs[] = line.split(" ");
						String word = strs[0];
						int tf = Integer.parseInt(strs[1]);
						int df = Integer.parseInt(strs[2]);
						// tfの値を更新
						if(tfMap.containsKey(word)){
							tf += tfMap.get(word);
							tfMap.put(word, tf);
						}else{
							tfMap.put(word, tf);
						}
						// dfの値を更新
						if(dfMap.containsKey(word)){
							df = dfMap.get(word);
							dfMap.put(word, df+1);
						}else{
							dfMap.put(word, 1);
						}
					}
					br.close();
				}catch(Exception e){
					Logger.sErrorln("" + e);
				}
			}else{
				Logger.sErrorln("ファイルが見つかりませんでした: " + word_map_path);
			}
		}
	}
	
	public void saveWordMap(){
		String lecture_word = dir.getAbsolutePath() + "/wordMap.txt";
		System.out.println(lecture_word);
		try {
			File file = new File(lecture_word);
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			
			for(String word : tfMap.keySet()){
				int tf = tfMap.get(word);
				int df = dfMap.get(word);
				pw.println(word + " " + tf + " " + df); 
			}
			pw.close();
		}catch(Exception e){
			Logger.sErrorln("" + e);
		}
	}
}