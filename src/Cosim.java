import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class Cosim{
	public static final boolean DEBUG = true;
	
	public static double calc(Map<String, Double> ma, Map<String, Double> mb){
		if(DEBUG){
			System.out.println("MapA Length: " + ma.size());
			System.out.println("MapB Length; " + mb.size());
		}
		
		Set<String> sa = ma.keySet();
		Set<String> sb = mb.keySet();
		
		double inner = 0.0;
		for(String word : sa){
			if(mb.containsKey(word)){
				inner += ma.get(word) * mb.get(word);
			}
		}
		if(DEBUG) System.out.println("内積: " + inner);
		
		// calc |ma|
		double sizea = calcSize(ma);
		double sizeb = calcSize(mb);
		
		if(DEBUG) System.out.println("SizeA: " + sizea);
		if(DEBUG) System.out.println("SizeB: " + sizeb);
		
		double cosine = inner / (sizea * sizeb);
		return cosine;
	}
	
	public static double calcSize(Map<String, Double> m){
		Set<String> s = m.keySet();
		double result_size = 0.0;
		for(String word : s){
			double value = m.get(word);
			result_size += value * value;
		}
		result_size = Math.sqrt(result_size);
		return result_size;
	}
}