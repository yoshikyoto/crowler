package slide.XMLConverter;

import jp.dip.utakatanet.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.pdfbox.PDFToImage;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFTextStripper;

class PdfXMLConverter{
	public static void convert(String slide_path){

		// データ保存先のディレクトリを作成
		Pattern p = Pattern.compile("(.*)\\.pdf");
		Matcher m = p.matcher(slide_path);
		m.find();
		File slide_dir = new File(m.group(1));
		Logger.sPrintln("ディレクトリを作成: " + slide_dir.getAbsolutePath());
		if(!slide_dir.mkdir()){
			// 権限が無かったりでディレクトリが作成出来なかった場合
			Logger.sPrintln("Warning: ディレクトリ作成に失敗しました。書き込み権限がないか、既にディレクトリが存在しています。");
		}
		
		// ファイルサイズ制限
		File slide_file = new File(slide_path);
		System.out.println(slide_file.length());

		if(slide_file.length() <= 700000) return;
		if(slide_file.length() > 800000){
			System.out.println("容量が大きすぎます");
			// delete(slide_dir);
			return;
		}
		

		try{
			// PDFをparseする
			FileInputStream fis = new FileInputStream(slide_path);
			PDFParser pdf_parser = new PDFParser(fis);
			pdf_parser.parse();
			PDDocument document = pdf_parser.getPDDocument();
			
			// ページ数を取得
			int page = document.getNumberOfPages();
			Logger.sPrintln("pdfページ数: " + page);
			
			// まずは画像変換しようとする
			Logger.sPrintln("imageに変換");
			// 変換
			PDPage pdpage = (PDPage) document.getDocumentCatalog().getAllPages().get(0);
			BufferedImage image = pdpage.convertToImage();
			
			// 画像のサイズを取得する
			int w = image.getWidth();
			int h = image.getHeight();
			System.out.println("width: " + w + "\theight: " + h);
			if(w < h){
				// 縦の方が長い場合
				// ディレクトリを削除
				delete(slide_dir);
				// 閉じる
				fis.close();
				pdf_parser.clearResources();
				document.close();
				// 終了
				return;
			}

			pdpage.clear();
			
			/* 画像にする部分はスキップ */
			for(int i = 0; i < page; i++){
				System.gc();
				// 変換
				pdpage = (PDPage) document.getDocumentCatalog().getAllPages().get(i);
				image = pdpage.convertToImage();
				
				// スライドっぽかったらそのまま画像を出す．
				File image_file = new File(slide_dir + "/" + i + ".png");
				Boolean result = ImageIO.write(image, "png", image_file);
				Logger.sPrintln("Page " + i + " 変換結果: " + result);
			}

			PDFTextStripper pdf_text_stripper = new PDFTextStripper();
			// String text = pdf_text_stripper.getText(document);
			
			String xml_path = slide_dir + "/slide.xml";
			Logger.sPrintln("xml出力先: " + xml_path);
			FileWriter fw = new FileWriter(xml_path);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
		
			// FIXME: とりあえず何も考えずに ja
			pw.println("<presentation language=\"ja\">");
			for(int i = 1; i <= page; i++){
				pw.println("<slide page=\"" + i + "\">");
				pw.println("<body>");
				pw.println("<p>");
				
				// PDPage pdpage = (PDPage) document.getDocumentCatalog().getAllPages().get(i-1);
			
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
			
			fis.close();
			document.close();
			pdf_parser.clearResources(); // TODO: このリソース解放でいい？
			
		}catch(Exception e){
			Logger.sErrorln(slide_path);
			Logger.sErrorln("Error: " + e);
			Logger.sClose();
		}
	}
	
    private static void delete(File f){
    	System.out.println("ディレクトリを削除");
         // ファイルまたはディレクトリが存在しない場合は何もしない
        if(f.exists() == false) {
        	System.out.println("ファイルが存在しない");
            return;
        }

        if(f.isFile()) {
            // ファイルの場合は削除する
            f.delete();

        } else if(f.isDirectory()){
            // ディレクトリの場合は、すべてのファイルを削除する

            // 対象ディレクトリ内のファイルおよびディレクトリの一覧を取得
            File[] files = f.listFiles();

            // ファイルおよびディレクトリをすべて削除
            for(int i=0; i<files.length; i++) {
                // 自身をコールし、再帰的に削除する
                delete( files[i] );
            }
            
            // 自ディレクトリを削除する
            f.delete();
        }
    }
}