import java.io.File;
import java.util.ArrayList;

public class SlideData {
	public static boolean DEBUG = true;
	File dir;
	ArrayList<OCWData> ocwDataList;
	
	SlideData(String data_path_string){
		System.out.println("Initializing SlideData: " + data_path_string);
		dir = new File(data_path_string);
		dir.mkdir();

		// ocwフォルダ取得
		ocwDataList = new ArrayList<OCWData>();
		for(File ocw_dir : dir.listFiles()){
			if(ocw_dir.getName().indexOf(".") == 0) continue;
			if(ocw_dir.getName().indexOf("wordMap.txt") == 0) continue;
			if(DEBUG) System.out.println(ocw_dir.toString());

			OCWData data = new OCWData(ocw_dir.toString());
			ocwDataList.add(data);
		}
	}
	
	public void calc(){
		
	}
}
