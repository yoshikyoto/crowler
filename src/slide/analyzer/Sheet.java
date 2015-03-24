package slide.analyzer;

import jp.dip.utakatanet.*;
import slide.database.*;
import slide.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;
import org.atilika.kuromoji.Tokenizer.Builder;
import org.atilika.kuromoji.Tokenizer.Mode;

public class Sheet{
	String xml;
	File slideDir;
	int page;
	Logger logger;
	String title, body;
	ArrayList<String> words, allwords;
	CountHashMap<String> words_2gram;
	HashMap<String, Integer> tfMap;
	
	Sheet(String str, int pg, Logger l, File sd){
		xml = str;
		logger = l;
		page = pg;
		slideDir = sd;
		logger.println("in Sheet Class, page: " + page);
		
		// タイトルを見つける
		Pattern p = Pattern.compile("<title>(.*?)</title>");
		Matcher m = p.matcher(xml);
		title = "";
		if(m.find()) title = m.group(1);
		logger.println("title = \"" + title + "\"");
		
		// 本文を見つける
		p = Pattern.compile("<body>(.*?)</body>");
		m = p.matcher(xml);
		body = "";
		if(m.find()) body = m.group(1);
		logger.println("body = \"" + body + "\"");
		
		makeWordArrayJa();
	}
	public void makeWordArrayJa(){
		words = new ArrayList<String>();
		words_2gram = new CountHashMap<String>();
		allwords = new ArrayList<String>();
		tfMap = new HashMap<String, Integer>();
		// FIXME: lvlの概念が存在していると解析できない
		logger.println("makeWordArrayJp");
		Pattern p = Pattern.compile("<p.*?>(.*?)</p>");
		Matcher m = p.matcher(body);
		
		// System.out.println("page:" + page);
		while(m.find()){
			String paragraph = m.group(1);
			logger.println("paragraph: " + paragraph);
			
			// パラグラフを形態素解析にかける
			Builder builder = Tokenizer.builder();
			builder.mode(Mode.SEARCH);
			Tokenizer tokenizer = builder.build();
			List<Token> tokens = tokenizer.tokenize(paragraph);
			
			String prev_word = null;
			// 各トークンを見ていく
			for(Token token : tokens){
				// 名詞以外はスルー
				//System.out.println(token.getAllFeatures());
				if(!token.getAllFeaturesArray()[0].equals("名詞")){
					allwords.add(" ");
					prev_word = null;
					continue; 
				}
				String surface = token.getSurfaceForm();
				// FIXME: 一文字はスルー
				if(surface.length() == 1) continue;
				// 数字はリジェクトしてしまう
				Pattern intp = Pattern.compile("^[0-9]+$");
				Matcher intm = intp.matcher(surface);
				if(intm.find()) continue;
				
				words.add(surface);
				allwords.add(surface);
				if(prev_word != null){
					// 2-gram;
					String word_2gram = prev_word + surface;
					// System.out.println("2-gram: " + word_2gram);
					if(WikipediaModel.hasPage(word_2gram)){
						// System.out.println("2-gram: 追加した");
						words_2gram.add(prev_word + surface);
					}
				}
				
				prev_word = surface;
				
				// TFを+1
				int tf = 1;
				if(tfMap.containsKey(surface)) tf += tfMap.get(surface);
				tfMap.put(surface, tf);

				logger.println(surface + " :\t" + token.getAllFeatures() + "\t" + token.isKnown() + "\t" + tf);
			}
		}
		
		try {
			FileWriter fw = new FileWriter(slideDir.getAbsolutePath() + "/sheet" + page + "Morp.txt");
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			for(String word : allwords){
				pw.println(word);
			}
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// tfMapを出力する
		try {
			File f = new File(slideDir.getAbsolutePath() + "sheet" + page + "wordMap.txt");
			f.delete();
			
			FileWriter fw = new FileWriter(slideDir.getAbsolutePath() + "/sheet" + page + "wordMap.txt");
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			
			Set<String> words = tfMap.keySet();
			for(String word : words){
				int tf = tfMap.get(word);
				pw.println(word + " " + tf);
			}
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		// wikipedia tfMapを出力する
		try {
			File f = new File(slideDir.getAbsolutePath() + "sheet" + page + "wword.txt");
			f.delete();
			
			FileWriter fw = new FileWriter(slideDir.getAbsolutePath() + "/sheet" + page + "wword.txt");
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			
			int cnt = 0;
			// 2-gram
			for(String word : words_2gram.keySet()){
				pw.println(word + " " + words_2gram.get(word));
				System.out.println("2-gram " + word + " " + words_2gram.get(word));
				cnt++;
			}
			
			// 1-gram
			/*
				for(String word : tfMap.keySet()){
					if(WikipediaModel.hasPage(word)){
						pw.println(word + " " + tfMap.get(word));
					}
				}
				*/
			
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}