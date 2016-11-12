package com.converter.pdfConverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.cli.*;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

import com.converter.utils.FileUtils;

public class OpenOfficePDFConverter implements PDFConverter{
	
	private static  OfficeManager officeManager;
	private static String OFFICE_HOME = "/Applications/OpenOffice.app/Contents";
	private static int port[] = {8100};


    private static final Option OPTION_OUTPUT_FILE = new Option("o", "output-file", true, "output file, 输出文档, 非必填");
    private static final Option OPTION_INPUT_FILE = new Option("i", "input-file", true, "input file, 必填");
    private static final Option OPTION_OFFICE_HOME = new Option("oh", "office-home", true, "office home, office安装路径, 必填");
    private static final Options OPTIONS = initOptions();

    public OpenOfficePDFConverter(String officeHome) {
        OFFICE_HOME = officeHome;
    }

    private static Options initOptions() {
        Options options = new Options();
        options.addOption(OPTION_OUTPUT_FILE);
        options.addOption(OPTION_INPUT_FILE);
        options.addOption(OPTION_OFFICE_HOME);
        return options;
    }

	public  void convert2PDF(String inputFile, String pdfFile) {
		
//		if(inputFile.endsWith(".txt")){
//			String odtFile = FileUtils.getFilePrefix(inputFile)+".odt";
//			if(new File(odtFile).exists()){
//				System.out.println("odt文件已存在！");
//				inputFile = odtFile;
//			}else{
//				try {
//					FileUtils.copyFile(inputFile,odtFile);
//					inputFile = odtFile;
//				} catch (FileNotFoundException e) {
//					System.out.println("文档不存在！");
//					e.printStackTrace();
//				}
//			}
//		}
		
		startService();
		System.out.println("进行文档转换转换:" + inputFile + " --> " + pdfFile);
        OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
        converter.convert(new File(inputFile),new File(pdfFile));
		stopService();
		System.out.println();
	}


	public void convert2PDF(String inputFile) {
		String pdfFile = FileUtils.getFilePrefix(inputFile)+".pdf";
		convert2PDF(inputFile,pdfFile);
		
	}
	
	public static void startService(){
		DefaultOfficeManagerConfiguration configuration = new DefaultOfficeManagerConfiguration();
        try {
            System.out.println("准备启动服务....");
            configuration.setOfficeHome(OFFICE_HOME);//设置OpenOffice.org安装目录
            configuration.setPortNumbers(port); //设置转换端口，默认为8100
            configuration.setTaskExecutionTimeout(1000 * 60 * 5L);//设置任务执行超时为5分钟
            configuration.setTaskQueueTimeout(1000 * 60 * 60 * 24L);//设置任务队列超时为24小时
         
            officeManager = configuration.buildOfficeManager();
            officeManager.start();	//启动服务
            System.out.println("office转换服务启动成功!");
        } catch (Exception ce) {
            System.out.println("office转换服务启动失败!详细信息:" + ce);
        }
	}
	
	public static void stopService(){
        System.out.println("关闭office转换服务....");
	    if (officeManager != null) {
	        officeManager.stop();
	    }
	    System.out.println("关闭office转换成功!");
	}

	public static void main(String[] args) throws ParseException, IOException {
        DefaultParser commandLineParser = new DefaultParser();
        CommandLine commandLine = commandLineParser.parse(OPTIONS, args);
        String inputFile = null;
        if(commandLine.hasOption(OPTION_INPUT_FILE.getOpt())) {
            inputFile = commandLine.getOptionValue(OPTION_INPUT_FILE.getOpt());
        }

        String officeHome = null;
        if(commandLine.hasOption(OPTION_OFFICE_HOME.getOpt())) {
            officeHome = commandLine.getOptionValue(OPTION_OFFICE_HOME.getOpt());
        }

        String outputFile = null;
        if(commandLine.hasOption(OPTION_OUTPUT_FILE.getOpt())) {
            outputFile = commandLine.getOptionValue(OPTION_OUTPUT_FILE.getOpt());
        }

        if (inputFile == null || officeHome == null) {
            String registry = "java -jar jodconverter-core.jar -i input-file -oh office-home\nor [options] -o output-file -i input-file -oh office-home";
            HelpFormatter configuration = new HelpFormatter();
            configuration.printHelp(registry, OPTIONS);
            System.exit(255);
        }

		OpenOfficePDFConverter convert = new OpenOfficePDFConverter(officeHome);
        if (outputFile != null) {
            convert.convert2PDF(inputFile, outputFile);
        } else {
            convert.convert2PDF(inputFile);
        }
	}
}
