package slide.lectureSim;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import jp.dip.utakatanet.*;
import slide.*;
import slide.database.LectureModel;
import slide.database.SimlecModel;

public class LectureSim extends Base{
	public static String datapath = "/Users/admin/ocwslidedata";
	public static HashMap<String, Integer> dfMap;
	public static ArrayList<Lecture> lectures;
	
	public static void calc() throws SQLException{
		Logger.setLogName("SlideSim");
		
		dfMap = new HashMap<String, Integer>();
		lectures = new ArrayList<Lecture>();

		// 講義一覧を取得
		LectureModel lecture_model = new LectureModel();
		lecture_model.getAll();
		
		// 各講義について見ていく
		while(lecture_model.next()){
			Logger.sPrintln(lecture_model.getDirName());
			Lecture lecture = new Lecture(lecture_model.getDirName());
			lecture.ocw = lecture_model.ocw;
			
			if(lecture.wordMapExist) lectures.add(lecture);
			lecture.saveWordMap(); // tf idf をファイルに出力する
			
			// lecture の df を全体の方に反映させる
			for(String word : lecture.tfMap.keySet()){
				int value = 1;
				if(dfMap.containsKey(word)) value += dfMap.get(word);
				dfMap.put(word, value);
			}
		}
		
		// ocw一覧取得
		/*
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
		*/
		
		// 講義の類似度を測っていく
		SimlecModel simlec_model = new SimlecModel();
		for(Lecture la : lectures){
			simlec_model.name1 = la.name;
			simlec_model.ocw1 = la.ocw;
			// 類似度の保存先
			try {
				/*
				File file = new File(la.dir.getAbsolutePath() + "/cosim.txt");
				FileWriter fw = new FileWriter(file);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw);
				*/
				
				// 類似度測っていく
				for(Lecture lb : lectures){
					if(la == lb) continue; // 同じ講義は図らなくていい
					simlec_model.name2 = lb.name;
					simlec_model.ocw2 = lb.ocw;
					
					// tf/idfを計算する
					HashMap<String, Double> ma = Cosim.calcTfidf(la.tfMap, dfMap);
					HashMap<String, Double> mb = Cosim.calcTfidf(lb.tfMap, dfMap);
					double cosim = Cosim.calc(ma, mb);
					if(0.001 < cosim && cosim < 0.95){
						// pw.println(cosim + "\t" + lb.name + "\t" + lb.dir.getAbsolutePath());
						simlec_model.score = cosim;
						simlec_model.insertUpdate();
					}
				}
				//pw.close();
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

	/**
	 * 類似講義を取ってくる。類似講義が足りない場合は閾値を下げる。最低5つ
	 * @param dir 講義のディレクトリ
	 * @return 類似講義の Lecture ArrayList 
	 * @throws IOException 
	 */
	public static ArrayList<Lecture> getSimilarLectures(String dir) throws IOException{
		double border = 0.1;
		ArrayList<Lecture> all = getAllSimLectures(dir + "/cosim.txt");
		ArrayList<Lecture> arr = new ArrayList<Lecture>();
		while(arr.size() < 3 && border > 0.01){
			p("border " + border);
			for(Lecture lec : all){
				// arrayに追加されてなくて類似度が高い場合は追加
				if(!arr.contains(lec) && lec.simPoint >= border){
					arr.add(lec);
				}
			}
			border = border * 0.5;
		}
		return arr;
	}
	
	/**
	 * 
	 * @param dir cosim.txtのディレクトリ名(String, 絶対パス)
	 * @return
	 * @throws IOException ファイルが見つからなかった時など
	 */
	public static ArrayList<Lecture> getAllSimLectures(String dir) throws IOException{
		BufferedReader br = getFileBr(dir);
		ArrayList<Lecture> arr = new ArrayList<Lecture>();
		String line;
		while((line = br.readLine()) != null){
			String s[] = line.split("\t");
			Lecture lec = new Lecture();
			lec.simPoint = Double.parseDouble(s[0]);
			lec.name = s[1];
			lec.dir = new File(s[2]);
			arr.add(lec);
		}
		return arr;
	}
}