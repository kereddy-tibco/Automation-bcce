package com.tibco.rest.test;

import io.codeclou.java.junit.xml.merger.JunitXmlParser;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;

public class Test {

    public static void main(String[] args) {
        try {
            FileInputStream fis1 = new FileInputStream(new File("./test-cases-files/SampleTestCases_2.xls"));
            Workbook wb = WorkbookFactory.create(fis1);
            Sheet sheet = wb.getSheetAt(0);
            System.out.println("sheetname : "+sheet.getSheetName());
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
