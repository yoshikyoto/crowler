
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

class SlideGroup{
	Slide slide;
	Word[] words;
	Word[] wordset;
	int startSlide;	// 0がタイトルスライド
	int slidePage;	// 1以上
	String label;
	Map<String, Double> wordMap;

	SlideGroup(Slide s, int start, int page){
		slide = s;
		startSlide = start;
		slidePage = page;
		// words と wordset を作成
		ArrayList<Word> words_arraylist = new ArrayList<Word>();
		ArrayList<Word> wordset_arraylist = new ArrayList<Word>();

		for(int i = 0; i < page; i++){
			int target_page = start + i;
			Word[] target_words = slide.wordMatrix[target_page];
			for(int j = 0; j < target_words.length; j++){
				Word target_word = target_words[j];
				// words の方はとにかく追加
				words_arraylist.add(target_word);
				// set の方。既に入っているかどうか確認
				if(!find(wordset_arraylist, target_word)){
					wordset_arraylist.add(target_word);
				}
			}
		}

		// リストを配列に変換
		words = (Word[])words_arraylist.toArray(new Word[0]);
		wordset = (Word[])wordset_arraylist.toArray(new Word[0]);
	}

	public void newGroup(int page){
		slidePage = page;
		// words と wordset を作成
		ArrayList<Word> words_arraylist = new ArrayList<Word>();
		ArrayList<Word> wordset_arraylist = new ArrayList<Word>();

		for(int i = 0; i < page; i++){
			int target_page = startSlide + i;
			Word[] target_words = slide.wordMatrix[target_page];
			for(int j = 0; j < target_words.length; j++){
				Word target_word = target_words[j];
				// words の方はとにかく追加
				words_arraylist.add(target_word);
				// set の方。既に入っているかどうか確認
				if(!find(wordset_arraylist, target_word)){
					wordset_arraylist.add(target_word);
				}
			}
		}

		// リストを配列に変換
		words = (Word[])words_arraylist.toArray(new Word[0]);
		wordset = (Word[])wordset_arraylist.toArray(new Word[0]);
	}

	private boolean find(ArrayList<Word> arraylist, Word word){
		for(int i = 0; i < arraylist.size(); i++){
			if(arraylist.get(i).word.equals(word.word)){
				return true;
			}
		}
		return false;
	}

	private boolean find(Word[] array, Word word){
		for(int i = 0; i < array.length; i++){
			if(array[i].word.equals(word.word)){
				return true;
			}
		}
		return false;
	}

	// 語の数をカウントする
	private int count(ArrayList<Word> arraylist, Word word){
		int result = 0;
		for(int i = 0; i < arraylist.size(); i++){
			if(arraylist.get(i).word.equals(word.word)){
				result++;
			}
		}
		return result;
	}

	private int count(Word[] array, Word word){
		int result = 0;
		for(int i = 0; i < array.length; i++){
			if(array[i].word.equals(word.word)){
				result++;
			}
		}
		return result;
	}

	// 入力語が見つかったら tf を、見つからなかったら0を返す。
	public int countWord(Word word){
		for(int i = 0; i < wordset.length; i++){
			if(wordset[i].word.equals(word.word)){
				return wordset[i].tf;
			}
		}
		return 0;
	}

	// tf の再計算を行う
	public void calcTf(){
		for(int i = 0; i < wordset.length; i++){
			Word target_word = wordset[i];
			target_word.tf = count(words, target_word);
		}
	}

	// idf の計算を行う
	public void calcIdf(ArrayList<SlideGroup> groups){
		// 各語について計算
		for(int i = 0; i < wordset.length; i++){
			Word target_word = wordset[i];
			int df = 0;
			for(int j = 0; j < groups.size(); j++){
				SlideGroup group = groups.get(j);
				if(0 < group.countWord(target_word)){
					df++;
				}
			}
			target_word.idf = Math.log(groups.size() / df);
		}
	}

	// tf idf の計算
	public void calcTfIdf(){
		for(int i = 0; i < wordset.length; i++){
			wordset[i].calculateTfIdf();
		}
	}

	SlideGroup(SlideGroup group1, SlideGroup group2){
	}

	public void printWords(){
		for(int i = 0; i < words.length; i++){
			System.out.print(words[i].word + "\t");
		}
		System.out.println("");
	}

	public void printWordset(){
		for(int i = 0; i < wordset.length; i++){
			// System.out.print(wordset[i].word + "\t");
			wordset[i].print();
			System.out.print("\t");
		}
		System.out.println("");
	}

	public void printPageInfo(){
		System.out.print("{" + (startSlide + 1));
		for(int i = 1; i < slidePage; i++){
			System.out.print(", " + (startSlide + i + 1));
		}
		System.out.print("}");
	}

	public String getPageInfo(){
		String result = "{" + (startSlide + 1);
		for(int i = 1; i < slidePage; i++){
			result += (", " + (startSlide + i + 1));
		}
		result += "}";
		return result;
	}

	public void consistingLabeling(){
		//ArrayList<Word> word_arr = new ArrayList<Word>();
		System.out.print(getPageInfo());

		for(int i = 0; i < wordset.length; i++){
			Word target_word = wordset[i];
			if(target_word.lvl != -1) continue;
			target_word.print();
		}
		System.out.println();
	}

	public void linkLabeling(){
		System.out.print(getPageInfo());

		for(int i = 0; i < wordset.length; i++){
			Word target_word = wordset[i];
			if(target_word.lvl != -1) continue;

			for(int s = 0; s < startSlide; s++){
				Word other_words[] = slide.wordMatrix[s];

				for(int w = 0; w < other_words.length; w++){
					Word other_word = other_words[w];
					if(other_word.lvl == -1) continue;

					//System.out.println(target_word + "\t" + other_word);

					if(target_word.word.equals(other_word.word))
						target_word.score += 1.0;
				}
			}

			for(int s = startSlide + slidePage; s < slide.wordMatrix.length; s++){
				Word other_words[] = slide.wordMatrix[s];

				for(int w = 0; w < other_words.length; w++){
					Word other_word = other_words[w];
					if(other_word.lvl == -1) continue;

					if(target_word.word.equals(other_word.word))
						target_word.score += 1.0;
				}
			}
		}

		for(int i = 0; i < wordset.length; i++){
			Word target_word = wordset[i];
			if(target_word.lvl == -1) System.out.print(target_word.word + "(" + target_word.score + ")");
		}
		System.out.println();
	}

	public void labeling(){
		// 最終的なスコア
		System.out.print(getPageInfo());

		for(int i = 0; i < wordset.length; i++){
			Word target_word = wordset[i];
			if(target_word.lvl != -1) continue;
			target_word.score = target_word.score * target_word.tf_idf;
			System.out.print(target_word.word + "(" + target_word.score + ")");
		}
		System.out.println();
	}
	
	public void makeWordMap(){
		wordMap = new HashMap<String, Double>();
		for(Word w : wordset){
			wordMap.put(w.word, w.tf_idf);
		}
	}
}