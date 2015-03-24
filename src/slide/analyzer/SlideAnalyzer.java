package slide.analyzer;

import java.sql.SQLException;

import slide.database.SlideModel;
import slide.Main.*;

public class SlideAnalyzer extends SlideMain{
	
	public static void analyze(SlideModel slide_model/*String ocw_name, String lecture_name, String slide_name*/){
		Slide slide = new Slide(root + "/" + slide_model.ocw + "/" + slide_model.lectureName + "/" + slide_model.name);
		slide.segmentation();
		slide.output();
		
		// all_word_countをDBに挿入
		// p("スライドを探す: " + ocw_name + " " + lecture_name + " " + slide_name);
		if(slide_model.exist()){
			// slide_model.query();
			//p("スライドが見つかったので値を更新");
			//p(slide_model.imageCount + " " + slide_model.allWordCount);
			// sleep(1000);
			int all_word_count = 0, noun_count = 0;
			for(Sheet sheet : slide.sheets){
				//p("シートのワードをカウント: " + sheet.allwords.size());
				all_word_count += sheet.allwords.size();
				noun_count += sheet.words.size();
			}
			slide_model.allWordCount = all_word_count;
			slide_model.nounCount = noun_count;
			//p(slide_model.allWordCount);
			
			// TODO: 仮にここでページ数もアップデートするけど、本当はここじゃないほうがいい
			slide_model.page = slide.sheets.size();
			System.out.println(slide_model.page);
			
			slide_model.segmentCount = slide.segments.size();
			//p("セグメントの数: " + slide_model.segmentCount);
			try {
				slide_model.update();
				//p("アップデート成功");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				slide.logger.error(e.toString());
			}
		}
		
		slide.logger.close();
	}
}