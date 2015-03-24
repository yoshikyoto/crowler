package slide.SegmentSim;

import jp.dip.utakatanet.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import slide.Cosim;
import slide.database.LectureModel;
import slide.database.SimlecModel;
import slide.database.SimsegModel;
import slide.database.SlideModel;

public class SegSimCalculator extends Base{
	public static String datapath = "/Users/admin/ocwslidedata";
	public static void calc() throws SQLException{
		
		Logger.setLogName("SegmentSim");
		
		// 講義一覧を取得
		LectureModel lectureA_model = new LectureModel();
		lectureA_model.getAll();
		while(lectureA_model.next()){
			// if(!lectureA_model.name.equals("06-コンパイラ")) continue;
			// 講義のdfを取得
			HashMap<String, Double> lecA_dfMap = getDfMap(lectureA_model.getDirName());
			
			// セグメント一覧を取得
			ArrayList<Segment> lecA_segments = makeSegmentArr(lectureA_model, lecA_dfMap);
			
			// 類似講義を取得
			SimlecModel simlec_model = new SimlecModel();
			simlec_model.getAllSimLec(lectureA_model.name, lectureA_model.ocw, 0.02);
			while(simlec_model.next()){
				LectureModel lectureB_model = new LectureModel();
				lectureB_model.name = simlec_model.name2;
				lectureB_model.ocw = simlec_model.ocw2;
				lectureB_model.query();
				
				Logger.sPrintln(lectureA_model.name + " " + lectureB_model.name + " " + simlec_model.score);
				
				// 講義Bのdfを取得
				HashMap<String, Double> lecB_dfMap = getDfMap(lectureB_model.getDirName());
				// セグメントリストを取得
				ArrayList<Segment> lecB_segments = makeSegmentArr(lectureB_model, lecB_dfMap);
				
				calcSegSim(lecA_segments, lecB_segments);
			}
		}
		
		
		// ocw一覧取得
		/*
		for(File ocwfile : listDirs(datapath)){
			if(ocwfile.getName().indexOf("kyoto") == -1) continue; // FIXME: 京大のみについて観てみる
			// 各ocwにいて
			Logger.sPrintln("OCW: " + ocwfile.getName());
			
			// 講義一覧取得
			for(File lecfile : listDirs(ocwfile.getAbsolutePath())){
				// 各講義に対しての処理 セグメントの類似度を測る
				SegSim ss = new SegSim(lecfile);
				ss.calcSegSim();
			}
		}
		*/

		Logger.sClose();
	}
	
	/**
	 * ２つのセグメントリストを入力として、セグメントの類似度を計算。データベースに格納する。
	 * @param segmentsA
	 * @param segmentsB
	 * @throws SQLException 
	 */
	public static void calcSegSim(ArrayList<Segment> segmentsA, ArrayList<Segment> segmentsB) throws SQLException{
		SimsegModel simseg_model = new SimsegModel();
		
		for(Segment segA : segmentsA){
			SlideModel s1 = new SlideModel();
			s1.ocw = segA.ocw;
			s1.lectureName = segA.lectureName;
			s1.name = segA.slideName;
			simseg_model.s1 = s1;
			simseg_model.segnum1 = segA.segmentNum;
			
			for(Segment segB : segmentsB){
				SlideModel s2 = new SlideModel();
				s2.ocw = segB.ocw;
				s2.lectureName = segB.lectureName;
				s2.name = segB.slideName;
				simseg_model.s2 = s2;
				simseg_model.segnum2 = segB.segmentNum;
				// コサイン類似度計算
				double cosim = Cosim.calc(segA.tfidfMap, segB.tfidfMap);
				if(cosim > 0.01){
					// 一定以上なら一応データベースに記憶
					simseg_model.score = cosim;
					simseg_model.insertUpdate();
				}
			}
		}
	}
	
	/**
	 * 講義のディレクトリを入力するとそのDFのHashMapを返してくれる
	 * @param dir
	 * @return
	 */
	public static HashMap<String, Double> getDfMap(String dir){
		HashMap<String, Double> map = new HashMap<String, Double>();
		BufferedReader br;
		try {
			br = getFileBr(dir + "/wordMap.txt");
			String line;
			while((line = br.readLine()) != null){
				String strs[] = line.split(" ");
				String word = strs[0];
				Double tf = Double.parseDouble(strs[1]);
				Double df = Double.parseDouble(strs[2]);
				map.put(word, df);
			}
			br.close();
			return map;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return map;
		}
	}
	
	/**
	 * ディレクトリの名前とdfのHashMapを入力すると、
	 * セグメント一をすべてとってきて、tf-idfのMapを作成する。
	 * SegmentのArrayListを返す。
	 * @param lecture_dir
	 * @param dfm
	 * @return
	 * @throws SQLException 
	 */
	public static ArrayList<Segment> makeSegmentArr(LectureModel lecture_model, HashMap<String, Double> dfm) throws SQLException{
		ArrayList<Segment> segs = new ArrayList<Segment>();
		
		// LectureModelのスライドを取得
		SlideModel slide_model = new SlideModel();
		slide_model.getSlides(lecture_model);
		while(slide_model.next()){
			// p("スライドを取得: " + slide_model.name);
			// sleep();
			// 各セグメントを見る
			for(int i = 0; i < slide_model.segmentCount; i++){
				// ファイルからセグメントの単語ベクトルを取得
				// セグメント番号は0からスタート
				File segfile = new File(slide_model.getDirName() + "/segment" + i + ".txt");
				Segment seg = new Segment(slide_model, i);
				// segmentのtfMapをつくる
				HashMap<String, Double> segtfMap = new HashMap<String, Double>();
				try {
					FileReader fr = new FileReader(segfile);
					BufferedReader br = new BufferedReader(fr);
					String line;
					while((line = br.readLine()) != null){
						String strs[] = line.split(" ");
						String word = strs[0];
						Double stf = Double.parseDouble(strs[1]);
						seg.setTfIdf(word, stf, dfm);
					}
					br.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				segs.add(seg);
			}
		}
		return segs;
	}

}