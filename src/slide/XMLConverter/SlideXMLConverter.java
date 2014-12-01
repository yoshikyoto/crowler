package slide.XMLConverter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jp.dip.utakatanet.Logger;

public class SlideXMLConverter{
	public static String slidePath;
	public boolean isSucceeded = false;
	public int page, imageCount;
	public long byteSize;
	public boolean convert(String slidepath){
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
			PdfXMLConverter converter = new PdfXMLConverter();
			
			// 処理に成功した場合
			if(converter.convert(slidePath)){
				// pageとか取ってくる処理
				page = converter.page;
				byteSize = converter.byteSize;
				imageCount = converter.imageCount;
				isSucceeded = true;
				return true;
			}
		}
		return false;
	}
}