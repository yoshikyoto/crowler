package slide.analyzer;

import jp.dip.utakatanet.*;
import slide.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Slide{
	File xmlFile, dir;
	String xml;
	String filename;
	Logger logger, segLog;
	ArrayList<Sheet> sheets;
	ArrayList<Segment> segments;
	HashMap<String, Integer> tfMap, dfMap;
	
	public Slide(String slide_dir_str){
		// スライドファイルの名前を取得してログを吐くように
		Pattern p = Pattern.compile("([^/]+?)$");
		Matcher m = p.matcher(slide_dir_str);
		m.find();
		filename = m.group(1);
		logger = new Logger(filename);
		logger.println("filename: " + filename);
		
		// xmlファイルの場所
		xmlFile = new File(slide_dir_str + "/slide.xml");
		logger.println("xmlFile: " + xmlFile.getAbsolutePath());
		
		// スライドのディレクトリの場所
		dir = new File(slide_dir_str);
		logger.println("dir: " + dir.getAbsolutePath());
		
		try{
			// XMLから
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(xmlFile),"UTF-8"));
			// FileReader fr = new FileReader();
			// BufferedReader br = new BufferedReader(fr);
			
			xml = "";
			String line = "";
			while((line = br.readLine()) != null) xml += line;
			
			logger.println("XML:");
			logger.println(xml);
		}catch(Exception e){
			System.err.println(e);
			Logger.sErrorln("XML読み取り時のエラー" + e);
			return;
		}
		
		// slideタグを見つけていく
		sheets = new ArrayList<Sheet>();
		tfMap = new HashMap<String, Integer>();
		p = Pattern.compile("<slide page=\"([0-9]+)\">.*?</slide>");
		m = p.matcher(xml);
		//System.out.println(xml);
		while(m.find()){
			//System.out.println("ページ: " + m.group(1) + " -----");
			//System.out.println(m.group());
			logger.println(m.group());
			Sheet sheet = new Sheet(m.group(), Integer.parseInt(m.group(1)), logger, dir);
			sheets.add(sheet);
			tfMap = Cosim.mergeMap(tfMap, sheet.tfMap);
		}
	}

	String output_file_name = "";
	public static double tfidf_border = 0.07;
	public static double t_border = 0.01;
	public void segmentation(){
		String method_name = "";
		segLog = new Logger(filename + "_segmentation");
		// 一つのスライドで一つのセグメントとする
		segments = new ArrayList<Segment>();
		for(Sheet sheet : sheets){
			Segment segment = new Segment(sheet, logger);
			segments.add(segment);
		}
		// TODO: 同じタイトルでセグメンテーションするようにすべき
		

		// wwordでセグメンテーション
		output_file_name += "t" + t_border + "_";
		while(true){
			// まずDF計算
			calcWDf();
			for(String word : dfMap.keySet()){
				segLog.println(word + "(" + dfMap.get(word) + ")");
			}
			
			// セグメントをログに出力
			for(Segment segment : segments){
				segLog.print("{ " + segment.getString() + "} ");
			}
			segLog.println("");
			
			// 隣同士のセグメント見る
			double max_cosim = 0.0;
			int segment_num = 0;
			for(int i = 1; i < segments.size() - 1; i++){
				Segment segmentA = segments.get(i);
				Segment segmentB = segments.get(i+1);
				
				// TODO: どちらかの長さは1でないといけない
				if(segmentA.sheets.size() != 1 && segmentB.sheets.size() != 1) continue;
				HashMap<String, Double> mapA, mapB;
				mapA = Cosim.calcTfLogIdf(segmentA.wwordTfMap, dfMap, segments.size());
				mapB = Cosim.calcTfLogIdf(segmentB.wwordTfMap, dfMap, segments.size());
				// コサイン類似度計算
				double cosim = Cosim.calc(mapA, mapB);
				segLog.println("Segment{ " + segmentA.getString() + "}, Segment{ " + segmentB.getString() + "}");
				segLog.println("コサイン類似度: " + cosim);
				if(max_cosim < cosim){
					max_cosim = cosim;
					segment_num = i;
				}
			}
			// マージする条件
			if(max_cosim > t_border){
				Segment segmentA = segments.get(segment_num);
				Segment segmentB = segments.get(segment_num + 1);
				segmentA.merge(segmentB);
				segments.remove(segment_num + 1);
			}else{
				break;
			}
		}
		
		// セグメンテーションしていく
		output_file_name += "tfidf" + tfidf_border + "_";
		while(true){
			// まずDF計算
			calcDf();
			for(String word : dfMap.keySet()){
				segLog.println(word + "(" + dfMap.get(word) + ")");
			}
			
			// セグメントをログに出力
			for(Segment segment : segments){
				segLog.print("{ " + segment.getString() + "} ");
			}
			segLog.println("");
			
			// 隣同士のセグメント見る
			double max_cosim = 0.0;
			int segment_num = 0;
			for(int i = 1; i < segments.size() - 1; i++){
				Segment segmentA = segments.get(i);
				Segment segmentB = segments.get(i+1);
				
				// TODO: どちらかの長さは1でないといけない
				if(segmentA.sheets.size() != 1 && segmentB.sheets.size() != 1) continue;
				HashMap<String, Double> mapA, mapB;
				mapA = Cosim.calcTfLogIdf(segmentA.tfMap, dfMap, segments.size());
				mapB = Cosim.calcTfLogIdf(segmentB.tfMap, dfMap, segments.size());
				// コサイン類似度計算
				double cosim = Cosim.calc(mapA, mapB);
				segLog.println("Segment{ " + segmentA.getString() + "}, Segment{ " + segmentB.getString() + "}");
				segLog.println("コサイン類似度: " + cosim);
				if(max_cosim < cosim){
					max_cosim = cosim;
					segment_num = i;
				}
			}
			// マージする条件
			// System.out.println("max_cosim: " + max_cosim);
			if(max_cosim > tfidf_border){
				Segment segmentA = segments.get(segment_num);
				Segment segmentB = segments.get(segment_num + 1);
				segmentA.merge(segmentB);
				segments.remove(segment_num + 1);
			}else{
				break;
			}
		}
		
		// 遠くのセグメント比較
		output_file_name += "skip_";
		while(true){
			// まずDF計算
			for(String word : dfMap.keySet()){
				segLog.println(word + "(" + dfMap.get(word) + ")");
			}
			
			// セグメントをログに出力
			for(Segment segment : segments){
				segLog.print("{ " + segment.getString() + "} ");
			}
			segLog.println("");
			
			// 隣同士のセグメント見る
			double max_cosim = 0.0;
			int segment_num = 0;
			for(int i = 2; i < segments.size() - 1; i++){
				Segment segmentC = segments.get(i);
				if(segmentC.sheets.size() != 1) continue;
				segLog.print("前後のスライドを見る");
				Segment segmentA = segments.get(i-1);
				Segment segmentB = segments.get(i+1);
				
				// TODO: どちらかの長さは1でないといけない
				// if(segmentC.sheets.size() != 1 && segmentB.sheets.size() != 1) continue;
				HashMap<String, Double> mapA, mapB;
				mapA = Cosim.calcTfLogIdf(segmentA.tfMap, dfMap, segments.size());
				mapB = Cosim.calcTfLogIdf(segmentB.tfMap, dfMap, segments.size());

				//mapA = Cosim.calcTfLogIdf(segmentA.wwordTfMap, dfMap, segments.size());
				//mapB = Cosim.calcTfLogIdf(segmentB.wwordTfMap, dfMap, segments.size());
				// コサイン類似度計算
				double cosim = Cosim.calc(mapA, mapB);
				segLog.println("Segment{ " + segmentC.getString() + "}, Segment{ " + segmentB.getString() + "}");
				segLog.println("コサイン類似度: " + cosim);
				if(max_cosim < cosim){
					max_cosim = cosim;
					segment_num = i;
				}
			}
			// マージする条件 TODO: マージのしきい値
			if(max_cosim > tfidf_border){
				Segment segmentA = segments.get(segment_num - 1);
				Segment segmentC = segments.get(segment_num);
				Segment segmentB = segments.get(segment_num + 1);
				segmentA.merge(segmentC);
				segments.remove(segment_num);
				segmentA.merge(segmentB);
				segments.remove(segment_num);
			}else{
				break;
			}
		}
		
		
		
		output_file_name += "segment.txt";
		segLog.close();
	}
	
	public void calcDf(){
		dfMap = new HashMap<String, Integer>();
		// segment DF
		for(Segment segment : segments){
			for(String word : segment.tfMap.keySet()){
				if(dfMap.containsKey(word)){
					int df = dfMap.get(word);
					dfMap.put(word, df + 1);
				}else{
					dfMap.put(word, 1);
				}
			}
		}
	}
	
	public void calcWDf(){
		dfMap = new HashMap<String, Integer>();
		// segment DF
		for(Segment segment : segments){
			for(String word : segment.wwordTfMap.keySet()){
				if(dfMap.containsKey(word)){
					int df = dfMap.get(word);
					dfMap.put(word, df + 1);
				}else{
					dfMap.put(word, 1);
				}
			}
		}
	}
	
	public void output(){
		// スライドのtfとdfを出力
		try{
			File file = new File(dir + "/wordMap.txt");
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			for(String word : tfMap.keySet())
				pw.println(word + " " + tfMap.get(word) + " " + dfMap.get(word));
			pw.close();
		}catch(Exception e){
			logger.errorln("wordMap 出力時IのOError: " + e);
		}
		
		// セグメント情報を出力
		try{
			System.out.println(output_file_name);
			File file = new File(dir + "/" + output_file_name);
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			for(Segment segment : segments){
				pw.print(segment.getString());
				pw.print("0 ");
			}
			pw.close();
		}catch(Exception e){
			logger.errorln("セグメント情報出力時のIOError: " + e);
		}
		
		// 各セグメントのtfを出力
		for(int i = 0; i < segments.size(); i++){
			try{
				File file = new File(dir + "/segment" + i + ".txt");
				FileWriter fw = new FileWriter(file);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw);
				
				Segment segment = segments.get(i);
				for(String word : segment.tfMap.keySet())
					pw.println(word + " " + segment.tfMap.get(word));
				
				pw.close();
			}catch(Exception e){
				e.printStackTrace();
				logger.errorln("セグメントtf出力時のIOError: " + e);
			}
		}
	}
}