package slide.SegmentSim;

import jp.dip.utakatanet.*;

import java.util.HashMap;

import slide.database.LectureModel;
import slide.database.SlideModel;

class Segment{
	String lectureName, slideName, lecturePath, slidePath, ocw;
	int segmentNum;
	HashMap<String, Double> tfidfMap;
	
	Segment(SlideModel slide_model, int i){
		ocw = slide_model.ocw;
		lectureName = slide_model.lectureName;
		slideName = slide_model.name;
		lecturePath = slide_model.getLectureDir();
		slidePath = slide_model.getDirName();
		segmentNum = i;
		tfidfMap = new HashMap<String, Double>();
	}
	
	Segment(String lecn, String sln, int segnum, String lecp){
		lectureName = lecn;
		slideName = sln;
		segmentNum = segnum;
		lecturePath = lecp;
		slidePath = lecturePath + "/" + slideName;
		tfidfMap = new HashMap<String, Double>();
	}
	
	void setTfIdf(String word, Double tf, HashMap<String, Double> dfMap){
		Logger.sPrintln(lectureName + "\t" + segmentNum + "\t" + "tdidfを計算: " + word);
		try{
			Double tfidf = tf / dfMap.get(word);
			tfidfMap.put(word, tfidf);
		}catch(Exception e){
			e.printStackTrace();
			System.err.println(lectureName + ": " + word);
		}
	}
}