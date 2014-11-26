package slide.analyzer;

public class SlideAnalyzer{
	
	public static void analyze(String slide_dir){
		Slide slide = new Slide(slide_dir);
		slide.segmentation();
		slide.output();
		slide.logger.close();
	}
}