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
		slide_model.name = slide_name;
		slide_model.ocw = ocw_name;
		slide_model.lectureName = lecture_name;
		p("スライドを探す: " + ocw_name + " " + lecture_name + " " + slide_name);
		if(slide_model.exist()){
			p("スライドが見つかったので値を更新");
			for(Sheet sheet : slide.sheets){
				p("シートのワードをカウント");
				slide_model.allWordCount += sheet.allwords.size();
			}
			p(slide_model.allWordCount);
			
			slide_model.segmentCount = slide.segments.size();
			p("セグメントの数: " + slide_model.segmentCount);
			try {
				slide_model.update();
				p("アップデート成功");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				slide.logger.error(e.toString());
			}
		}
		slide_model.close();
		
		slide.logger.close();
	}
}