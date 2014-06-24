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
			"ocw.u-tokyo.ac.jp",
			"ocw.nagoya-u.jp",
			"ocw.kyoto-u.ac.jp",
		};
		
		for(String domain : domains){
			SlideOCW ocw = new SlideOCW(domain);
			ocw.startCrawling();
		}
		
		SlidePDF slide_pdf = new SlidePDF("ocw.kyoto-u.ac.jp/dip_01.pdf");
		slide_pdf.parse();
	}
}