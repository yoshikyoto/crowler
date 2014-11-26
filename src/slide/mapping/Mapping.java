package slide.mapping;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import jp.dip.utakatanet.*;
import slide.lectureSim.*;

public class Mapping extends Base{

	public static String root = "/Users/admin/ocwslidedata";
	public static void calc(){
		Logger.setLogName("Mapping");

		for(File ocwfile : listDirs(root)){
			if(ocwfile.getName().indexOf("kyoto") == -1) continue;
			for(File lecfile : listDirs(ocwfile.getAbsolutePath())){
				p(lecfile.getName());
				if(lecfile.getName().indexOf("コンパイラ") == -1) continue;
				try {
					calc(lecfile.getAbsolutePath());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logger.sError(e.toString());
					e.printStackTrace();
				}
			}
		}
		Logger.sClose();
	}
	
	/**
	 * 
	 * @param lecdir
	 * @throws Exception
	 */
	public static void calc(String lecdir) throws Exception{
		// 類似講義を幾つか取得
		ArrayList<Lecture> sim_lec_arr = LectureSim.getSimilarLectures(lecdir);

		for(Lecture sim_lec : sim_lec_arr){
			// p(lec.name);
			// 各類似講義について見ていく
			for(File slide_file : listDirs(lecdir)){
				HashMap<String, double[][]> matrixMap = new HashMap<String, double[][]>(); // 類似講義 - segSimMatrix の Map
				int segcount = getSegmentCount(slide_file); // 基ファイルのセグメント数
				Logger.sPrintln("Slide: " + slide_file.getName());
				// 講義に含まれる各スライドについて見ていく
				// 結果を記録するための HashMap Keyは講義スライド名、Valueは類似セグメントの対応
				HashMap<String, String> simap = new HashMap<String, String>();
				
				// FIXME: 単純に類似度が高いものを対応セグメントとする
				int i = 0;
				while(true){
					File sim_info_file = new File(slide_file.getAbsolutePath() + "/SegSimData/" + sim_lec.name + "segment" + i + "sim.txt");
					//p(sim_info_file.getName());
					if(!sim_info_file.exists()) break;
					// 類似データファイルの各行をパースする
					BufferedReader br = getFileBr(sim_info_file.getAbsolutePath());
					// 類似データファイルの書式:
					// 類似度(tab)スライドファイル名(tab)segmentXX
				
					
					double max = 0.0;
					String line, sim_slide_name = "null", sim_segment = "null";
					while((line = br.readLine()) != null){

						String s[] = line.split("\t");
						double point = Double.parseDouble(s[0]);
						sim_slide_name = s[1]; // 類似講義のスライドの名前
						sim_segment = s[2]; // その中のセグメント
						if(sim_segment.indexOf("segment") >= 0)
							sim_segment = sim_segment.substring(7);
						int sim_segment_num = Integer.parseInt(sim_segment);
						
						String sim_slide_absolute_path = sim_lec.dir.getAbsolutePath() + "/" + sim_slide_name;
						if(!matrixMap.containsKey(sim_slide_absolute_path)){
							// まだmapにmatrixが作られていない場合
							int sim_lec_segcount = getSegmentCount(new File(sim_slide_absolute_path));
							double[][] sim_matrix = new double[segcount][sim_lec_segcount];
							matrixMap.put(sim_slide_absolute_path, sim_matrix);
						}
						
						double[][] sim_matrix = matrixMap.get(sim_slide_absolute_path);
						try{
							sim_matrix[i][sim_segment_num] = point;
						}catch(Exception e){
							p(sim_slide_absolute_path + " " + i + ", " + sim_segment_num);
							e.printStackTrace();
						}
						
						if(max < point){
							max = point;
						}
					}
					// これで類似セグメントが求まる筈。
					Logger.sPrintln(slide_file.getName() + "のsegment" + i + " の類似セグメント: " + sim_slide_name + " " + sim_segment);
					// セグメント i が sname の segment に対応
					if(!sim_segment.equals("null")){
						if(!simap.containsKey(sim_slide_name)) simap.put(sim_slide_name, "");
						simap.put(sim_slide_name, simap.get(sim_slide_name) + i + "," + sim_segment + " ");
					}
					i++;
				}
				// 結果
				PrintWriter pw = getPrintWriter(slide_file.getAbsolutePath(), "simmap" + sim_lec.name + ".txt");
				pw.println(sim_lec.dir.getAbsolutePath());
				for(String sname : simap.keySet()){
					pw.println(sname + "\t" + simap.get(sname));
				}
				pw.close();
				
				for(String sim_slide_absolute_path : matrixMap.keySet()){
					double[][] matrix = matrixMap.get(sim_slide_absolute_path);
					File sim_lec_file = new File(sim_slide_absolute_path);
					printSimMatrix(slide_file + "/map", sim_lec_file.getName(), matrix, sim_slide_absolute_path);
					makeMapping(matrix, slide_file.getAbsolutePath(), sim_slide_absolute_path);
				}
			}
		}
	}
	
	public static void makeMapping(double[][] matrix, String slide_path, String sim_slide_path){
		p("makeMapping");
		File slide_file = new File(slide_path);
		File sim_slide_file = new File(sim_slide_path);
		p(slide_file.getAbsolutePath() + "/simmap" + sim_slide_file.getName());
		
		//PrintWriter pw = getPrintWriter(slide_file.getAbsolutePath() + "/simmap");
		
		double[] avg = new double[matrix[0].length];
		for(int i = 0; i < matrix[0].length; i++){
			for(int j = 0; j < matrix.length; j++){
				avg[i] += matrix[j][i];
			}
			avg[i] /= matrix[0].length;
			// avg[i] *= 1.2; // 閾値になる
		}
		
		for(int i = 0; i < avg.length; i++)
			p(avg[i] + " ");
		
		String map_str = "";
		for(int i = 0; i < matrix.length; i++){
			// i:基スライドセグメント番号
			for(int k = 0; k < 3; k++){
				int max_sim_seg = -1;
				double max_score = 0.0;
				for(int j = 0; j < matrix[i].length; j++){
					if(max_score < matrix[i][j]){
						max_score = matrix[i][j];
						max_sim_seg = j;
					}
				}
				
				p("max_score" + max_score);
				if(max_score == 0.0) break;

				p("avg max:" + max_score + "\tavg:" + avg[max_sim_seg]);
				// 向こうからみた類似度も見る
				if(max_score > avg[max_sim_seg]){
					map_str += i + "," + max_sim_seg;
				}else{
					matrix[i][max_sim_seg] = 0.0;
				}
			}
		}
		
		p(map_str);
	}
	
	public static void printSimMatrix(String dirname, String filename, double[][] matrix, String header) throws IOException{
		PrintWriter pw = getPrintWriter(dirname, filename);
		pw.println(header);
		for(int i = 0; i < matrix.length; i++){
			for(int j = 0; j < matrix[i].length - 1; j++){
				pw.print(matrix[i][j] + " ");
			}
			pw.println(matrix[i][matrix[i].length - 1]);
		}
		pw.close();
	}
	
	public static int getSegmentCount(File sfile) throws IOException{
		String str = getLineFromFile(sfile.getAbsolutePath() + "/segment.txt");
		String strs[] = str.split(" ");
		int count = 0;
		for(String s : strs){
			if(s.equals("0")) count++;
		}
		return count;
	}
}