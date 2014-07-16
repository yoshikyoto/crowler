import java.io.File;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;

class Main{
	
	/*
	 * OCW一覧: http://edu.k-forte.net/2013/06/ocw-japan-university/
	 * ocw.kyoto-u.ac.jp
	 * ocw.u-tokyo.ac.jp
	 * ocw.nagoya-u.jp
	 */
	public static void main(String args[]){
		String domains[] = {
			"ocw.kyoto-u.ac.jp",
			"ocw.u-tokyo.ac.jp",
			"ocw.nagoya-u.jp"
		};
		
		SlideData data = new SlideData("/Users/admin/ocwslidedata");

		// File data_dir = new File("data");
		// data_dir.mkdir();
		
		/*
		KyotoOCW k = new KyotoOCW();
		k.getCourseLists();
		k.getLectures();
		k.getPDFs();
		*/
		
		/*
		TokyoOCW t = new TokyoOCW();
		t.getCourseLists();
		t.getLectures();
		t.getPDFs();
		*/
		
		/*
		TokyoTechOCW tt = new TokyoTechOCW();
		tt.getCourseLists();
		tt.getLectures();
		tt.getPDFs();
		*/
		
		/*
		for(String domain : domains){
			SlideOCW ocw = new SlideOCW(domain);
			ocw.startCrawling("courselist");
		}
		*/
		
		// SlidePDF slide_pdf = new SlidePDF("ocw.kyoto-u.ac.jp/dip_01.pdf");
		// slide_pdf.parse();
	}
}