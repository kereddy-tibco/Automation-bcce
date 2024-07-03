package com.tibco.rest.restAssured;

import com.tibco.rest.common.PropertiesUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.List;

public class Assertions {
    //private PropertiesUtil props;
    protected StringBuffer verificationErrors;
    protected StringBuffer assertionsLog;
    protected String method;
    protected String responseDir;
    protected String reportDir;

    public Assertions() {
        assertionsLog = new StringBuffer();
        verificationErrors = new StringBuffer();
        reportDir = PropertiesUtil.getBundle().getPropertyValue("report.dir").toString();
        responseDir = PropertiesUtil.getBundle().getPropertyValue("report.response.dir").toString();
    }

    public void checkForVerificationErrors() {
        String verificationErrorString = getVerificationErrors();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }

    public static void fail(String message) {
        throw new AssertionError(message);
    }

    protected void clearVerificationErrors() {
        verificationErrors = new StringBuffer();
    }

    public String getVerificationErrors() {
        return verificationErrors.toString().trim();

    }

    String SEC_Header = "<div class=\"result_pshow result_fshow result_ishow\" > "
            + "<input checked type=\"checkbox\" value=\"result_p\" onclick=\"toggleResult(this)\"/><span class=\"pass-label\">P</span>ass"
            + "<input checked type=\"checkbox\" value=\"result_f\" onclick=\"toggleResult(this)\"/><span class=\"fail-label\">F</span>ail"
            + "<input checked type=\"checkbox\" value=\"result_i\" onclick=\"toggleResult(this)\"/><span class=\"info-label\">I</span>nfo";
    String SEC_Footer = "</div>";

    public String getAssertionsLog() {
        String retVal = assertionsLog.toString();
        return SEC_Header + retVal + SEC_Footer;
    }

    public void addVerificationError(String msg) {
            verificationErrors.append(msg);
    }

    public void addVerificationError(Throwable e) {
        addVerificationError(e.getMessage());
    }

    public void clearAssertionsLog() {
        assertionsLog = new StringBuffer();
    }

    public void addAssertionsLog(String assertionsLog) {
        this.assertionsLog.append(formatInfoMessage(assertionsLog));
    }

    protected static String formatSuccessMessage(String message) {
        return MessageTypes.Pass.formatMessage(message);
    }

    protected static String formatFailureMessage(String message) {
        return MessageTypes.Fail.formatMessage(message);
    }

    protected static String formatInfoMessage(String message) {
        return MessageTypes.Info.formatMessage(message);
    }

    protected String getTestCaseName() {
        if (null == method || method == "") {
            return "RestTest";
        }
        return method;
    }

    protected void setMethod(String method) {
        this.method = method;
        clearVerificationErrors();
        clearAssertionsLog();
    }

    public void storeResponseInFile(String fileName , Object response){
        addAssertionLog(receiveResponseAndGetLink(fileName, response), MessageTypes.Info);
    }

    public void storeValueInFile(String fileName , Object response){
        try {
            System.out.println("stored File Path :"+ new File(responseDir).getAbsolutePath()+"/"+fileName+".json");
			File frepdir=new File(responseDir);
			if(!frepdir.exists()){
			    System.out.println("creating dir = "+frepdir.getAbsolutePath());
				frepdir.mkdirs();
			}
			frepdir = new File(responseDir+"/"+fileName+".json");

			//f.setWritable(true, false);
            FileUtils.writeStringToFile(frepdir, response.toString(),"UTF-8", false);
            //String sc = getRelativePath(reportDir, responseDir) + "/" + fileName + ".json";
        } catch (Throwable th) {
            System.err.println("Unable to create file OR no response received: " + th.getMessage());
        }
    }

    private String receiveResponseAndGetLink(String fileName, Object responseBody) {
        try {
            //System.out.println("report dir : "+new File(reportDir).getAbsolutePath());
            //System.out.println("response dir : "+new File(responseDir).getAbsolutePath());
            //System.out.println("stored File Path :"+ new File(responseDir).getAbsolutePath()+"/"+fileName+".json");
            storeValueInFile(fileName,responseBody);
            //FileUtils.writeStringToFile(new File(responseDir+"/"+fileName+".json"), responseBody.toString(),"UTF-8", false);
            String sc = getRelativePath(reportDir, responseDir) + "/" + fileName + ".json";
            //System.out.println("link :"+ sc);
            return " <a href=\"" + sc + "\" target=\"_blank\">[View Response]</a> ";
        } catch (Throwable th) {
            System.err.println("Unable to create file OR no response received: " + th.getMessage());
        }
        return "";
    }

    public String getValueFromFile(String fileName) {
        try {
            //System.out.println("Value retrieve File Path :"+ new File(responseDir).getAbsolutePath()+"/"+fileName+".json");
            //FileUtils.writeStringToFile(new File(responseDir+"/"+fileName+".json"), responseBody.toString(),"UTF-8", false);
            //String sc = getRelativePath(reportDir, responseDir) + "/" + fileName + ".json";

            String value = FileUtils.readFileToString(new File(responseDir+"/"+fileName+".json"),"UTF-8");
            return value;
        } catch (Throwable th) {
            System.err.println("Unable to get value from file "+fileName);
            addAssertionLog("Unable to get value from file "+fileName, MessageTypes.Fail);
        }
        return "";
    }

    private String getRelativePath(String root, String filePath) {
        String relative = new File(root).toURI().relativize(new File(filePath).toURI()).getPath();
        return relative;
    }

    public void addAssertionLog(String msg, MessageTypes type) {
        assertionsLog.append(type.formatMessage(msg));
        if (type == MessageTypes.Fail) {
            verificationErrors.append(msg);
        }
    }

    public void assertTrue(boolean b, String failMsg, String successMsg) {
        if (!b) {
            addAssertionLog(failMsg, MessageTypes.Fail);
            throw new AssertionError(failMsg);
        }else {
            addAssertionLog(successMsg, MessageTypes.Pass);
        }
    }

    public void assertFalse(boolean b, String failMsg, String successMsg) {
        assertTrue(!b, failMsg, successMsg);
    }

    public void assertEquals(String actual, String expected, String message) {
        String msg = MessageFormat.format(PropertiesUtil.getBundle().getPropertyValue("assert.equals.common").toString(),
                expected.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;"),
                String.valueOf(actual).toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
        if(equals(expected,actual)) {
            assertTrue( true, msg,msg);
        }else{
            assertTrue( false, msg,msg);
        }
    }

    public void assertNotEquals(String actual, String expected, String message) {
        //System.out.println("actual : "+actual.toString());
        //System.out.println("expected : "+expected.toString());

        String msg = MessageFormat.format(PropertiesUtil.getBundle().getPropertyValue("assert.not.equals.common").toString(),
                expected.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;"),
                actual.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;"));

        if(equals(expected,actual)) {
            assertTrue( false, msg,msg);
        }else{
            assertTrue( true, msg,msg);
        }
    }

    public void assertContains(Object actual, Object expected, String message) {
        //System.out.println("actual : "+actual.toString());
        //System.out.println("expected : "+expected.toString());
        String msg = MessageFormat.format(PropertiesUtil.getBundle().getPropertyValue("assert.contains.common").toString(),
                expected.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;"),
                String.valueOf(actual).toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
        if(actual instanceof String) {
            if (actual.toString().contains(expected.toString())) {
                assertTrue(true, msg, msg);
            } else {
                assertTrue(false, msg, msg);
            }
        }else
        {
            if (((List<String>) actual).contains((List<String>) expected)) {
                assertTrue(true, msg, msg);
            } else {
                assertTrue(false, msg, msg);
            }
        }
    }

    public void assertNotContains(Object actual, Object expected, String message) {
        String msg = MessageFormat.format(PropertiesUtil.getBundle().getPropertyValue("assert.not.contains.common").toString(),
                expected.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;"),
                String.valueOf(actual).toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
        if(actual instanceof String) {
            if (actual.toString().contains(expected.toString())) {
                assertTrue(false, msg, msg);
            } else {
                assertTrue(true, msg, msg);
            }
        }else
        {
            if (((List<String>) actual).contains((List<String>) expected)) {
                assertTrue(false, msg, msg);
            } else {
                assertTrue(true, msg, msg);
            }
        }
    }


    public void assertLessThan(Object actual, Object expected, String message) {
        String msg = MessageFormat.format(PropertiesUtil.getBundle().getPropertyValue("assert.lessthan.common").toString(),
                expected.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;"),
                String.valueOf(actual).toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
        try {
            if (actual instanceof String) {
                int actualVal = Integer.parseInt(actual.toString());
                int expectedVal = Integer.parseInt(expected.toString());
                if (actualVal < expectedVal) {
                    assertTrue( true, msg,msg);
                }else{
                    assertTrue( false, msg,msg);
                }
            } else if (actual instanceof Number) {
                int actualVal = (Integer) actual;
                int expectedVal = Integer.parseInt(expected.toString());
                if (actualVal < expectedVal) {
                    assertTrue( true, msg,msg);
                }else{
                    assertTrue( false, msg,msg);
                }
            }
        } catch (Exception e) {
            AssertManager.getReporter().addAssertionLog("Unable to convert expected value into numeric value", Assertions.MessageTypes.Fail);
            AssertManager.getReporter().verifyTrue(false, "Unable to convert expected value into numeric value","Unable to convert expected value into numeric value");
        }
    }

    public void assertLessOrEqual(Object actual, Object expected, String message) {
        String msg = MessageFormat.format(PropertiesUtil.getBundle().getPropertyValue("assert.lessorequal.common").toString(),
                expected.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;"),
                String.valueOf(actual).toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
        try {
            if (actual instanceof String) {
                int actualVal = Integer.parseInt(actual.toString());
                int expectedVal = Integer.parseInt(expected.toString());
                if (actualVal <= expectedVal) {
                    assertTrue( true, msg,msg);
                }else{
                    assertTrue( false, msg,msg);
                }
            } else if (actual instanceof Number) {
                int actualVal = (Integer) actual;
                int expectedVal = Integer.parseInt(expected.toString());
                if (actualVal <= expectedVal) {
                    assertTrue( true, msg,msg);
                }else{
                    assertTrue( false, msg,msg);
                }
            }
        } catch (Exception e) {
            AssertManager.getReporter().addAssertionLog("Unable to convert expected value into numeric value", Assertions.MessageTypes.Fail);
            AssertManager.getReporter().verifyTrue(false, "Unable to convert expected value into numeric value","Unable to convert expected value into numeric value");
        }
    }

    public void assertGreaterThan(Object actual, Object expected, String message) {
        String msg = MessageFormat.format(PropertiesUtil.getBundle().getPropertyValue("assert.greaterthan.common").toString(),
                expected.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;"),
                String.valueOf(actual).toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
        try {
            if (actual instanceof String) {
                int actualVal = Integer.parseInt(actual.toString());
                int expectedVal = Integer.parseInt(expected.toString());
                if (actualVal > expectedVal) {
                    assertTrue( true, msg,msg);
                }else{
                    assertTrue( false, msg,msg);
                }
            } else if (actual instanceof Number) {
                int actualVal = (Integer) actual;
                int expectedVal = Integer.parseInt(expected.toString());
                if (actualVal > expectedVal) {
                    assertTrue( true, msg,msg);
                }else{
                    assertTrue( false, msg,msg);
                }
            }
        } catch (Exception e) {
            AssertManager.getReporter().addAssertionLog("Unable to convert expected value into numeric value", Assertions.MessageTypes.Fail);
            AssertManager.getReporter().verifyTrue(false, "Unable to convert expected value into numeric value","Unable to convert expected value into numeric value");
        }
    }

    public void assertGreaterOrEqual(Object actual, Object expected, String message) {
        String msg = MessageFormat.format(PropertiesUtil.getBundle().getPropertyValue("assert.greaterorequal.common").toString(),
                expected.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;"),
                String.valueOf(actual).toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
        try {
            if (actual instanceof String) {
                int actualVal = Integer.parseInt(actual.toString());
                int expectedVal = Integer.parseInt(expected.toString());
                if (actualVal >= expectedVal) {
                    assertTrue( true, msg,msg);
                }else{
                    assertTrue( false, msg,msg);
                }
            } else if (actual instanceof Number) {
                int actualVal = (Integer) actual;
                int expectedVal = Integer.parseInt(expected.toString());
                if (actualVal >= expectedVal) {
                    assertTrue( true, msg,msg);
                }else{
                    assertTrue( false, msg,msg);
                }
            }
        } catch (Exception e) {
            AssertManager.getReporter().addAssertionLog("Unable to convert expected value into numeric value", Assertions.MessageTypes.Fail);
            AssertManager.getReporter().verifyTrue(false, "Unable to convert expected value into numeric value","Unable to convert expected value into numeric value");
        }
    }

    public boolean equals(Object expected, Object actual){
        if (actual instanceof String) {
            //System.out.println("expected : "+expected);
            //System.out.println("actual : "+actual);
            return ((String) expected).equalsIgnoreCase((String)actual) ? true : false;
        }else if(actual instanceof Integer){
            return String.valueOf(expected).equalsIgnoreCase(String.valueOf(actual)) ? true : false;
        }
        return expected.equals(actual);
    }

    public boolean contains(Object expected, Object actual){
        if (actual instanceof String) {
            //System.out.println("expected : "+expected);
            //System.out.println("actual : "+actual);
            return ((String) expected).contains((String)actual) ? true : false;
        }else if(actual instanceof Integer){
            return String.valueOf(expected).contains(String.valueOf(actual)) ? true : false;
        }
        return false;
    }


    // ********************************************************//
    // verifications //
    /** Like assertTrue, but fails at the end of the test (during tearDown) */
    public boolean verifyTrue(boolean b, String failMessage, String successMessage) {
        try {
            assertTrue(b, failMessage, successMessage);
            return true;
        } catch (Error e) {
            addVerificationError(e);
            return false;
        }
    }

    /** Like assertFalse, but fails at the end of the test (during tearDown) */
    public boolean verifyFalse(boolean b, String failMessage, String successMessage) {
        return verifyTrue(!b, failMessage, successMessage);
    }

    /** Like assertEquals, but fails at the end of the test (during tearDown) */
    public boolean verifyEquals(String actual, String expected, String message) {
        try {
            assertEquals(actual, expected, message);
            return true;
        } catch (Error e) {
            addVerificationError(e);
            return false;
        }
    }

    public enum MessageTypes {
        Pass, Fail, Info, Warn;
        public String formatMessage(String message) {
            return String.format(MSG_FORMAT, name().toLowerCase(), name().toLowerCase(), name().charAt(0), message);
        }
        private static final String MSG_FORMAT = "<div  class=\"%s\"><span class=\"%s-label\">%s </span>%s</div>";

    }


}
