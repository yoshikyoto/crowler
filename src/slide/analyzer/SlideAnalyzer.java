package slide.analyzer;

import java.sql.SQLException;

import slide.database.SlideModel;
import slide.Main.*;

public class SlideAnalyzer extends SlideMain{
	
	public static void analyze(String ocw_name, String lecture_name, String slide_name){
		Slide slide = new Slide(root + "/" + ocw_name + "/" + lecture_name + "/" + slide_name);
		slide.segmentation();
		slide.output();
		
		// all_word_countをDBに挿入
		SlideModel slide_model = new SlideModel();
		if(slide_model.exist()){
			for(Sheet sheet : slide.sheets){
				slide_model.allWordCount += sheet.allwords.size();
			}
			slide_model.segmentCount = slide.segments.size();
			try {
				slide_model.update();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				slide.logger.error(e.toString());
			}
		}
		
		slide.logger.close();
	}
}