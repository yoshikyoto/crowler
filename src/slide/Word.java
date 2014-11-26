package slide;

class Word{
	String word;
	int lvl;
	int tf;
	int df;
	double idf;
	double tf_idf;
	public double score; // いろんなスコア付けに使う

	Word(){
	}

	Word(String w, int l){
		word = w;
		lvl = l;
		df = 0;
		idf = 1;
		score = 1;
	}

	public void calculateTfIdf(){
		double tf_d = tf;
		double idf_d = idf;
		tf_idf = tf_d * idf_d;
	}

	public void print(){
		System.out.print(word + "(" + lvl + ", " + tf + ", " + idf + "(" + df + "), " + tf_idf + ")");
	}
}