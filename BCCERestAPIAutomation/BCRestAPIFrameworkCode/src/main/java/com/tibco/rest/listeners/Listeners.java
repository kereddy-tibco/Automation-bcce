package com.tibco.rest.listeners;

import com.tibco.rest.common.PropertiesUtil;
import com.tibco.rest.common.SendMail;
import com.tibco.rest.common.TestCaseRunResult;
import com.tibco.rest.restAssured.AssertManager;
import com.tibco.rest.restAssured.Assertions;
import com.tibco.testlink.TestLinkTestCase;
import com.tibco.testlink.TestlinkUpdator;
import io.codeclou.java.junit.xml.merger.JunitXmlParser;
import org.apache.commons.configuration.Configuration;
import org.testng.*;
import org.testng.xml.XmlSuite;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Listeners implements ITestListener, IReporter, IInvokedMethodListener{

    int passedTests = 0;
    int skippedTests = 0;
    int failedTests = 0;

    public TestlinkUpdator testlinkUpdator;
    private String PROJECT_ID = "project.id";
    private String TESTPLAN_ID = "testplan.id";
    private String PLATFORM_ID = "paltform.id";
    private String BUILD_ID = "build.id";

    public static final String ENABLE_FLG_KEY = "testlink.enable.updator";
    boolean deployOnTestLink;

    public Listeners() {
        testlinkUpdator = new TestlinkUpdator();
    }

    public void onFinish(ITestContext arg0) {
    }

    public void onStart(ITestContext arg0) {

        //System.out.println("DevKey : " + PropertiesUtil.getBundle().getPropertyValue("testlink.dev.key").toString());
        if (PropertiesUtil.getBundle().getBoolean(ENABLE_FLG_KEY)) {
            PropertiesUtil.getBundle().setProperty(PROJECT_ID,
                    testlinkUpdator.getProjectId(PropertiesUtil.getBundle().getPropertyValue("testlink.project.name").toString()));
            PropertiesUtil.getBundle().setProperty(TESTPLAN_ID,
                    testlinkUpdator.getTestPlanId(PropertiesUtil.getBundle().getPropertyValue("testlink.testplan.name").toString()));
            PropertiesUtil.getBundle().setProperty(BUILD_ID,
                    testlinkUpdator.getBuildId(PropertiesUtil.getBundle().getPropertyValue("testlink.project.name").toString(),
                            PropertiesUtil.getBundle().getPropertyValue("testlink.testplan.name").toString(),
                            PropertiesUtil.getBundle().getPropertyValue("testlink.build.version").toString()));
            PropertiesUtil.getBundle().setProperty(PLATFORM_ID, PropertiesUtil.getBundle().getPropertyValue("testlink.project.platform.name").toString());
        }
    }

    public void onTestFailedButWithinSuccessPercentage(ITestResult arg0) {

    }

    public void onTestFailure(ITestResult arg0) {
        ++failedTests;
        System.out.println(arg0.getMethod().getMethodName() + " Failed");
        System.out.println(
                "Completed Test Cases : " + (passedTests + failedTests + skippedTests) + String.format(" Pass:%d  Skip:%d  Fail:%d  ",
                        passedTests, skippedTests, failedTests));
        if (PropertiesUtil.getBundle().getBoolean(ENABLE_FLG_KEY)) {
            if(PropertiesUtil.getBundle().getPropertyValue("testlink_tcname") != null && PropertiesUtil.getBundle().getPropertyValue("testlink_tcname").toString()!= "") {
                deployResultOnTestLink(PropertiesUtil.getBundle().getPropertyValue("testlink_tcname"), TestCaseRunResult.FAIL);
            }
        }
    }

    public void onTestSkipped(ITestResult arg0) {
        ++skippedTests;
        System.out.println(arg0.getMethod().getMethodName() + " Skipped");
        System.out.println(
                "Completed Test Cases : " + (passedTests + failedTests + skippedTests) + String.format(" Pass:%d  Skip:%d  Fail:%d  ",
                passedTests, skippedTests, failedTests));
    }

    public void onTestStart(ITestResult arg0) {
    }

    public void onTestSuccess(ITestResult arg0) {

        if (AssertManager.getReporter().getVerificationErrors().length() > 0) {
            onTestFailure(arg0);
        } else {
            ++passedTests;
            System.out.println(arg0.getMethod().getMethodName() + " Passed");
            System.out.println(
                    "Completed Test Cases : " + (passedTests + failedTests + skippedTests) + String.format(" Pass:%d  Skip:%d  Fail:%d  ",
                            passedTests, skippedTests, failedTests));

            if (PropertiesUtil.getBundle().getBoolean(ENABLE_FLG_KEY)) {
                if(PropertiesUtil.getBundle().getPropertyValue("testlink_tcname") != null && PropertiesUtil.getBundle().getPropertyValue("testlink_tcname").toString()!= "") {
                    deployResultOnTestLink(PropertiesUtil.getBundle().getPropertyValue("testlink_tcname"), TestCaseRunResult.PASS);
                }
            }
        }
    }

    private void deployResultOnTestLink(Object method, TestCaseRunResult result) {

        if(method instanceof Method) {
            HashMap<String, Object> params = new HashMap<String, Object>();
            Method m = (Method) method;
            if (m.isAnnotationPresent(TestLinkTestCase.class)) {
                TestLinkTestCase tc = m.getAnnotation(TestLinkTestCase.class);
                params.put(TestLinkTestCase.class.getName(), tc.testCaseName());
            } else {
                params.put(TestLinkTestCase.class.getName(), m.getName());
            }
            System.out.println("Result updated : " + updateResult(params, result, "Details"));
        }else if(method instanceof String){
            System.out.println("Result updated : " + updateResult((String) method, result, "Details"));
        }

    }

    public boolean updateResult(Map<String, ? extends Object> params, TestCaseRunResult result, String details) {
        if (params != null && params.containsKey(TestLinkTestCase.class.getName())) {
            System.out.println("Test case name : " + params.get(TestLinkTestCase.class.getName()));
            String tc = (String) params.get(TestLinkTestCase.class.getName());
            try {
                String tcName = "";
                if (tc.contains("_")) {
                    tcName = tc.replace("_", "-");
                } else {
                    tcName = tc;
                }
                System.out.println("Updating result of test case " + tcName + " with Result " + result);
                String status = "";
                if (result.name().equalsIgnoreCase("pass") || result.name().equalsIgnoreCase("Pass")) {
                    status = "p";
                } else if (result.name().equalsIgnoreCase("fail") || result.name().equalsIgnoreCase("Fail")) {
                    status = "f";
                } else
                    status = "f";

                System.out.println("Test Case : " + tcName + " , Update : "
                        + testlinkUpdator.updateTCByID(tcName, PropertiesUtil.getBundle().getPropertyValue(PROJECT_ID).toString(),
                        PropertiesUtil.getBundle().getPropertyValue(TESTPLAN_ID).toString(),
                        PropertiesUtil.getBundle().getPropertyValue(BUILD_ID).toString(),
                        PropertiesUtil.getBundle().getPropertyValue(PLATFORM_ID).toString(), status));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public boolean updateResult(String tcName, TestCaseRunResult result, String details) {
        if (tcName != null && tcName != "") {
            System.out.println("Test case name : " + tcName);
            try {
                if (tcName.contains("_")) {
                    tcName = tcName.replace("_", "-");
                }

                System.out.println("Updating result of test case " + tcName + " with Result " + result);
                String status = "";
                if (result.name().equalsIgnoreCase("pass") || result.name().equalsIgnoreCase("Pass")) {
                    status = "p";
                } else if (result.name().equalsIgnoreCase("fail") || result.name().equalsIgnoreCase("Fail")) {
                    status = "f";
                } else
                    status = "f";

                System.out.println("Test Case : " + tcName + " , Update : "
                        + testlinkUpdator.updateTCByID(tcName, PropertiesUtil.getBundle().getPropertyValue(PROJECT_ID).toString(),
                        PropertiesUtil.getBundle().getPropertyValue(TESTPLAN_ID).toString(),
                        PropertiesUtil.getBundle().getPropertyValue(BUILD_ID).toString(),
                        PropertiesUtil.getBundle().getPropertyValue(PLATFORM_ID).toString(), status));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outDir) {


    }

    private XmlSuite getRootSuit(XmlSuite suite) {
        if (null != suite.getParentSuite()) {
            return getRootSuit(suite.getParentSuite());
        }
        return suite;
    }

    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {

    }

    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {

    }
}
