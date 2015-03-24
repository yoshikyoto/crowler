package slide.analyzer;

import jp.dip.utakatanet.*;
import slide.*;

import java.util.ArrayList;
import java.util.HashMap;

class Segment{
	Logger logger;
	ArrayList<Sheet> sheets;
	HashMap<String, Integer> tfMap;
	CountHashMap<String> wwordTfMap;
	
	Segment(Sheet sheet, Logger l){
		logger = l;
		sheets = new ArrayList<Sheet>();
		sheets.add(sheet);
		
		tfMap = sheet.tfMap;
		wwordTfMap = sheet.words_2gram;
	}
	
	public void merge(Segment segment){
		// tfMapをマージ
		for(String word : segment.tfMap.keySet()){
			if(tfMap.containsKey(word)){
				// tf を更新
				int tf = tfMap.get(word);
				tf += segment.tfMap.get(word);
				tfMap.put(word, tf);
			}else{
				// tfを追加
				int tf = segment.tfMap.get(word);
				tfMap.put(word, tf);
			}
		}
		
		
		// 2-gram をマージ
		for(String word : segment.wwordTfMap.keySet()){
			if(wwordTfMap.containsKey(word)){
				// tf を更新
				int tf = wwordTfMap.get(word);
				tf += segment.wwordTfMap.get(word);
				wwordTfMap.put(word, tf);
			}else{
				// tfを追加
				int tf = segment.wwordTfMap.get(word);
				wwordTfMap.put(word, tf);
			}
		}
		
		// Sheetをマージ
		for(Sheet sheet : segment.sheets){
			sheets.add(sheet);
		}
	}
	
	public String getString(){
		String str = "";
		for(Sheet sheet : sheets){
			str += sheet.page + " ";
		}
		return str;
	}
	
	/*
	 * for segmentSim
	 */
}