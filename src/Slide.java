
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;

class Slide extends SlidePrintArr{
	int page = 0;
	String language;

	Word[][] word_array;
	Word[][] wordArrayWithoutDupulication;

	String[] title_array;
	String[] body_array;

	String ppt_file_title;	//pptファイルの名前(.pptxは含まない)
	String ppt_unzip_path;	//解凍したディレクトリのパス
	File ppt_file;
	File xml_file;

	//単語配列を出力するメソッド
	public void printArr(){
		int i, j, n;
		for(i = 0; i < page; i++){
			n = word_array[i].length;
			System.out.print("Slide" + (i+1) + ":\t");
			for(j = 0; j < n; j++){
				System.out.print(word_array[i][j].word + "(" + word_array[i][j].lvl + ")\t");
			}
			System.out.print("\n");
		}
	}

	public void outputWordToTxt(){
		try{
			File file = new File(ppt_unzip_path + "/word.txt");
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);

			for(int i = 0; i < page; i++){
				int n = word_array[i].length;
				for(int j = 0; j < n; j++){
					pw.print(word_array[i][j].word + "(" + word_array[i][j].lvl + ", "
							+ word_array[i][j].tf_idf + ")\t");
				}
				pw.print("\n");
			}
			pw.close();
		}catch(Exception e){
			System.out.println("Exception in printArrToResult()\n" + e);
		}
	}

	/*******************************************************************************
	 * Slide -> XML関係
	 *******************************************************************************/

	Slide(String input_path){
		System.out.println("SlideEn: " + input_path + " が入力されました．");

		// 各種変数の初期化
		initFiles(input_path);

		try{
			decompXls();
		}catch(Exception e){
			System.err.println("pptx展開時のエラー: " + e);
		}

		//続いてSlide Open XMLの解析をして，slide.xml の出力
		try{
			outputSlideXML();
		}catch(Exception e){
			System.err.println("slide.xml出力時のエラー: " + e);
		}

		// 単語の配列を生成
		makeArr();

		// TF/IDFスコアの計算
		calculateTfIdf();

		// word.txt を出力
		outputWordToTxt();
	}

	private void initFiles(String input_path){
		//ppt_file_path = input_path;
		ppt_file = new File(input_path);

		System.out.println("pptx Path: " + ppt_file.getAbsolutePath());
		System.out.println("pptx FileName: " + ppt_file.getName());

		// .pptxを除いたファイルタイトルを取得
		Pattern regex_pattern = Pattern.compile("(.*)(\\.pptx)");
		Matcher regex_matcher = regex_pattern.matcher(ppt_file.getName());
		regex_matcher.find();
		ppt_file_title = regex_matcher.group(1);
		System.out.println("pptx Title: " + ppt_file_title);

		System.out.println("Parent Path: " + ppt_file.getParent());

		// zip解凍先
		ppt_unzip_path = ppt_file.getParent() + "/" + ppt_file_title;
		System.out.println("zip解凍先ディレクトリ: " + ppt_unzip_path);

		xml_file = new File(ppt_unzip_path + "/slide.xml");
		System.out.println("XML File Path: " + xml_file);
	}


	//zip解凍処理
	private int decompXls() throws Exception{

		File zip_file = new File(ppt_file.getParent() + "/" + ppt_file_title + ".zip");
		System.out.println("pptxファイルをrenameしました:" + zip_file.getAbsolutePath());
		ppt_file.renameTo(zip_file);

		ZipInputStream in;
		BufferedOutputStream out;

		//zipを開く
		in = new ZipInputStream(new BufferedInputStream(new FileInputStream(zip_file.getAbsolutePath())));

		ZipEntry zip_entry;
		int data = 0;

		//ZIPの終端に達するまでループ
		while((zip_entry = in.getNextEntry()) != null){
			//ディレクトリを作成
			String decPath = ppt_file.getParent() + "/" + ppt_file_title + "/" + zip_entry.getName();
			new File(decPath).getParentFile().mkdirs();

			out = new BufferedOutputStream(new FileOutputStream(decPath));

			//データを読んで出力
			while((data = in.read()) != -1){
				out.write(data);
			}
			//ZIPエントリを閉じる
			in.closeEntry();
			out.close();
			out = null;
		}
		//zipを閉じる
		in.close();

		//pptxにrename
		zip_file.renameTo(ppt_file);
		return 0;
	}


	public void outputSlideXML() throws Exception{
		//xml_file にテキストを書き込む
		FileWriter fw = new FileWriter(xml_file);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);

		// 言語判定 slide2.xml の，コンテンツプレースホルダーを見る
		FileReader fr = new FileReader(ppt_unzip_path + "/ppt/slides/slide2.xml");
		BufferedReader br = new BufferedReader(fr);

		String slide2_xml_str = new String();
		String tmp;
		while((tmp = br.readLine()) != null){
			slide2_xml_str += tmp;
		}
		if(slide2_xml_str.indexOf("コンテンツ プレースホルダ") != -1){
			language = "ja";
		}else{
			language = "en";
		}
		pw.println("<presentation language=\""+ language + "\">");

		// プレゼンテーション全体のタグ

		int i = 1;
		//ここで一枚一枚ページを見ていく
		while(outputSlidePage(i, pw)){
			i++;
		}

		//ここでページが初期化される．
		System.out.println(ppt_file_title + "\tスライド枚数: " + (i - 1) + "枚");
		page = i - 1;

		pw.println("</presentation>");
		pw.close();
	}


	private boolean outputSlidePage(int pagenum, PrintWriter pw){
		try{
			//xmlを読む
			FileReader fr = new FileReader(ppt_unzip_path + "/ppt/slides/slide" + pagenum + ".xml");
			BufferedReader br = new BufferedReader(fr);


			//改行を全部削除
			String xml_str = new String();
			String tmp;
			while((tmp = br.readLine()) != null){
				xml_str += tmp;
			}

			pw.println("<slide page=\"" + pagenum + "\">");

			// titleとbodyの内容をページ単位で取得
			String title = "";
			String body = "";

			// 正規表現で，<p:sp>か<a:tbl>を抽出
			Pattern pattern = Pattern.compile("(<p:sp.*?>.+?</p:sp>|<a:tbl.*?>.+?</a:tbl>)");
			Matcher matcher = pattern.matcher(xml_str);
			while(matcher.find()){
				String[] para_result_arr = outputSlidePara(matcher.group());

				// タイトルの内容かどうか判断して処理
				if(para_result_arr[0].equals("title")){
					title += para_result_arr[1];
				}else if(para_result_arr[0].equals("body")){
					body += para_result_arr[1];
				}
			}


			pw.println("<title>" + title + "</title>");
			pw.println("<body>");
			pw.print(body);
			pw.println("</body>");

			pw.println("</slide>");

			return true;
		}catch(Exception e){
			System.out.println("スライドの末尾まできました");
			return false;
		}
	}

	//<p:sp>の中を解析する関数
	//[1]テキスト
	//[2]titleかbodyか
	//ここでコンテンツの種類を見る
	private static String[] outputSlidePara(String str){
		// System.out.print("p:sp 処理");
		String[] result = {"", ""};
		//そのコンテンツの種類を特定する
		String contenttype = tagParRet(str, 0, "p:ph", "type");
		String contentname = tagParRet(str, 0, "p:cNvPr", "name");

		// System.out.println("コンテンツ" + contenttype);

		if(contenttype.equals("subTitle")){
			result[0] = "body";
			result[1] = paragraphRet(str);
		}else if(contenttype.equals("ctrTitle")||contenttype.equals("title")){
			//ctrは1枚目のタイトル、titleは2枚目以降のタイトル
			result[0] = "title";
			result[1] = textRet(str);
		}else if((contentname.indexOf("コンテンツ プレースホルダ") != -1)
				||(contentname.indexOf("Content Placeholder") != -1)
				||(contentname.indexOf("Inhaltsplatzhalter") != -1)){
			result[0] = "body";
			result[1] = cphRet(str);
		}else if((contentname.indexOf("テキスト ボックス") != -1)||(contentname.indexOf("TextBox") != -1)){
			result[0] = "body";
			result[1] = paragraphRet(str);
		}else{
			//表とかはここで処理されるかな？
			result[0] = "body";
			result[1] = paragraphRet(str);
		}

		if(result[1].equals("")){
			result[0] = "";
		}
		return result;
	}

	//コンテンツプレースホルダーの中を見る
	private static String cphRet(String str){
		String resulttext = "";
		String targetp, rettext;
		String lvl;

		int[] tagindex = tagIndexRet(str, 0, "a:p");

		while(tagindex[0] != -1){
			targetp = str.substring(tagindex[0], tagindex[3]);
			rettext = textRet(targetp);

			//抽出するテキストが見つかった場合だけタグを付加
			if(rettext.equals("")){
			}else{
				lvl = tagParRet(targetp, 0, "a:pPr", "lvl");
				if(lvl.equals("")){
					resulttext += "<p lvl=\"0\">";
					resulttext += rettext;
					resulttext += "</p>\n";
				}else{
					resulttext += "<p lvl=\"" + lvl + "\">";
					resulttext += rettext;
					resulttext += "</p>\n";
				}
			}
			tagindex = tagIndexRet(str, tagindex[3], "a:p");
		}
		return resulttext;
	}

	//<a:p>を見つけては，その中身をtextRetに渡す
	//その前後に<p>タグを付加する
	private static String paragraphRet(String str){
		String resulttext = "";
		String targetp, rettext;

		int[] tagindex = tagIndexRet(str, 0, "a:p");

		while(tagindex[0] != -1){
			targetp = str.substring(tagindex[0], tagindex[3]);

			rettext = textRet(targetp);
			if(rettext.equals("")){
			}else{
				resulttext += "<p>";
				resulttext += rettext;
				resulttext += "</p>\n";
			}
			tagindex = tagIndexRet(str, tagindex[3], "a:p");
		}
		return resulttext;
	}


	//<a:t>の中身全てを結合して返す
	private static String textRet(String str){
		String resulttext = "";

		int[] tagindex = tagIndexRet(str, 0, "a:t");

		while(tagindex[0] != -1){
			resulttext += str.substring(tagindex[1], tagindex[2]);

			//値更新
			tagindex = tagIndexRet(str, tagindex[3], "a:t");
		}
		return resulttext;
	}

	/******************************************************************************
	 * XML -> 単語配列　関連
	 ******************************************************************************/
	//xmlを読み取って名詞を抽出した2次元配列をつくる．
	//引数はスライドのページ数とxmlがあるフォルダのパス
	private void makeArr(){

		//スライドの単語配列を初期化
		title_array = new String[page];
		word_array = new Word[page][];
		wordArrayWithoutDupulication = new Word[page][];
		//lvl_array = new int[page][];
		//lvl_string_arr = new String[page][];

		try{
			FileReader fr = new FileReader(xml_file);
			BufferedReader br = new BufferedReader(fr);

			File file = new File(ppt_unzip_path + "/keitaiso.txt");
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);


			String str = new String();
			String tmp = new String();
			while((tmp = br.readLine()) != null){
				str += tmp;
			}
			System.out.println(str);
			System.out.println(page);

			Pattern regex_pattern = Pattern.compile("<slide page=\"([0-9]+)\">.*?</slide>");
			Matcher regex_matcher = regex_pattern.matcher(str);
			while(regex_matcher.find()){
				System.out.println(regex_matcher.group());
				System.out.println(regex_matcher.group(1));
				slideWordRet(regex_matcher.group(), Integer.parseInt(regex_matcher.group(1)), pw);
			}
			/*
			String aslidestr = "";
			str = br.readLine();
			while((str = br.readLine()) != null){
				while(str.indexOf("</slide>") == -1){
					//スライドタグの終わりまで読み，その内容をaslidestrに入れる
					aslidestr += (str + "\n");
					str = br.readLine();
				}
				aslidestr = aslidestr + str;
				pw.println(targetpage + 1);
				slideWordRet(aslidestr, targetpage, pw); //テキスト抽出して単語解析
				targetpage++;
				aslidestr = "";
			}
			*/

			//最後に，lvlarrにstringlvlarrを整数化して整理したものを代入
			//orderlvlArr();

			//結果をresult.txtに出力
		}catch(Exception e){
			System.out.println("in makeWordArr " + e);
		}
	}


	private void slideWordRet(String str, int targetpage, PrintWriter pw){
		//形態素解析準備
		ArrayList<Word> word_array_list = new ArrayList<Word>();
		//ArrayList<String> lvl_array_list = new ArrayList<String>();

		//タイトルの単語抽出
		Pattern regex_pattern = Pattern.compile("<title>(.*?)</title");
		Matcher regex_matcher = regex_pattern.matcher(str);
		if(regex_matcher.find()){
			String title_text = regex_matcher.group(1);
			title_array[targetpage-1] = title_text;
			if(language.equals("en")){
				word_array_list.addAll(enTextToWordArrayList(title_text, -1));
			}else if(language.equals("ja")){
				word_array_list.addAll(jaTextToWordArrayList(title_text, -1));
			}
		}else{
			title_array[targetpage-1] = "";
		}

		regex_pattern = Pattern.compile("<body>(.*?)</body>");
		regex_matcher = regex_pattern.matcher(str);
		regex_matcher.find();
		String body_str =  regex_matcher.group();

		// インデントあり paragraph
		regex_pattern = Pattern.compile("<p lvl=\"([0-9]+)\">(.*?)</p>");
		regex_matcher = regex_pattern.matcher(body_str);
		while(regex_matcher.find()){
			System.out.println(regex_matcher.group(1) + " " + regex_matcher.group(2));

			ArrayList<Word> paragraph_word_list = new ArrayList<Word>();
			if(language.equals("en")){
				String text = regex_matcher.group(2);
				int lvl = Integer.parseInt(regex_matcher.group(1));
				paragraph_word_list = enTextToWordArrayList(text, lvl);
			}else if(language.equals("ja")){
				String text = regex_matcher.group(2);
				int lvl = Integer.parseInt(regex_matcher.group(1));
				paragraph_word_list = jaTextToWordArrayList(text, lvl);
			}
			word_array_list.addAll(paragraph_word_list);
		}

		// インデントなし paragraph
		regex_pattern = Pattern.compile("<p>(.*?)</p>");
		regex_matcher = regex_pattern.matcher(body_str);
		while(regex_matcher.find()){
			System.out.println("-1 " + regex_matcher.group(1));
			ArrayList<Word> paragraph_word_list = new ArrayList<Word>();
			if(language.equals("en")){
				String text = regex_matcher.group(1);
				paragraph_word_list = enTextToWordArrayList(text, -2);
			}else if(language.equals("ja")){
				String text = regex_matcher.group(1);
				paragraph_word_list = jaTextToWordArrayList(text, -2);
			}
			word_array_list.addAll(paragraph_word_list);
		}

		//arrayListを変数に代入
		word_array[targetpage-1] = (Word[])word_array_list.toArray(new Word[0]);
	}

	//文を渡すと、ステミングとストップワード処理をしてArrayListを返す。
	private ArrayList<Word> enTextToWordArrayList(String str, int lvl){
		String strLower = str.toLowerCase();
		ArrayList<Word> result = new ArrayList<Word>();
		char c;
		Stemmer s = new Stemmer();

		String stopwords[] =
			{"a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't", "as", "at",
				"be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can't", "cannot", "could", "couldn't",
				"did", "didn't", "do", "does", "doesn't", "doing", "don't", "down", "during", "each", "few", "for", "from", "further",
				"had", "hadn't", "has", "hasn't", "have", "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's",
				"i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself",
				"no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours", "	ourselves", "out", "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some", "such",
				"than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too",
				"under", "until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were", "weren't", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom", "why", "why's", "with", "won't", "would", "wouldn't",
				"you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves"};

		for(int i = 0; i < str.length(); i++){
			c = strLower.charAt(i);
			if((c >= 'a')&&(c <= 'z')&&(i < str.length() - 1)){
				s.add(c);
			}else{
				s.stem();
				if(Arrays.asList(stopwords).contains(s.toString())){
				}else if(s.getResultLength() >= 3){
					result.add(new Word(s.toString(), lvl));
				}else if(s.getResultLength() == 2){
					c = str.charAt(i - 1);
					if((c >= 'A')&&(c <= 'Z')){
						result.add(new Word(s.toString(), lvl));
					}
				}
				s = new Stemmer();
			}
		}
		return result;
	}


	private ArrayList<Word> jaTextToWordArrayList(String str, int lvl){
		ArrayList<Word> result = new ArrayList<Word>();

		// 形態素解析
		Tokenizer tokenizer = Tokenizer.builder().build();
		List<Token> tokens = tokenizer.tokenize(str);

		String stopwords[] =
			{"/", "†", "・"};

		// 名刺を取り出す
		for(Token token : tokens){
			if(token.getAllFeaturesArray()[0].equals("名詞")){ //名詞だけ抽出して配列を返す．
				String word_surface = token.getSurfaceForm();
				if(Arrays.asList(stopwords).contains(word_surface)) continue; // ストップワード処理
				// 数字をリジェクト
				Pattern regex_pattern = Pattern.compile("[0-9]+");
				Matcher regex_matcher = regex_pattern.matcher(word_surface);
				if(regex_matcher.find()) continue;

				// 1文字のはリジェクト？
				if(word_surface.length() == 1) continue;

				result.add(new Word(word_surface, lvl));
			}
		}
		return result;
	}

	/*************************************************************************
	 * 共通のtagRetrieval関連
	 *************************************************************************/
	//パラメータの値を返す．開始と終了タグ一体のものにも対応．パラメータが無い場合は""を返す．
	private static String tagParRet(String str, int base, String tagname, String parname){
		int tagstart, parstart, parend, rpar;
		String result = "";

		int parlength = parname.length();

		//開始タグを求める tagstart = タグ開始位置
		int tagstart1, tagstart2;
		tagstart = -1;

		tagstart1 = str.indexOf("<" + tagname + ">", base);
		tagstart2 = str.indexOf("<" + tagname + " ", base);
		if(tagstart1 < tagstart2){
			if(tagstart1 != -1){
				tagstart = tagstart1;
			}else{
				tagstart = tagstart2;
			}
		}else{
			if(tagstart2 != -1){
				tagstart = tagstart2;
			}else{
				tagstart = tagstart1;
			}
		}

		parstart = str.indexOf(parname + "=\"", tagstart); // ="
		parend = str.indexOf("\"", parstart + parlength + 2); // "
		rpar = str.indexOf(">", tagstart); // >

		if((tagstart > -1)&&(rpar > -1)&&(parstart > -1)&&(parend > -1)&&(parend < rpar)){
			result = str.substring(parstart + parlength + 2, parend);
		}
		return result;
	}

	/*
	 * タグのインデックスを求める
	 * [0] タグの始まり
	 * [1] タグ内のテキストの始まり
	 * [2] テキストの終わり
	 * [3] タグの終わり
	 * 複数のタグの名前に対応したものも欲しいかもしれない。
	 */
	private static int[] tagIndexRet(String str, int base, String tagname){
		int tagstart, rpar, tagend;
		int[] result = new int[4];

		int tagstart1, tagstart2;
		tagstart = -1;

		tagstart1 = str.indexOf("<" + tagname + ">", base);
		tagstart2 = str.indexOf("<" + tagname + " ", base);
		if(tagstart1 < tagstart2){
			if(tagstart1 != -1){
				tagstart = tagstart1;
			}else{
				tagstart = tagstart2;
			}
		}else{
			if(tagstart2 != -1){
				tagstart = tagstart2;
			}else{
				tagstart = tagstart1;
			}
		}

		rpar = str.indexOf(">", tagstart);
		tagend = str.indexOf("</" + tagname + ">", rpar);

		int taglength = tagname.length();

		result[0] = tagstart;
		result[1] = rpar + 1;
		result[2] = tagend;
		result[3] = tagend + taglength + 3;

		//最終的に，見つからないパーツがあったら，result[0] = -1とする
		if((rpar == -1)||(tagend == -1)){
			result[0] = -1;
		}
		return result;
	}

	/*************************************************************************
	 * TF/IDF
	 *************************************************************************/
	private void calculateTfIdf(){
		// TF の計算
		for(int slide_num = 0; slide_num < word_array.length; slide_num++){
			for(int self_word_num = 0; self_word_num < word_array[slide_num].length; self_word_num++){
				for(int target_word_num = 0; target_word_num < word_array[slide_num].length; target_word_num++){
					if(word_array[slide_num][self_word_num].word.equals(word_array[slide_num][target_word_num].word)){
						word_array[slide_num][self_word_num].tf++;
					}
				}
			}
		}

		// IDF の計算
		for(int self_slide_num = 0; self_slide_num < word_array.length; self_slide_num++){
			for(int self_word_num = 0; self_word_num < word_array[self_slide_num].length; self_word_num++){
				for(int target_slide_num = 0; target_slide_num < word_array.length; target_slide_num++){
					if(self_slide_num == target_slide_num) continue;
					for(int target_word_num = 0; target_word_num < word_array[target_slide_num].length; target_word_num++){
						if(word_array[self_slide_num][self_word_num].word.equals(word_array[target_slide_num][target_word_num].word)){
							word_array[self_slide_num][self_word_num].idf++;
						}
					}
				}
			}
		}

		// TF/IDF の計算
		for(int slide_num = 0; slide_num < word_array.length; slide_num++){
			for(int word_num = 0; word_num < word_array[slide_num].length; word_num++){
				word_array[slide_num][word_num].calculateTfIdf();
			}
		}
		
		// 重複を除去
		for(int page = 0; page < word_array.length; page++){
			ArrayList<Word> word_array_list = new ArrayList<Word>();
			for(int wordnum = 0; wordnum < word_array[page].length; wordnum++){
				if(isNotContain(word_array[page][wordnum], word_array_list))
					word_array_list.add(word_array[page][wordnum]);
			}
			wordArrayWithoutDupulication[page] = (Word[])word_array_list.toArray(new Word[0]);
		}
	}
	// Array にすでに Word が入っているかどうかを確認するメソッド
	public boolean isNotContain(Word word, ArrayList<Word> array){
		for(int i = 0; i < array.size(); i++){
			if(array.get(i).word.equals(word.word)){
				return false;
			}
		}
		return true;
	}
}