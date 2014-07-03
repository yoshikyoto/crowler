import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.util.PDFTextStripper;


class SlidePDF{
	String pdfPath;
	String xmlFilePath;
	File pdfFile;
	int page;
	List<PDPage> pageList;
	
	
	SlidePDF(String path){
		pdfPath = path;
		pdfFile = new File(path);
	}
	
	SlidePDF(String path, String xml_path){
		pdfPath = path;
		pdfFile = new File(path);
		xmlFilePath = xml_path;
	}
	
	public void parse(){
		try{
			FileInputStream fis = new FileInputStream(pdfPath);
			PDFParser pdf_parser = new PDFParser(fis);
			pdf_parser.parse();
			PDDocument document = pdf_parser.getPDDocument();

			PDFTextStripper pdf_text_stripper = new PDFTextStripper();
			String text = pdf_text_stripper.getText(document);
			
			// System.out.println(text);
			
			page = document.getNumberOfPages();
			

			try{
				FileWriter fw = new FileWriter(xmlFilePath);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw);
			
				pw.println("<presentation language=\"ja\">");
				for(int i = 1; i <= page; i++){
					pw.println("<slide page=\"" + i + "\">");
					pw.println("<body>");
					pw.println("<p>");
					
					PDPage pdpage = (PDPage) document.getDocumentCatalog().getAllPages().get(i-1);
				
					pdf_text_stripper.setStartPage(i);
					pdf_text_stripper.setEndPage(i);
					String str = pdf_text_stripper.getText(document);
					
					pw.print(str);
					
					pw.println("</p>");
					pw.println("</body>");
					pw.println("</slide>");
				}
				pw.println("</presentation>");
				
				pw.close();
			}catch(Exception e){
				e.printStackTrace();
			}
				
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}