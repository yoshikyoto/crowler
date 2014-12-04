package slide.XMLConverter;

import jp.dip.utakatanet.*;
import slide.database.*;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.util.PDFTextStripper;

class PdfXMLConverter{
	public int page, imageCount = 0;
	public long byteSize;
	public File slide_dir;
	public static long minByteSize = 1000000;
	public static long maxByteSize = 1500000;
	public boolean convert(String slide_path){

		// データ保存先のディレクトリを作成
		Pattern p = Pattern.compile("(.*)\\.pdf");
		Matcher m = p.matcher(slide_path);
		m.find();
		slide_dir = new File(m.group(1));
		Logger.sPrintln("ディレクトリを作成: " + slide_dir.getAbsolutePath());
		if(!slide_dir.mkdir()){
			// 権限が無かったりでディレクトリが作成出来なかった場合
			Logger.sPrintln("Warning: ディレクトリ作成に失敗しました。書き込み権限がないか、既にディレクトリが存在しています。");
		}
		
		// ファイルサイズの制限とか設ける場合
		File slide_file = new File(slide_path);
		byteSize = slide_file.length();
		System.out.println(byteSize);

		if(byteSize < minByteSize){
			System.out.println("指定した容量より小さいためスキップされました");
			// delete(slide_dir);
			return false;
		}
		
		if(byteSize > maxByteSize){
			Logger.sPrintln("容量が大きいためスキップされました");
			// delete(slide_dir);
			return false;
		}

		try{
			// PDFをparseする
			FileInputStream fis = new FileInputStream(slide_path);
			PDFParser pdf_parser = new PDFParser(fis);
			pdf_parser.parse();
			PDDocument document = pdf_parser.getPDDocument();
			
			// ページ数を取得
			page = document.getNumberOfPages();
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
				Logger.sPrintln("縦横比からスライドでないと判断されました");
				delete(slide_dir);
				// 閉じる
				fis.close();
				pdf_parser.clearResources();
				document.close();
				// 終了
				return false;
			}

			pdpage.clear();
			
			// 画像にする部分
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
			
			List<PDPage> list = document.getDocumentCatalog().getAllPages();
		
			// FIXME: とりあえず何も考えずに ja
			pw.println("<presentation language=\"ja\">");
			for(int i = 1; i <= page; i++){
				pw.println("<slide page=\"" + i + "\">");
				pw.println("<body>");
				pw.println("<p>");
				

				pdf_text_stripper.setStartPage(i);
				pdf_text_stripper.setEndPage(i);
				String str = pdf_text_stripper.getText(document);
				pw.print(str);
				
				pw.println("</p>");
				
				// PDPage page = (PDPage) document.getDocumentCatalog().getAllPages().get(i-1);
				PDPage page = list.get(i-1);
				PDResources resources = page.getResources();
				/*
				 * getImages() では画像を取ってこれないパターンがあるようで、
				 * getXObjects() で変えてくる Library から ImageObject 的なものを取ってくるのが確実な模様。
				 */
				Map images = resources.getImages();
				//System.out.println("画像: " + images.size());
				// そのスライドに画像が含まれているかどうかだけ見ておく
				if(images.size() > 0){
					pw.println("<img count=\"" + images.size() + "\" />");
					imageCount++;
				}
				
				pw.println("</body>");
				pw.println("</slide>");
			}
			pw.println("</presentation>");
			pw.close();
			
			fis.close();
			document.close();
			pdf_parser.clearResources(); // TODO: このリソース解放でいい？
			
			return true;
			
		}catch(Exception e){
			Logger.sErrorln(slide_path);
			Logger.sErrorln("Error: " + e);
			Logger.sClose();
			return false;
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