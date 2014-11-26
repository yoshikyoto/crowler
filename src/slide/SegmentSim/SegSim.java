package slide.SegmentSim;

import slide.*;
import jp.dip.utakatanet.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;



class SegSim extends Base{
	HashMap<String, Double> tfMap, dfMap;
	ArrayList<String> simLecs;
	ArrayList<Segment> segments;
	
	SegSim(File lecture_dir){
		FileReader fr;
		
		// まずはtf,dfを読み込む
		tfMap = new HashMap<String, Double>();
		dfMap = new HashMap<String, Double>();
		try {
			fr = new FileReader(lecture_dir.getAbsolutePath() + "/wordMap.txt");
			BufferedReader br = new BufferedReader(fr);
			
			String line;
			while((line = br.readLine()) != null){
				// tf-idfを読んではハッシュに突っ込んでいく
				String strs[] = line.split(" ");
				String word = strs[0];
				Double tf = Double.parseDouble(strs[1]);
				Double df = Double.parseDouble(strs[2]);
				tfMap.put(word, tf);
				dfMap.put(word, df);
			}
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 類似講義を読み込む
		simLecs = new ArrayList<String>();
		try {
			fr = new FileReader(lecture_dir.getAbsolutePath() + "/cosim.txt");
			BufferedReader br = new BufferedReader(fr);
			String line;
			while((line = br.readLine()) != null){
				String strs[] = line.split("\t");
				Double point = Double.parseDouble(strs[0]);
				if(point > 0.01) 
					simLecs.add(strs[2]);
			}
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 自身のsegments取得
		segments = makeSegmentArr(lecture_dir, dfMap);
		// System.out.println(lecture_dir.getName() + " セグメントサイズ: " + segments.size());
	}
	
	ArrayList<Segment> makeSegmentArr(File lecture_dir, HashMap<String, Double> dfm){
		ArrayList<Segment> segs = new ArrayList<Segment>();
		
		for(File slide_dir : lecture_dir.listFiles()){
			if(slide_dir.getName().indexOf(".") == 0) continue;
			if(!slide_dir.isDirectory()) continue;
			
			int segnum = 0;
			File segfile = new File(slide_dir.getAbsolutePath() + "/segment" + segnum + ".txt");
			while(segfile.exists()){
				//System.out.println(segfile.getAbsolutePath() + "がみつかりました");
				Segment seg = new Segment(lecture_dir.getName(), slide_dir.getName(), segnum, lecture_dir.getAbsolutePath());
				
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
				// 次のセグメントへ
				segnum++;
				segfile = new File(slide_dir.getAbsolutePath() + "/segment" + segnum + ".txt");
			}
		}
		return segs;
	}
	
	void calcSegSim(){
		if(segments.size() == 0) return;
		
		// 類似講義を見ていく
		for(String simlec : simLecs){
			// 講義のdfMapを取得
			HashMap<String, Double> sldfMap = new HashMap<String, Double>();
			try {
				FileReader fr = new FileReader(simlec + "/wordMap.txt");
				BufferedReader br = new BufferedReader(fr);
				String line;
				while((line = br.readLine()) != null){
					String strs[] = line.split(" ");
					String word = strs[0];
					Double sldf = Double.parseDouble(strs[2]);
					sldfMap.put(word, sldf);
				}
				br.close();
			}catch(Exception e){
				e.printStackTrace();
			}
			
			// 類似講義からsegArrを作成
			File simlecdir = new File(simlec);
			ArrayList<Segment> slsegments = makeSegmentArr(simlecdir, sldfMap);
			
			// 類似度計算
			try {
				calcSegSim(segments, slsegments);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	void calcSegSim(ArrayList<Segment> sbjSegs, ArrayList<Segment> objSegs) throws Exception{
		// System.out.println("類似度計算");
		String oname = objSegs.get(1).lectureName;
		for(Segment sseg : sbjSegs){
			// 結果の保存先 slide/segmentXsim.txt
			/*
			FileWriter fw = new FileWriter(sseg.slidePath + "/SegSimData/" + oname + "segment" + sseg.segmentNum + "sim.txt");
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			*/
			PrintWriter pw = getPrintWriter(sseg.slidePath + "/SegSimData", oname + "segment" + sseg.segmentNum + "sim.txt");
			
			for(Segment oseg : objSegs){
				double cosim = Cosim.calc(sseg.tfidfMap, oseg.tfidfMap);
				if(cosim > 0.0){
					pw.print(cosim + "\t");
					pw.println(oseg.slideName + "\tsegment" + oseg.segmentNum);
				}
			}
			pw.close();
		}
	}
}