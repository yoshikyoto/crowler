
import java.io.*;

import java.util.*;

//形態素解析器kuromoji
//import org.atilika.kuromoji.*;

//zip関係
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.*;

/*
//Document,XML関連
import org.w3c.dom.*;
import org.xml.sax.*;

//XPath関連
import javax.xml.xpath.*;
import javax.xml.parsers.*;

//MaCabを使うライブラリ
import slothLib.SlothLibException;
import slothLib.NLP.*;
*/


public class SlideStructure {

	/*
	public static void old_main(String args[]){
		//"C:\\Users\\Sakamoto\\pptData\\VLBD2011_Outline";

		//入力されたpathフォルダ内にあるファイルを全て取得
		PPTFiles ppt_files = new PPTFiles("C:\\Users\\Sakamoto\\pptData\\VLBD2011_Outline");
		// ppt配列を取得
		String[] ppt_files_arr = ppt_files.getPPTPathArray();


		SlideEn[] slides = new SlideEn[ppt_files_arr.length];
		//リスト内のスライドを処理
		for(int i = 0; i < ppt_files_arr.length; i++){
			slides[i] = new SlideEn(ppt_files_arr[i]);
		}

		// 以下はスライドセグメンテーションのためのコード
		/*
		SlideCalcEn[] slidec = new SlideCalcEn[pptnum];
		for(int i = 0; i < pptnum; i++){
			slidec[i] = new SlideCalcEn(slides[i]);
		}
		TitlewordEn titles = new TitlewordEn(slides);
		*/
	//}

	public static void printArr(String[][] wordarr, int[][] lvlarr){
		int i, j, m, n;

		//スライドのページ数を取得
		m = wordarr.length;

		for(i = 0; i < m; i++){
			n = wordarr[i].length;
			System.out.print("Slide" + (i+1) + ":\t");
			for(j = 0; j < n; j++){
				System.out.print(wordarr[i][j] + "(" + lvlarr[i][j] + ")\t");
			}
			System.out.print("\n");
		}
	}

	//二次元配列を出力する関数
	public static void printArr(String[][] array){
		int i = 0;
		int j = 0;
		int m = array.length;
		int n;
		for(i = 0; i < m; i++){
			n = array[i].length;
			for(j = 0; j < n; j++){
				System.out.print(array[i][j] + "\t");
			}
			System.out.print("\n");
		}
	}

	public static void printArr(int[][] array){
		int i = 0;
		int j = 0;
		int m = array.length;
		int n;
		for(i = 0; i < m; i++){
			n = array[i].length;
			for(j = 0; j < n; j++){
				if(array[i][j] != -1){
					System.out.print(array[i][j] + "\t");
				}else{
					System.out.print("x\t");
				}
			}
			System.out.print("\n");
		}
	}

	//一次元配列を出力する
	public static void printArr(String[] array){
		int i = 0;
		int m = array.length;
		for(i = 0; i < m; i++){
			System.out.print(array[i] + "\t");
		}
	}

	public static void printArr(int[] array){
		int m = array.length;
		for(int i = 0; i < m; i++){
			System.out.print(array[i] + "\t");
		}
		System.out.print("\n");
	}
}


