import java.io.File;
import java.io.FileInputStream;

import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;


class SlidePDF{
	String pdfPath;
	File pdfFile;
	
	SlidePDF(String path){
		pdfPath = path;
		pdfFile = new File(path);
	}
	
	public void parse(){
		try{
			FileInputStream fis = new FileInputStream(pdfPath);
			PDFParser pdf_parser = new PDFParser(fis);
			pdf_parser.parse();
			PDDocument document = pdf_parser.getPDDocument();

			PDFTextStripper pdf_text_stripper = new PDFTextStripper();
			String text = pdf_text_stripper.getText(document);
			
			System.out.println(text);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}