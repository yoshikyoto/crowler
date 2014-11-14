package slide.analyzer;

import jp.dip.utakatanet.*;
import slide.*;

import java.util.ArrayList;
import java.util.HashMap;

class Segment{
	Logger logger;
	ArrayList<Sheet> sheets;
	HashMap<String, Integer> tfMap;
	
	Segment(Sheet sheet, Logger l){
		logger = l;
		sheets = new ArrayList<Sheet>();
		sheets.add(sheet);
		
		tfMap = sheet.tfMap;
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
}