package com.tibco.rest.common;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;

public class GetDataFromExcel {

    @DataProvider(name = "GetTCData")
    public static Object[][] getData() {

        //System.out.println("TestCaseFilePath in GetDataExcel : "+filePath);
        //System.out.println("SheetName in GetDataExcel : "+sheetName);

        Object[][] dataObj = null;
        ArrayList<HashMap<String, String>> listMap = new ArrayList<HashMap<String, String>>();
        boolean descriptionSet = false;
        boolean groupFlag = false;
        List<String> headerList = new ArrayList<String>();
        ArrayList<String> groupsList = new ArrayList<String>();

        FileInputStream fis = null;
        int rowCount = 0;
        try {
            String filePath = PropertiesUtil.getBundle().getPropertyValue("TestCaseFilePath").toString();
            String sheetName = PropertiesUtil.getBundle().getPropertyValue("SheetName").toString();
            String groups = PropertiesUtil.getBundle().getPropertyValue("TestCaseGroups").toString();

            System.out.println("TestCaseFilePath : " + filePath);
            System.out.println("SheetName : " + sheetName);
            System.out.println("TestCaseGroups : " + groups);

            //XSSFWorkbook workbook = null;
            //try {
                fis = new FileInputStream(new File(filePath));
                XSSFWorkbook workbook = new XSSFWorkbook(fis);
            //}catch(Exception e){
            //    e.printStackTrace();
            //}

            XSSFSheet sheet = workbook.getSheet(sheetName);
            Iterator<Row> rows = sheet.rowIterator();

            if(groups != null && !groups.equalsIgnoreCase("")){
                String[] tempGroups = groups.split(",");
                for(String grp : tempGroups){
                    String temp = grp.replaceAll("\\[", "").replaceAll("\\]","").trim();
                    //System.out.println("group : "+temp);
                    groupsList.add(temp);
                }
            }

            while (rows.hasNext()) {
                HashMap<String, String> dataMap = new HashMap<String, String>();
                XSSFRow row = ((XSSFRow) rows.next());

                if (row.getRowNum() == 0) {
                    Iterator<Cell> cells = row.cellIterator();
                    while (cells.hasNext()) {
                        Cell cell = cells.next();
                        if(!cell.getStringCellValue().equalsIgnoreCase("")) {
                            if (!(cell.getStringCellValue().equalsIgnoreCase("Enable")
                                    || cell.getStringCellValue().toLowerCase().equalsIgnoreCase("description")
                                    || cell.getStringCellValue().toLowerCase().equalsIgnoreCase("groups"))) {
                                headerList.add(cell.getStringCellValue());
                            } else {
                                if (cell.getStringCellValue().toLowerCase().equalsIgnoreCase("description")) {
                                    descriptionSet = true;
                                }
                                if (cell.getStringCellValue().toLowerCase().equalsIgnoreCase("groups")
                                        || cell.getStringCellValue().toLowerCase().equalsIgnoreCase("Groups")) {
                                    groupFlag = true;
                                }
                            }
                        }
                    }
                }

                if (row.getRowNum() != 0) {
                    boolean flag1 = false;
                    //System.out.println("first col : "+row.getCell(0).toString().trim());
                    if (row.getCell(0)!=null && !row.getCell(0).toString().equalsIgnoreCase("")
                            && Boolean.parseBoolean(row.getCell(0).toString().trim())) {

                        String[] actualGroupList = null;
                        try {
                            actualGroupList = row.getCell(1).toString().trim().split(",");
                            //System.out.println("actualGroupList : " + Arrays.asList(actualGroupList).toString()
                            // + ", flag : " + Arrays.asList(actualGroupList).containsAll(groupsList));
                        }catch(NullPointerException e){
                            actualGroupList = new String[]{""};
                        }
                        if (groupsList.containsAll(Arrays.asList(actualGroupList))
                                || Arrays.asList(actualGroupList).containsAll(groupsList)) {
                            int j;
                            if(groupFlag){
                                j = 2;
                            }else{
                                j = 1;
                            }

                            if(!headerList.contains("ResponseCode")){
                                headerList.add("ResponseCode");
                            }

                            for (int i = 1; i <= headerList.size(); i++) {
                                try {
                                   // System.out.println("header : "+headerList.get(i-1));

                                    Cell cells = row.getCell(j);
                                    String value = "";
                                    try {
                                        if (cells.getCellType() == 0) {
                                            value = String.valueOf(cells.getNumericCellValue());
                                        } else if (cells.getCellType() == 1) {
                                            value = cells.getStringCellValue();
                                        }
                                    } catch (NullPointerException e) {
                                        value = "null";
                                    }
                                    if (!value.equalsIgnoreCase("")) {
                                        if (!value.equalsIgnoreCase("null")) {
                                            value = value.trim();
                                        }
                                        //System.out.println("key : "+headerList.get(i-1)+" , value : "+value);
                                        dataMap.put(headerList.get(i - 1), value);
                                        flag1 = true;

                                    }
                                    if ((j == 1 || j==2) && descriptionSet) {
                                        j++;
                                    }
                                    j++;
                                } catch (Exception e1) { }
                            }
                            if (flag1) {
                                rowCount++;
                                listMap.add(dataMap);
                            }
                        }
                    }
                }
            }

            //System.out.println("header list : "+headerList.size());

            dataObj = new Object[rowCount][headerList.size()];

            for (int j = 0; j < rowCount; j++) {
                HashMap<String, String> map = listMap.get(j);
                for (int k = 0; k < headerList.size(); k++) {
                    //System.out.println("key : " + headerList.get(k) + " , value : " + map.get(headerList.get(k)));
                    dataObj[j][k] = map.get(headerList.get(k));
                }
            }
       } catch (Exception e) {
            System.err.println("Unable to read test cases files.");
            new SkipException("Unable to read test cases files. Please check provided test case xlsx file.");
            System.exit(0);
            //return null;
        }
        if (dataObj.length <= 0) {
            System.err.println("ERROR - No Test cases are marked for execution or there are no test cases in file. Please check file and rerun.");
            new SkipException("ERROR - No Test cases are marked for execution or there are no test cases in file. Please check file and rerun.");
            System.exit(0);
            return null;
        } else {
            return dataObj;
        }
    }

    public static void main(String[] args){
        GetDataFromExcel.getData();
    }
}
