package slide.converter;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

import jp.dip.utakatanet.Base;

import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;

import slide.database.LectureModel;
import slide.database.SlideModel;

import com.sun.pdfview.decode.PDFDecoder;

public class Pdf2Image extends Base{
	public static void convert(SlideModel slide_model) throws Exception{
		p("デコーダーを準備");
		PdfDecoder pdf_decoder = new PdfDecoder(true);
		
		// PDFを開く(ここでpdfExceptionが出ることがある)
		p("pdfを開く");
		pdf_decoder.openPdfFile(slide_model.getDirName() + ".pdf");
		
		for(int i = 0; i < slide_model.page; i++){
			// 倍率とページ
			pdf_decoder.setPageParameters(1.0f, i+1);
			
			p("画像を取得");
			BufferedImage bi = pdf_decoder.getPageAsImage(i+1);
			
			p("画像を保存 " + slide_model.getDirName() + "/" + i + ".png");
			FileOutputStream os = new FileOutputStream(slide_model.getDirName() + "/" + i + ".png");
			ImageIO.write(bi, "PNG", os);
		}
	}
}
