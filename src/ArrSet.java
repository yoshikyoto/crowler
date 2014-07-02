
class ArrSet{
	public String[] StringArr;
	public int[] intArr;
	public String[] sortString;
	public int[] sortint;

	ArrSet(String[] sarr, int[] iarr){
		StringArr = sarr;
		intArr = iarr;
	}

	public void setintArr(int[] arr){
		intArr = arr;
		StringArr = new String[intArr.length];
	}
	public void setStringArr(String[] arr){
		StringArr = arr;
		intArr = new int[StringArr.length];
	}

	public void sort(){
		sortString = new String[StringArr.length];
		sortint = new int[intArr.length];

		//freq譛�螟ｧ繧定ｪｿ縺ｹ縺ｦ縺ｿ繧�
		int max = 0;
		for(int i = 0; i < intArr.length; i++){
			if(intArr[i] > max){
				max = intArr[i];
			}
		}
		//繧ｽ繝ｼ繝医☆繧�
		int position = 0;
		for(int i = max; i > 0; i--){
			for(int j = 0; j < intArr.length; j++){
				if(intArr[j] == i){
					sortint[position] = intArr[j];
					sortString[position] = StringArr[j];
					position++;
				}
			}
		}
	}

	public void printArr(){
		for(int i = 0; i < StringArr.length; i++){
			System.out.print(StringArr[i] + "(" + intArr[i] + ")\t");
		}
	}
	public void printResult(){
		for(int i = 0; i < sortString.length; i++){
			System.out.print(sortString[i] + "(" + sortint[i] + ")\t");
		}
	}
}