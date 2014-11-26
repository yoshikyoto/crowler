package slide.XMLConverter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jp.dip.utakatanet.Logger;

public class SlideXMLConverter{
	public static String slidePath;
	public static void convert(String slidepath){
		slidePath = slidepath;
		System.out.println("Convert: " + slidePath);
		
		Pattern pptxp = Pattern.compile("\\.pptx$");
		Matcher pptxm = pptxp.matcher(slidePath);
		if(pptxm.find()){
			Logger.sPrintln("pptxとして処理");
			// TODO: pptxの処理
		}
		
		Pattern pdfp = Pattern.compile("\\.pdf$");
		Matcher pdfm = pdfp.matcher(slidePath);
		if(pdfm.find()){
			Logger.sPrintln("pdfとして処理");
			PdfXMLConverter.convert(slidePath);
		}
	}
}