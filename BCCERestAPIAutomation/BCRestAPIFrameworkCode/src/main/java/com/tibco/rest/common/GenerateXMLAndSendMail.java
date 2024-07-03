package com.tibco.rest.common;

import io.codeclou.java.junit.xml.merger.JunitXmlParser;

import java.io.File;

public class GenerateXMLAndSendMail {
	
	//Pass database parameters from the command line
	public static String dBType=System.getProperty("databaseType");
	//Pass build number parameters from the command line
	public static String buildNum=System.getProperty("buildNumber");
	
	public static String outDir=System.getProperty("mail.output.dir");
	
    public static void main(String[] args){
    	
        if(!Boolean.parseBoolean(PropertiesUtil.getBundle().getPropertyValue("mailer.skip").toString())) {
           // String outDir = args[0];

            System.out.println("outDir : " + GenerateXMLAndSendMail.outDir);
            File outputFolder = new File(GenerateXMLAndSendMail.outDir);
            //System.out.println("outDir : " +outDir);
           // File outputFolder = new File(outDir);
            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }

            if (!new File(outputFolder.getAbsolutePath() + "/junitreports").exists()) {
                //System.out.println("creating new folder : " + new File(outDir + "/junitreports").getAbsolutePath());
                new File(outputFolder.getAbsolutePath() + "/junitreports").mkdirs();
            }

            File[] files = outputFolder.listFiles();
            for(File f : files){
                if(f.getName().contains("testng-failed.xml")){
                    f.delete();
                }
            }

            String[] testArgs = {"-i=" + outDir, "-o=" + outDir + "/junitreports/TESTS-TestSuites.xml", "-s=RESTAPIAutomationSuites"};
            JunitXmlParser parser = new JunitXmlParser();
            try {
                parser.run(testArgs);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                System.out.println("sending mail to recipients " + PropertiesUtil.getBundle().getPropertyValue("mailer.to.list").toString());
               // SendMail.generateMailAndSendMail(PropertiesUtil.getBundle().getPropertyValue("mailer.template.file").toString(), outDir + "/junitreports/TESTS-TestSuites.xml");
                SendMail.generateMailAndSendMail(PropertiesUtil.getBundle().getPropertyValue("mailer.template.file").toString(), outDir + "/junitreports/TESTS-TestSuites.xml",GenerateXMLAndSendMail.dBType,GenerateXMLAndSendMail.buildNum);
            } catch (Exception e) {
                System.err.println("Unable to send mail.");
            }
        }else{
            System.out.println("Skip sending report email");
        }
    }

}
