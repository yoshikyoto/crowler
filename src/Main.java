import java.util.ArrayDeque;
import java.util.Map;
import java.util.HashMap;
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

		Map<String, Double> ma = new HashMap<String, Double>();
		Map<String, Double> mb = new HashMap<String, Double>();
		Map<String, Double> mc = new HashMap<String, Double>();
		
		ma.put("hoge", 1.0);
		ma.put("huga", 2.5);
		ma.put("foo", 1.0);
		
		mb.put("hoge", 2.0);
		mb.put("huga", 5.0);
		mb.put("foo", 2.0);
		
		mc.put("foo", 1.2);
		mc.put("bar", 2.3);

		System.out.println(Cosim.calc(ma, mb));
		System.out.println(Cosim.calc(ma, mc));
		
		
		/*
		KyotoOCW k = new KyotoOCW();
		k.getCourseLists();
		k.getLectures();
		k.getPDFs();
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