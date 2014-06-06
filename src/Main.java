class Main{
	
	/*
	 * Kyoto University: ocw.kyoto-u.ac.jp
	 * University of Tokyo: ocw.u-tokyo.ac.jp
	 */
	public static void main(String args[]){
		String domains[] = {
			"ocw.kyoto-u.ac.jp"
		};
		
		for(String domain : domains){
			SlideOCW ocw = new SlideOCW(domain);
			ocw.startCrawling();
		}
	}
}