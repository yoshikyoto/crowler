import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class OCWData {
	public File dir;
	public ArrayList<Lecture> lectureList;
	HashMap<String, Integer> tfMap;
	HashMap<String, Integer> dfMap;
	
	OCWData(String ocw_dir_string){
		System.out.println("Initializing OCW Data: " + ocw_dir_string);
		dir = new File(ocw_dir_string);
		
		lectureList = new ArrayList<Lecture>();
		
		// Lecture を lecturelist に突っ込んでいく
		for(File lecture_dir : dir.listFiles()){
			// 隠しファイルと wordMap は除く
			if(lecture_dir.getName().indexOf(".") == 0) continue;
			if(lecture_dir.getName().indexOf("wordMap.txt") == 0) continue;
			// DEBUG CODE
			System.out.println("Lecture: " + lecture_dir.getName() + " (" + lecture_dir.toString() + ")");
			
			Lecture lecture = new Lecture(lecture_dir.getName(), lecture_dir.toString());
			lectureList.add(lecture);
		}
		
		// TF,DF 計算
		tfMap = new HashMap<String, Integer>();
		dfMap = new HashMap<String, Integer>();
		for(Lecture lecture : lectureList){
			if(lecture.tfMap != null){
				Set<String> wordSet = tfMap.keySet(); // 単語(key)のSetを取得
				for(String word : wordSet){
					int tf = lecture.tfMap.get(word);
					if(tfMap.containsKey(word)) tf += tfMap.get(word);
					tfMap.put(word, tf);
					
					int df = 1;
					if(dfMap.containsKey(word)) df += dfMap.get(word);
					dfMap.put(word, df);
				}
			}
			saveWordMap();
		}
		// 結果はキャッシュ（保存）
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
}
