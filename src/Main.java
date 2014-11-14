import java.io.*;
import java.util.*;

import jp.dip.utakatanet.*;
import slide.XMLConverter.*;

class Main extends Base{
	
	/*
	 * OCW一覧: http://edu.k-forte.net/2013/06/ocw-japan-university/
	 * ocw.kyoto-u.ac.jp
	 * ocw.u-tokyo.ac.jp
	 * ocw.nagoya-u.jp
	 */
	public static String root = "/Users/admin/ocwslidedata";
	public static void main(String args[]){
		String domains[] = {
			"ocw.kyoto-u.ac.jp",
			"ocw.u-tokyo.ac.jp",
			"ocw.nagoya-u.jp"
		};
		
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Input number");
		switch(sc.nextInt()){
		case 0: // Crawling?
			break;
		case 1: // slide/pdf to XML
			slideToXML();
			
			break;
		case 2: // slide/pdf to Image
			break;
		}
		
		// SlideData d = new SlideData("/Users/admin/ocwslidedata");
	}
	
	public static void slideToXML(){
		Logger.setLogName("ConvertXML");
		
		for(File ocwfile : listDirs(root)){
			// 各 OCW のディレクトリについて
			Logger.sPrintln("OCW: " + ocwfile.getName());
			
			for(File lecfile : listDirs(ocwfile.getAbsolutePath())){
				// 各講義のディレクトリについて
				Logger.sPrintln("Lecture: " + lecfile.getName());
				
				for(File sfile : listDirs(lecfile.getAbsolutePath())){
					// 各スライドのファイルに対して
					Logger.sPrintln("Slide: " + sfile.getName());
					
					SlideXMLConverter.convert(sfile.getAbsolutePath());
				}
			}
		}
		Logger.sClose();
	}

		/*
		PPTFiles pf1 = new PPTFiles("/Users/admin/eclipse_workspace/Slide/testdata_kyoto-u");
		String presentation_files1[] = pf1.getPresentaitonPathArray();
		
		Slide slides1[] = new Slide[presentation_files1.length];
		for(int i = 0; i < slides1.length; i++){
			slides1[i] = new Slide(presentation_files1[i]);
		}
		
		SlideCalc sc1[] = new SlideCalc[presentation_files1.length];
		for(int i = 0; i < slides1.length; i++){
			sc1[i] = new SlideCalc(slides1[i]);
			sc1[i].clustering();
			sc1[i].labeling();
		}
		
		PPTFiles pf2 = new PPTFiles("/Users/admin/eclipse_workspace/Slide/testdata_tokyo-teck");
		String presentation_files2[] = pf2.getPresentaitonPathArray();
		
		Slide slides2[] = new Slide[presentation_files2.length];
		for(int i = 0; i < slides2.length; i++){
			slides2[i] = new Slide(presentation_files2[i]);
		}
		
		SlideCalc sc2[] = new SlideCalc[presentation_files2.length];
		for(int i = 0; i < slides2.length; i++){
			sc2[i] = new SlideCalc(slides2[i]);
			sc2[i].clustering();
			sc2[i].labeling();
		}
		
		for(SlideCalc sc : sc1)
			for(SlideGroup sg : sc.groups)
				sg.makeWordMap();
		
		for(SlideCalc sc : sc2)
			for(SlideGroup sg : sc.groups)
				sg.makeWordMap();

		try {
			FileWriter fw = new FileWriter("testdata-result.txt");
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			

			FileWriter afw = new FileWriter("testdata-highScore.txt");
			BufferedWriter abw = new BufferedWriter(afw);
			PrintWriter apw = new PrintWriter(abw);
			
			pw.print("\t");
			for(SlideCalc tsc :sc2){
				for(int t = 0; t < tsc.groups.size(); t++){
					pw.print(tsc.slide.presentationFileTitle + " s" + (t+1) + "\t");
				}
			}
			pw.println();
		
			for(SlideCalc psc : sc1){
				for(int p = 0; p < psc.groups.size(); p++){
					SlideGroup psg = psc.groups.get(p);
					String psg_name = psc.slide.presentationFileTitle + " s" + (p+1);
					pw.print(psg_name + "\t");
					
					for(SlideCalc tsc :sc2){
						for(int t = 0; t < tsc.groups.size(); t++){
							SlideGroup tsg = tsc.groups.get(t);
							String tsg_name = tsc.slide.presentationFileTitle + " s" + (t+1);
							
							double cosim = Cosim.calc(psg.wordMap, tsg.wordMap);
							pw.print(cosim + "\t");
							
							if(cosim >= 0.1){
								apw.print("- ");
								if(cosim >= 0.25){
									apw.print("☆");
									if(cosim >= 0.5){
										apw.print("☆");
									}
								}
								apw.print(" " + psg_name + psg.getPageInfo() + " - " + tsg_name + tsg.getPageInfo());
								apw.println("\t" + cosim);
							}
						}
					}
					pw.println();
				}
			}
			pw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		/*
		KyotoOCW k = new KyotoOCW();
		k.getCourseLists();
		k.getLectures();
		k.getPDFs();
		*/
		
		/*
		for(String domain : domains){
			SlideOCW ocw = new SlideOCW(domain);
			ocw.startCrawling("courselist");
		}
		*/
		
		// SlidePDF slide_pdf = new SlidePDF("ocw.kyoto-u.ac.jp/dip_01.pdf");
		// slide_pdf.parse();
}