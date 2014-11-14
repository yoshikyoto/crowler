package slide.lectureSim;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import jp.dip.utakatanet.*;
import slide.*;

public class LectureSim{
	public static String datapath = "/Users/admin/ocwslidedata";
	public static HashMap<String, Integer> dfMap;
	public static ArrayList<Lecture> lectures;
	
	public static void calc(){
		Logger.setLogName("SlideSim");
		dfMap = new HashMap<String, Integer>();
		lectures = new ArrayList<Lecture>();
		
		// ocw一覧取得
		File data_dir = new File(datapath);
		for(File ocw_dir : data_dir.listFiles()){
			if(isNotValid(ocw_dir.getName())) continue;
			if(ocw_dir.getName().indexOf("www.ocw.titech.ac.jp") >= 0) continue;
			Logger.sPrintln("OCW: " + ocw_dir.getName());
			
			// 講義一覧取得
			if(ocw_dir.listFiles() == null) continue;
			for(File lecture_dir : ocw_dir.listFiles()){
				if(isNotValid(lecture_dir.getName())) continue;
				Logger.sPrintln("Lecture: " + lecture_dir.getName());
				
				Lecture lecture = new Lecture(lecture_dir.getAbsolutePath());
				if(lecture.wordMapExist) lectures.add(lecture);
				// tf dfをファイルに出力
				lecture.saveWordMap();
				
				// lecture の df を全体の方に反映させる
				for(String word : lecture.tfMap.keySet()){
					if(dfMap.containsKey(word)){
						int df = dfMap.get(word);
						dfMap.put(word, df+1);
					}else{
						dfMap.put(word, 1);
					}
				}
			}
		}
		
		// 講義の類似度を測っていく
		for(Lecture la : lectures){
			// 類似度の保存先
			try {
				File file = new File(la.dir.getAbsolutePath() + "/cosim.txt");
				FileWriter fw = new FileWriter(file);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw);
				
				// 類似度測っていく
				for(Lecture lb : lectures){
					if(la == lb) continue; // 同じ講義は図らなくていい
					// tf/idfを計算する
					HashMap<String, Double> ma = Cosim.calcTfidf(la.tfMap, dfMap);
					HashMap<String, Double> mb = Cosim.calcTfidf(lb.tfMap, dfMap);
					double cosim = Cosim.calc(ma, mb);
					if(cosim < 0.95){ // 類似度が一定以上なら
						pw.println(cosim + "\t" + lb.name + "\t" + lb.dir.getAbsolutePath());
					}
				}
				pw.close();
			}catch(Exception e){
				e.printStackTrace();
				Logger.sErrorln("IOError: " + e);
			}
			

		}
		Logger.sClose();
	}
	
	public static boolean isNotValid(String name){
		if(name.indexOf(".") == 0) return true;
		if(name.indexOf(".txt") != -1) return true;
		return false;
	}

}