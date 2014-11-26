package slide;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Cosim{
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
	
	public static HashMap<String, Double> calcTfidf(HashMap<String, Integer> tfMap, HashMap<String, Integer> dfMap){
		HashMap<String, Double> tfidfMap = new HashMap<String, Double>();
		for(String word : tfMap.keySet()){
			int tf = tfMap.get(word);
			int df = dfMap.get(word);
			double tfidf = (double)tf / (double)df;
			tfidfMap.put(word, tfidf);
		}
		return tfidfMap;
	}
	
	public static HashMap<String, Integer> mergeMap(HashMap<String, Integer> a, HashMap<String, Integer> b){
		for(String word : b.keySet()){
			if(a.containsKey(word)){
				int tf = a.get(word);
				tf += b.get(word);
				a.put(word, tf);
			}else{
				a.put(word, b.get(word));
			}
		}
		return a;
	}
}