
class SlidePrintArr{
	// 2次元配列を標準出力へ
	public static void printArr(String[][] array){
		int m = array.length;
		for(int i = 0; i < m; i++){
			int n = array[i].length;
			for(int j = 0; j < n; j++){
				System.out.print(array[i][j] + "\t");
			}
			System.out.print("\n");
		}
	}

	// 1次元配列を標準出力へ
	public static void printArr(String[] array){
		int m = array.length;
		for(int i = 0; i < m; i++){
			System.out.print(array[i] + "\t");
		}
	}
}