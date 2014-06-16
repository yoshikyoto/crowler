import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.util.PDFTextStripper;


class SlidePDF{
	String pdfPath;
	File pdfFile;
	int page;
	List<PDPage> pageList;
	
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
			
			page = document.getNumberOfPages();
			pageList = document.getDocumentCatalog().getAllPages();
			
			for(int i = 0; i < page; i++){
				PDPage target_page = pageList.get(i);
				PDResources resources = target_page.getResources();
				
			}
				
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}