package slide.SegmentSim;

import jp.dip.utakatanet.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SegSimCalculator extends Base{
	public static String datapath = "/Users/admin/ocwslidedata";
	public static void calc(){
		
		Logger.setLogName("SegmentSim");
		// File file = new File(datapath + "/ocw.kyoto-u.ac.jp/06-コンパイラ");
		// SegSim s = new SegSim(file);
		// s.calcSegSim();
		// Logger.sClose();
		
		// ocw一覧取得
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
		Logger.sClose();
	}
	
	/*
	public static void calc(File file){
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			
			String line = "";
			while((line = br.readLine()) != null){
				String strs[] = line.split("\t");
				File tfile = new File(strs[2]);
				System.out.println(line + ": " + tfile.exists());
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
	}
	*/
}