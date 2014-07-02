

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

/*************************************************************************
 * スライド行列計算関連のクラス
 *************************************************************************/

public class SlideCalc{
	Slide slide;
	double[][] all, detail, title, horizontal, body, totitle;

	double allavg, detailavg, titleavg, horizontalavg, bodyavg, totitleavg;
	double[] allavga, detailavga, titleavga, horizontalavga, bodyavga, totitleavga;
	int[] segment;
	//int page;
	//String filepath; //xmlがあるディレクトリのパス
	int[][] edge;
	int[] outlines;
	ArrayList<SlideGroup> groups;
	PrintWriter clusteringPW;

	/****************************************************************************
	 * コンストラクタ
	 ****************************************************************************/
	SlideCalc(Slide s){
		slide = s;

		// 各行列の計算
		calMatrix();
	}

	/****************************************************************************
	 * 行列を計算する関数
	 ****************************************************************************/
	private void calMatrix(){
		try{
			File file = new File(slide.ppt_unzip_path + "\\matrix.txt");
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);

			//タイトル同士
			calTitle();
			pw.println("Title\tAvg: " + titleavg);
			printArr(title, pw);

			//全て
			calAll();
			pw.println("All\tAvg: " + allavg);
			printArr(all, pw);

			//Body
			calBody();
			pw.println("Body\tAvg: " + bodyavg);
			printArr(body, pw);

			//インデント変化なし
			calHorizontal();
			pw.println("Horizontal\tAvg: " + horizontalavg);
			printArr(horizontal, pw);

			//上位化
			calDetail();
			pw.println("Detail\tAvg: " + detailavg);
			printArr(detail, pw);

			//タイトルへの上位化
			calTotitle();
			pw.println("To Title\tAvg: " + totitleavg);
			printArr(totitle, pw);

			pw.close();
		}catch(Exception e){
			System.err.println("Exception in calMatrix: " + e);
		}
	}

	/****************************************************************************
	 * クラスタリング
	 ****************************************************************************/
	public void clustering(){
		//セグメント行列の初期化
		segment = new int[slide.page];
		System.out.println("クラスタリング開始");

		try{
			File file1 = new File(slide.ppt_unzip_path + "\\clustresult.txt");
			FileWriter fw1 = new FileWriter(file1);
			BufferedWriter bw1 = new BufferedWriter(fw1);
			PrintWriter result_pw = new PrintWriter(bw1);

			File file2 = new File(slide.ppt_unzip_path + "\\clustdetail.txt");
			FileWriter fw2 = new FileWriter(file2);
			BufferedWriter bw2 = new BufferedWriter(fw2);
			PrintWriter detail_pw = new PrintWriter(bw2);

			File cfile = new File(slide.ppt_unzip_path + "\\clustering.txt");
			FileWriter cfw = new FileWriter(cfile);
			BufferedWriter cbw = new BufferedWriter(cfw);
			clusteringPW = new PrintWriter(cbw);


			/* 目次スライドに応じたクラスタリングをする場合
			switch(findOutline(resultpw)){
			case 0:
				//outlineが見つからない場合
				resultpw.println("case M3: No outline slides");
				printArr(outlines, resultpw);
				caseM3(resultpw, detailpw);
				break;
			case 1:
				//outlineが1枚の場合
				resultpw.println("case M2: One outline slide");
				caseM3(resultpw, detailpw);
				break;
			default:
				//outlineが複数枚の場合
				resultpw.println("case M1: outline slides");
				caseM1();
				resultpw.println("Clustering from outline to outline");
				printArr(index, resultpw);
				printArr(segment, resultpw);
				break;
			}
			*/
			//conclusionも見つけたほうがいいかもしれない。

			// まずはタイトルでクラスタリング
			clusteringByTitle();
			result_pw.println("Segmentation by Title");
			printSegment(result_pw);

			/*
			for(int i = 0; i < segment.length; i++){
				System.out.println(segment[i]);
			}
			*/

			// 単純に本文でクラスタリング
			clusteringByBody(result_pw, detail_pw);

			result_pw.close();
			detail_pw.close();
			clusteringPW.close();
		}catch(Exception e){
			System.err.println("Exception in Clustering: " + e);
		}
	}

	//複数枚の目次スライドがある場合
	private void caseM1(){
		//とりあえずoutline前で切る
		for(int i = 1; i < slide.page-1; i++){
			if(outlines[i+1] == 0){
				segment[i] = 1;
			}
		}
	}

	//目次スライドが無い場合
	private void caseM3(PrintWriter resultpw, PrintWriter detailpw){
		//まずタイトルでクラスタリング
		clusteringByTitle();
		resultpw.println("Clustering by title");
		for(int i = 0; i < slide.page; i++){
			System.out.print(i+1 + "\t");
		}
		System.out.println("");
		printArr(segment, resultpw);
		printSegment(resultpw);

		//horizontalでクラスタリング
		//clustering(equByNum(horizontal), "equalized Horizontal", detailpw, resultpw);
		//resultpw.println("Clustering by equalized_Horizontal");
		//clustering(equByNum(horizontal), 0.075, "equalized Horizontal",resultpw,  detailpw);
		//clustering(equByNum(body), 0.1, "equalized Horizontal",resultpw,  detailpw);
		//printClust(resultpw);

		//このあとbodyとdetailでクラスタリングさせたいかな

		//最終的にconclusionスライドは分離したいので
		resultpw.println("Divide outline and conclusion");
		divideOutlines();
		printSegment(resultpw);

		//クラスタについていろいろ見てみる
		//何故かresultが出力されていない…？
		resultpw.println("horizontal segment");
		//calcsegment(equByNum(horizontal), resultpw, detailpw);
		//calcsegment(equByNum(detail), resultpw, detailpw);

		//分割クラスタリングもためしてみる。
		/* segment[0] = 0;
		for(int i = 1; i < page; i++){
			segment[i] = 1;
		}
		clustering(equByNum(horizontal), 1, page-1, resultpw, detailpw);

		System.out.println("分割おわり");

		segment[0] = 0;
		for(int i = 1; i < page; i++){
			segment[i] = 1;
		}
		clustering(equByNum(body), 1, page-1, resultpw, detailpw);
		*/

		System.out.print(slide.ppt_file_title + ":\t");
		//printSegment();
		/*
		int c = 0;
		for(int i = 0; i < slide.page; i++){
			if(segment[i] == 0) c++;
		}
		System.out.println(c - 1);
		 */
		detailAvg();
	}

	/****************************************************************************
	 * 配列の出力
	 ****************************************************************************/


	//2次元Int配列を標準出力
	private static void printArr(int[][] array){
		for(int i = 0; i < array.length; i++){
			for(int j = 0; j < array[i].length; j++){
				System.out.print(array[i][j] + "\t");
			}
			System.out.print("\n");
		}
	}

	//2次元Int配列を標準出力
	private static void printArr(double[][] array){
		for(int i = 0; i < array.length; i++){
			for(int j = 0; j < array[i].length; j++){
				System.out.print(array[i][j] + "\t");
			}
			System.out.print("\n");
		}
	}

	//2次元Int配列をpwに出力
	private static void printArr(double[][] array, PrintWriter pw){
		double sum;
		for(int i = 0; i < array.length; i++){
			sum = 0;
			for(int j = 0; j < array[i].length; j++){
				if(i != j){
					pw.print(array[i][j] + "\t");
					sum += array[i][j];
				}else{
					pw.print("■\t");
				}
			}
			pw.print("Slide" + (i+1) + "\tAvg:" + (sum / (array[i].length - 1)) + "\n");
		}
	}

	//2次元Int配列をpwに出力
	private static void printArr(int[][] array, PrintWriter pw){
		double sum;
		for(int i = 0; i < array.length; i++){
			sum = 0;
			for(int j = 0; j < array[i].length; j++){
				if(i != j){
					pw.print(array[i][j] + "\t");
					sum += array[i][j];
				}else{
					pw.print("■\t");
				}
			}
			pw.print("Slide" + (i+1) + "\tAvg:" + (sum / (array[i].length - 1)) + "\n");
		}
	}

	//1次元Int配列を標準出力
	private static void printArr(int[] array){
		for(int i = 0; i < array.length; i++){
			System.out.print(array[i] + "\t");
		}
		System.out.print("\n");
	}

	//1次元double配列を標準出力
	private static void printArr(double[] array){
		for(int i = 0; i < array.length; i++){
			System.out.print(array[i] + "\t");
		}
		System.out.print("\n");
	}

	//1次元int配列をpwに出力
	private static void printArr(int[] array, PrintWriter pw){
		for(int i = 0; i < array.length; i++){
			pw.print(array[i] + "\t");
		}
		pw.print("\n");
	}

	private void printSegment(PrintWriter pw){
		pw.print("{");
		for(int i = 0; i < slide.page - 1; i++){
			pw.print(i+1);
			if(segment[i] == 0){
				pw.print("} {");
			}else{
				pw.print(" ");
			}
		}
		pw.print(slide.page + "}\n");
	}

	private void printSegment(){
		System.out.print("{");
		for(int i = 0; i < slide.page - 1; i++){
			System.out.print(i+1);
			if(segment[i] == 0){
				System.out.print("} {");
			}else{
				System.out.print(" ");
			}
		}
		System.out.print(slide.page + "}\n");
	}

	private void printClust(PrintWriter pw,int seg,int segpage){
		pw.print("{");
		for(int i = seg; i < seg + segpage - 1; i++){
			pw.print((i+1) + " ");
		}
		pw.print(seg + segpage);
		pw.print("}");
	}

	/****************************************************************************
	 * 平均
	 ****************************************************************************/

	//全体の平均を求める
	private static double calAvg(double[][] array){
		double sum = 0;
		int m = array.length;
		for(int i = 1; i < m; i++){
			for(int j = 1; j < m; j++){
				if(i != j){
					sum += array[i][j];
				}
			}
		}
		return sum / ((m-1) * (m-2));
	}

	//ページiの平均を求める
	private static double calAvg(int[][] array, int i){
		double sum = 0;
		int m = array[i].length;
		for(int j = 1; j < m; j++){
			if(j != i){
				sum += array[i][j];
			}
		}
		return sum / (m - 2);
	}

	//配列を閾値で01に分ける
	private static int[][] holdArr(int[][] array, int value){
		int m = array.length;
		int[][] result = new int[m][m];
		for(int i = 0; i < m; i++){
			for(int j = 0; j < m; j++){
				if(array[i][j] >= value){
					result[i][j] = 1;
				}else{
					result[i][j] = 0;
				}
			}
		}
		return result;
	}

	/****************************************************************************
	 * 行列の計算
	 ****************************************************************************/
	//階層関係無しに語の一致を見る(all)
	private void calAll(){
		all = new double[slide.page][slide.page];
		for(int self = 0; self < slide.page; self++){
			for(int target = 0; target < slide.page; target++){
				for(int self_word = 0; self_word < slide.word_array[self].length; self_word++){
					for(int target_word = 0; target_word < slide.word_array[target].length; target_word++){
						if(slide.word_array[self][self_word].word.equals(slide.word_array[target][target_word].word)){
							all[self][target] += 1;
						}
					}
				}
			}
		}
		//allavg = calAvg(all);
	}

	//階層が上位化されているものを見る(detail)
	private void calDetail(){
		detail = new double[slide.page][slide.page];
		for(int self = 0; self < slide.page; self++){
			for(int target = 0; target < slide.page; target++){
				for(int self_word = 0; self_word < slide.word_array[self].length; self_word++){
					for(int target_word = 0; target_word < slide.word_array[target].length; target_word++){
						if((slide.word_array[self][self_word].word.equals(slide.word_array[target][target_word].word))
							&&(slide.word_array[self][self_word].lvl != -1)&&(slide.word_array[target][target_word].lvl != -1) //末節の部分を除く
							&&(slide.word_array[self][self_word].lvl > slide.word_array[target][target_word].lvl)){
								detail[self][target] += 1;
								break;
						}
					}
				}
			}
		}
		//detailavg = calAvg(detail);
	}

	//title同士を見る(title)
	private void calTitle(){
		title = new double[slide.page][slide.page];
		for(int self = 0; self < slide.page; self++){
			for(int target = 0; target < slide.page; target++){
				for(int self_word = 0; self_word < slide.word_array[self].length; self_word++){
					for(int target_word = 0; target_word < slide.word_array[target].length; target_word++){
						if(		(slide.word_array[self][self_word].lvl == -1)
								&&(slide.word_array[target][target_word].lvl == -1)
								&&(slide.word_array[self][self_word].word.equals(slide.word_array[target][target_word].word))){
							title[self][target] += 1;
						}
					}
				}
			}
		}
		//titleavg = calAvg(title);
	}

	//body同士を見る(body)
	private void calBody(){
		body = new double[slide.page][slide.page];
		for(int self = 0; self < slide.page; self++){
			for(int target = 0; target < slide.page; target++){
				for(int self_word = 0; self_word < slide.word_array[self].length; self_word++){
					for(int target_word = 0; target_word < slide.word_array[target].length; target_word++){
						// タイトルだったら break;
						if(slide.word_array[self][self_word].lvl == -1) continue;
						if(slide.word_array[target][target_word].lvl == -1) continue;
						if(slide.word_array[self][self_word].word.equals(slide.word_array[target][target_word].word)) body[self][target] += 1;
					}
				}
			}
		}
		//bodyavg = calAvg(body);
	}

	//同じ階層同士を見る(horizontal)
	private void calHorizontal(){
		horizontal = new double[slide.page][slide.page];
		for(int self = 0; self < slide.page; self++){
			for(int target = 0; target < slide.page; target++){
				if(self != target){
					for(int self_word = 0; self_word < slide.word_array[self].length; self_word++){
						for(int target_word = 0; target_word < slide.word_array[target].length; target_word++){
							if((slide.word_array[self][self_word].lvl == slide.word_array[target][target_word].lvl)
									&&(slide.word_array[self][self_word].word.equals(slide.word_array[target][target_word].word))){
								horizontal[self][target] += 1;
							}
						}
					}
				}
			}
		}
		//horizontalavg = calAvg(horizontal);
	}

	//タイトルに上位化されているものを見る
	private void calTotitle(){
		totitle = new double[slide.page][slide.page];
		for(int i = 0; i < slide.page; i++){
			for(int j = 0; j < slide.page; j++){
				for(int k = 0; k < slide.word_array[i].length; k++){
					for(int l = 0; l < slide.word_array[j].length; l++){
						if((slide.word_array[i][k].lvl != -1)&&(slide.word_array[j][l].lvl == -1)
								&&(slide.word_array[i][k].word.equals(slide.word_array[j][l].word))){
							totitle[i][j] += 1;
							break;
						}
					}
				}
			}
		}
		//totitleavg = calAvg(totitle);
	}

	/****************************************************************************
	 * スライド平均化
	 ****************************************************************************/
	//スライド各ページについて、アベレージで平均化
	private double[][] equalize(int[][] array){
		int m = array.length;
		double[][] result = new double[m][m];
		double avg;
		for(int i = 0; i < m; i++){
			avg = calAvg(array, i);
			for(int j = 0; j < m; j++){
				if(i != j){
					result[i][j] = array[i][j] / avg;
				}
			}
		}
		return result;
	}

	//ページについて単語数で平均化。
	//int同士の計算→doubleに代入　で正しくdoubleに変換されるかが微妙
	//テストしていない
	private double[][] equByNum(int[][] array){
		int m = array.length;
		double[][] result = new double[m][m];
		double n;
		for(int i = 0; i < m; i++){
			n = (double)slide.word_array[i].length;
			for(int j = 0; j < m; j++){
				if((i != j)&&(n != 0)){
					result[i][j] = array[i][j] / n;
				}else{
					result[i][j] = 0;
				}
			}
		}
		return result;
	}

	/*****************************************************************************
	 * アウトラインの情報を見つける
	 *****************************************************************************/
	private int findOutline(PrintWriter rpw){
		//まずは Outline というタイトルを探す
		int result = 0;
		outlines = new int[slide.page];
		for(int i = 0; i < slide.page; i++){
			if(slide.title_array[i].equalsIgnoreCase("outline")){
				result++;
				rpw.print(i + " ");
				outlines[i] = 1;
			}else if(slide.title_array[i].equalsIgnoreCase("conclusion")
					||slide.title_array[i].equalsIgnoreCase("conclusions"))
				outlines[i] = 2;
		}
		//ここで、detailとか使ってoutline見つけることになるのかも
		rpw.println(result);
		return result;
	}

	private void divideOutlines(){
		for(int i = 1; i < slide.page; i++){
			if(outlines[i] != 0){
				segment[i-1] = 0;
				segment[i] = 0;
			}
		}
	}

	/******************************************************************
	 * clustering関連
	 ******************************************************************/

	//title行列からクラスタリング
	private void clusteringByTitle(){
		//System.out.println("clusteringByTitle -------------------------------------------------------");
		for(int i = 1; i < (slide.page - 1); i++){
			if(slide.title_array[i].equals(slide.title_array[i+1])){
				//タイトルが一致していたらクラスタリング
				segment[i] = 2;
			}else if(title[i][i+1] >= 2){
				//2語以上一致していたらクラスタリング
				segment[i] = 1;
			}else if((i < (slide.page - 2))&&(slide.title_array[i].equals(slide.title_array[i+2]))){
				//2つ先のスライドとタイトルが一致していた場合
				segment[i] = 1;
				segment[i+1] = 1;
				i++;
			}
		}
	}

	// 本文のところのクラスタリング
	private boolean clusteringByBody(PrintWriter result_pw, PrintWriter detail_pw){
		// グループarraylist の初期化
		groups = new ArrayList<SlideGroup>();

		int begin_pointer = 1;
		int page_num = 0;
		// 最後のページに達するまでやる
		while(begin_pointer < segment.length){
			// 開始ページ番号から、ページ数を取得
			page_num = getSegmentPage(begin_pointer);
			// その数値から集合クラスを生成
			SlideGroup slide_group = new SlideGroup(slide, begin_pointer, page_num);
			slide_group.calcTf();	// tf を再計算させて
			// 配列に突っ込んでおく。
			groups.add(slide_group);
			/*
			for(int i = begin_pointer; i < begin_pointer + page_num; i++){
				Word word_array[] = slide.word_array[i];
				for(int j = 0; j < word_array.length; j++){
					Word word = word_array[j];
					if(word.lvl != -1){	// タイトル以外の単語を見る
						System.out.print(word.word + "\t");
					}
				}
				System.out.println("");
			}
			*/
			begin_pointer += page_num;
		}

		// idf を再計算させる
		for(int i = 0; i < groups.size(); i++){
			SlideGroup target_group = groups.get(i);
			target_group.calcIdf(groups);
			target_group.calcTfIdf();
		}

		// 状態を出力してみる
		for(int i = 0; i < groups.size(); i++){
			SlideGroup target_group = groups.get(i);
			// いろいろ出力してみる。
			target_group.printPageInfo();
			clusteringPW.println(target_group.getPageInfo());
			target_group.printWordset();
			target_group.printWords();
		}

		// スコア計算してみる
		while(true){
			double high_score = 0;
			int high_score_group_int = 0;
			for(int i = 0; i < groups.size() - 1; i++){
				SlideGroup group1 = groups.get(i);
				SlideGroup group2 = groups.get(i + 1);

				// group1.printPageInfo();
				// group2.printPageInfo();
				double score = calcSimilarity(group1, group2);
				// System.out.println(score);
				if(score > high_score){
					if((group1.slidePage == 1)||(group2.slidePage == 1)){
						high_score = score;
						high_score_group_int = i;
					}
				}
			}

			if(high_score == 0) break;

			System.out.println("high: " + high_score_group_int);

			// 結合させる
			SlideGroup group1 = groups.get(high_score_group_int);
			SlideGroup group2 = groups.get(high_score_group_int + 1);
			group1.newGroup(group1.slidePage + group2.slidePage);
			groups.remove(high_score_group_int + 1);

			// 状態を出力してみる
			for(int i = 0; i < groups.size(); i++){
				SlideGroup target_group = groups.get(i);
				// いろいろ出力してみる。
				target_group.printPageInfo();
				clusteringPW.print(target_group.getPageInfo());
				target_group.calcTf();
				target_group.calcIdf(groups);
				target_group.calcTfIdf();
				//target_group.printWordset();
				//target_group.printWords();
			}
			clusteringPW.println();
		}

		return false;
	}

	// 類似度的なものの計算
	private double calcSimilarity(SlideGroup group1, SlideGroup group2){
		// 各種単語配列を取ってきて、tf-idfの積をスコアとする手法
		Word[] wordset1 = group1.wordset;
		Word[] wordset2 = group2.wordset;
		double score = 0;

		for(int i = 0; i < wordset1.length; i++){
			Word word1 = wordset1[i];
			for(int j = 0; j < wordset2.length; j++){
				Word word2 = wordset2[j];
				if(word1.word.equals(word2.word)){
					score += (word1.tf_idf * word2.tf_idf);
				}
			}
		}

		score = score / (wordset1.length * wordset2.length);
		return score;
	}

	//先頭スライドを渡すと、セグメントのスライドのページ数が帰ってくる
	private int getSegmentPage(int target){
		int result = 1;
		while((target + result < segment.length)&&(segment[target + result - 1] != 0)){
			result++;
		}
		return result;
	}



	//先頭スライドを渡すと、セグメントのスライドのページ数が帰ってくる
	private static int getSegmentPage(int[] array, int target){
		int result = 1;
		while(array[target + result - 1] != 0){
			result++;
		}
		return result;
	}


	//語の出現頻度からクラスタリング 引数として行列を取る。double版（平均化版）
	private void clustering(double[][] array, String caption, PrintWriter dpw, PrintWriter rpw){
		int pivot, pivpage, target, tarpage;
		double avg;
		dpw.println(caption + "Clustering");
		int i = 1;
		//最初のセグメントを見つける
		pivot = i;
		pivpage = 1;
		while(i < slide.page){
			if(segment[i] == 0){
				i++;
				break;
			}else{
				i++;
				pivpage++;
			}
		}

		//targetを見つけていく
		target = i;
		tarpage = 1;
		double max = 0.0;
		int maxpiv = 0;
		int maxpage = 0;
		int maxtar = 0;
		while(i < slide.page){
			if(segment[i] == 0){
				avg = calcClustAvg(array, pivot, pivpage, target, tarpage);
				printsegmentAvg(pivot, pivpage, target, tarpage, avg, dpw);
				if((pivpage == 1)||(tarpage == 1)){
					//ここで、0.5の閾値をつけてみる。
					if((max < avg)&&(avg >= 0.5)){
						max = avg;
						maxpiv = pivot;
						maxtar = target;
						maxpage = pivpage + tarpage;
					}
				}
				pivot = target;
				pivpage = tarpage;
				i++;
				target = i;
				tarpage = 1;
			}else{
				i++;
				tarpage++;
			}
		}
		dpw.println("max:" + max + "\t(" + (maxpiv+1) + ", " + pivpage + ")");
		if(max > 0){
			segment[maxtar - 1] = 1;
			rpw.println(caption);
			printArr(segment, rpw);
			clustering(array, caption, dpw, rpw);
		}
	}

	//語の出現頻度からクラスタリング 引数として行列と閾値をとる
	private void clustering(double[][] array, double th, String caption, PrintWriter resultpw, PrintWriter detailpw){
		int seg1, seg1page, seg2, seg2page;
		double point;
		detailpw.println(caption + "Clustering");
		int i = 1;

		//最初のセグメントを見つける
		seg1 = i;
		seg1page = getSegmentPage(segment, seg1);


		double max = 0.0;
		int maxpiv = 0;
		int maxpage = 0;
		int maxtar = 0;

		while(seg1 + seg1page < slide.page){
			//seg2を見つける
			seg2 = seg1 + seg1page;
			seg2page = getSegmentPage(segment, seg2);

			//segmentのスコアを計算
			point = calcClustAvg(array, seg1, seg1page, seg2, seg2page);
			printsegmentAvg(seg1, seg1page, seg2, seg2page, point, detailpw);

			//ここで単独のスライドだけ見るようにしている
			if((seg1page == 1)||(seg2page == 1)){
				//閾値以上なら
				if((max < point)&&(point >= th)){
					max = point;
					maxpiv = seg1;
					maxtar = seg2;
					maxpage = seg1page + seg2page;
				}
			}
			//ここで次のsegmentへ
			seg1 = seg2;
			seg1page = seg2page;
		}
		resultpw.println("max:" + max + "\t(" + (maxpiv+1) + ", " + seg1page + ")");
		detailpw.println("");
		if(max > 0){
			segment[maxtar - 1] = 1;
			printSegment(resultpw);
			clustering(array, th, caption, resultpw, detailpw);
		}
	}

	//分割していく方
	private void clustering(double[][] array, int left, int right, PrintWriter resultpw, PrintWriter detailpw){
		System.out.println("left:" + left + " right:" + right);
		if(left == right) return;

 		double point;
		double minpoint = 100;
		int min = 0;

		System.out.println("left:" + left + " right:" + right);

		for(int i = 1; i <= right - left; i++){

			System.out.println("left:" + left + " right:" + right + " i:" + i);

			point = calcClustAvg(array, left, i, left+i, right - (left+i) + 1);
			detailpw.println("Bunkatsu clustering " + left + "-" + (left + i) + ": " + point);
			if(point < minpoint){
				minpoint = point;
				min = left + i - 1;
			}
		}
		System.out.println("left:" + left + " right:" + right + " min:" + min);
		if(min == 0) return;
		if(minpoint > 0.3) return;
		segment[min] = 0;
		resultpw.println("Min Point: " + minpoint);
		printSegment(resultpw);
		clustering(array, left, min, resultpw, detailpw);
		clustering(array, min+1, right, resultpw, detailpw);
	}

	private void detailAvg(){
		try{
			File file = new File(slide.ppt_unzip_path + "\\detailScore.txt");
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);

			printSegment(pw);
			printArr(segment, pw);

			int seg1, seg1page, seg2, seg2page;
			double sum, avg;
			seg1 = 1;
			seg1page = 1;
			while(seg1 < slide.page){
				//↓ここんところにout of boundsしないように<page みたいな条件必要かも。
				while((seg1 + seg1page - 1 < slide.page)&&(segment[seg1 + seg1page - 1] != 0)){
					seg1page++;
				}

				sum = 0;
				avg = 0;

				//seg2を探す
				seg2 = 1;
				seg2page = 1;
				while(seg2 < slide.page){
					while((seg2 + seg2page - 1 < slide.page)&&(segment[seg2 + seg2page - 1] != 0)){
						seg2page++;
					}

					if(seg1 != seg2){
						pw.print("detail score (" + (seg1+1) + ", " + seg1page + ")->(" + (seg2+1) + ", " + seg2page + ")\t");
						avg = calcClustAvg(detail, seg1, seg1page, seg2, seg2page);
						pw.println(avg);
						sum += avg;
					}

					seg2 = seg2 + seg2page;
					seg2page = 1;
				}
				pw.println("sum: " + sum + "\n");

				//pw.println(seg1 + " " + seg1page);
				seg1 = seg1 + seg1page;
				seg1page = 1;
			}
			pw.close();
		}catch(Exception e){
			System.err.println(e);
		}
	}

	private void calcsegment(double[][] array, PrintWriter resultpw, PrintWriter detailpw){
		int seg1, seg1page, seg2, seg2page;
		int i = 1;
		seg1 = 1;
		//最初のセグメントを見つける
		while(seg1 < slide.page){
			seg1page = getSegmentPage(segment, seg1);
			printClust(resultpw, seg1, seg1page);
			resultpw.println("");

			seg2 = 1;
			while(seg2 < slide.page){
				seg2page = getSegmentPage(segment, seg2);
				resultpw.print("- ");
				printClust(resultpw, seg2, seg2page);
				resultpw.print(":\t" + calcClustAvg(array, seg1, seg1page, seg2, seg2page) + "\t");
				resultpw.println("");

				seg2 = seg2 + seg2page;
			}
			resultpw.println("");
			seg1 = seg1 + seg1page;
		}
	}

	//クラスタ平均を求める
	private double calcClustAvg(int[][] array, int pivot, int pivpage, int target, int tarpage){
		double result = 0;
		for(int i = 0; i < pivpage; i++){
			for(int j = 0; j < tarpage; j++){
				result += array[pivot + i][target + j];
			}
		}
		return (result / (pivpage * tarpage));
	}

	//クラスタ平均を求める double行列版
	private double calcClustAvg(double[][] array, int pivot, int pivpage, int target, int tarpage){
		double result = 0;
		for(int i = 0; i < pivpage; i++){
			for(int j = 0; j < tarpage; j++){
				result += array[pivot + i][target + j];
			}
		}
		return (result / (pivpage * tarpage));
	}

	//2つのクラスタと値を出力
	private static void printsegmentAvg(int pivot, int pivpage, int target, int tarpage, double avg){
		pivot++;
		target++;
		System.out.print("{" + pivot);
		for(int i = 1; i < pivpage; i++){
			System.out.print(", " + (pivot + i));
		}
		System.out.print("}, {" + target);
		for(int i = 1; i < tarpage; i++){
			System.out.print(", " + (target + i));
		}
		System.out.print("}\t" + avg + "\n");
	}

	//セグメント平均をファイルに出力
	private static void printsegmentAvg(int pivot, int pivpage, int target, int tarpage, double avg, PrintWriter pw){
		pivot++;
		target++;
		pw.print("{" + pivot);
		for(int i = 1; i < pivpage; i++){
			pw.print(", " + (pivot + i));
		}
		pw.print("}, {" + target);
		for(int i = 1; i < tarpage; i++){
			pw.print(", " + (target + i));
		}
		pw.print("}\t" + avg + "\n");
	}

	 //detailとhorizontalの比をみて、クラスタリングのどの位置かに関係なく、
	 //分離してみる。 起点スライドとして記録しておく？
	/*
	private void detailDivide(PrintWriter dpw, PrintWriter rpw){
		dpw.println("Divide by detail/horizontal");
		double point;
		for(int i = 0; i < page - 1; i++){
			//i+1番目のに着目する
			if((segment[i] != 2)&&(segment[i+1] != 2)){
				//タイトルでクラスタリングされたもの以外を見る
				point = (calAvg(detail, i+1) + calAvg(totitle, i+1)*2) / calAvg(horizontal, i+1);
				dpw.println("Slide" + (i+1) + ": " + point);
				if(point > 0.45){
					segment[i] = 0;
					segment[i+1] = 0;
					dpw.println("Divide Slide" + (i+2) + "\tPoint: " + point);
					dpw.println(calAvg(detail, i+1) + ", " + calAvg(horizontal, i+1));
					rpw.println("Divide Slide" + (i+2) + "\tPoint: " + point);
					printArr(index, rpw);
					printArr(segment, rpw);

					for(int j = 1; j < page; j++){
						if(slide.title_array[i+1].equals(slide.title_array[j])){
							dpw.print(j+1 + "\t");
						}
					}
					dpw.print("\n");
				}
			}
		}
	}
	*/
	/******************************************************************
	 * ラベリング
	 ******************************************************************/

	public void labeling(){
		System.out.println("ラベリング開始");
		// まず，タイトルが一致している場合はそれでラベリング
		// 共通のタイトルも出力してみる
		//System.out.println(segment.length);
		for(int i = 0; i < segment.length; i++){
			//System.out.println(slide.title_array[i]);
			if(segment[i] == 2) System.out.println(slide.title_array[i]);
		}

		System.out.println("タイトル語のtf-idfによるスコア");
		consistingLabeling();


		System.out.println("上位化によるスコア");
		linkLabeling();

		System.out.println("合計スコア");
		for(int i = 0; i < groups.size(); i++){
			//System.out.println(i);
			SlideGroup group = groups.get(i);
			group.labeling();
		}

		System.out.println("ラベリング終了");
	}

	public void consistingLabeling(){
		//System.out.println(groups.size());
		for(int i = 0; i < groups.size(); i++){
			//System.out.println(i);
			SlideGroup group = groups.get(i);
			group.consistingLabeling();
		}
	}

	public void linkLabeling(){
		for(int i = 0; i < groups.size(); i++){
			SlideGroup target_group = groups.get(i);
			target_group.linkLabeling();
		}
	}

}