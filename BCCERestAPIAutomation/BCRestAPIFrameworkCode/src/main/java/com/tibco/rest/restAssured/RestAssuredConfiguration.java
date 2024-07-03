package com.tibco.rest.restAssured;

import com.tibco.rest.common.PropertiesUtil;
import com.tibco.rest.common.SendMail;
import io.codeclou.java.junit.xml.merger.JunitXmlParser;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.SSLConfig;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.ClassUtils;
import org.apache.http.params.CoreConnectionPNames;
import org.testng.*;
import org.testng.annotations.*;

import java.io.File;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;

import static com.tibco.rest.restAssured.ExtendedRestAssured.preemptive;

public class RestAssuredConfiguration implements ITest {

    private String tcName = "";
    private String htmlOutputDir = "";
    private String xmlOutputDir = "";

    public String getTestName() {
        return this.tcName;
    }

    private void setTestCaseName(String tcname) {
        this.tcName = tcname;
    }

    @Parameters({"baseURI", "basePath", "port", "username", "password", "TestCaseFilePath", "SheetName", "TestCaseGroups"})
    @BeforeTest
    public void setup(ITestContext ctx, @Optional String baseURI, @Optional String basePath, @Optional String port, @Optional String username, @Optional String password, @Optional String TestCaseFilePath, @Optional String SheetName, @Optional String TestCaseGroups) {

        TestRunner runner = (TestRunner) ctx;
        if (baseURI != null && !baseURI.equalsIgnoreCase("")) {
            PropertiesUtil.getBundle().setProperty("baseURI", baseURI);
            //System.out.println("baseURI : "+ PropertiesUtil.getBundle().getPropertyValue("baseURI"));
        }

        if (basePath != null && !basePath.equalsIgnoreCase("")) {
            PropertiesUtil.getBundle().setProperty("basePath", basePath);
            //System.out.println("basePath : "+ PropertiesUtil.getBundle().getPropertyValue("basePath"));
        }

        if (port != null && !port.equalsIgnoreCase("")) {
            PropertiesUtil.getBundle().setProperty("port", port);
            //System.out.println("port : "+ PropertiesUtil.getBundle().getPropertyValue("port"));
        }

        if (username != null && !username.equalsIgnoreCase("")) {
            PropertiesUtil.getBundle().setProperty("username", username);
            //System.out.println("username : "+ PropertiesUtil.getBundle().getPropertyValue("username"));
        }

        if (password != null && !password.equalsIgnoreCase("")) {
            PropertiesUtil.getBundle().setProperty("password", password);
            //System.out.println("password : "+ PropertiesUtil.getBundle().getPropertyValue("password"));
        }

        if (TestCaseFilePath != null && !TestCaseFilePath.equalsIgnoreCase("")) {
            PropertiesUtil.getBundle().setProperty("TestCaseFilePath", TestCaseFilePath);
            //System.out.println("TestCaseFilePath : "+ PropertiesUtil.getBundle().getPropertyValue("TestCaseFilePath"));
        }
        if (SheetName != null && !SheetName.equalsIgnoreCase("")) {
            PropertiesUtil.getBundle().setProperty("SheetName", SheetName);
            //System.out.println("SheetName : "+ PropertiesUtil.getBundle().getPropertyValue("SheetName"));
        }

        if (TestCaseGroups != null && !TestCaseGroups.equalsIgnoreCase("")) {
            PropertiesUtil.getBundle().setProperty("TestCaseGroups", TestCaseGroups);
            //System.out.println("SheetName : "+ PropertiesUtil.getBundle().getPropertyValue("SheetName"));
        }

        PropertiesUtil.getBundle().setProperty("report.dir", runner.getOutputDirectory().split("Suite")[0] + "/html");
        PropertiesUtil.getBundle().setProperty("report.response.dir", runner.getOutputDirectory().split("Suite")[0] + "/html/response/" + ctx.getName());
        //System.out.println("report.dir : "+PropertiesUtil.getBundle().getPropertyValue("report.dir").toString());
        //System.out.println("report.response.dir : "+PropertiesUtil.getBundle().getPropertyValue("report.response.dir").toString());
        ExtendedRestAssured.baseURI = PropertiesUtil.getBundle().getPropertyValue("baseURI").toString();
        ExtendedRestAssured.port = Integer.parseInt(PropertiesUtil.getBundle().getPropertyValue("port").toString());
        //ExtendedRestAssured.basePath = PropertiesUtil.getBundle().getPropertyValue("basePath").toString();

        if ((PropertiesUtil.getBundle().getPropertyValue("username") != null && !PropertiesUtil.getBundle().getPropertyValue("username").toString().equalsIgnoreCase("")) && (PropertiesUtil.getBundle().getPropertyValue("password") != null && !PropertiesUtil.getBundle().getPropertyValue("password").toString().equalsIgnoreCase(""))) {
            ExtendedRestAssured.authentication = preemptive().basic(PropertiesUtil.getBundle().getPropertyValue("username").toString(), PropertiesUtil.getBundle().getPropertyValue("password").toString());
        }

        ExtendedRestAssured.useRelaxedHTTPSValidation();
        ExtendedRestAssured.urlEncodingEnabled = false;
        RestAssuredConfig config = ExtendedRestAssured.config().httpClient(HttpClientConfig.httpClientConfig().setParam(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000));
        ExtendedRestAssured.given().config(config);

        HashMap<String, String> cookieMap = new HashMap<String, String>();
        PropertiesUtil.getBundle().addProperty("cookie",cookieMap);
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        try {
            htmlOutputDir = PropertiesUtil.getBundle().getProperty("report.dir").toString();
            if (new File(htmlOutputDir).exists()) {
                System.out.println("HTML Report location : " + htmlOutputDir);
            } else {
                System.out.println("Output directory doesn't exist.");
                //System.exit(1);
            }
        } catch (Exception e) {
            System.out.println("Output directory doesn't exist.");
        }
    }


    @BeforeMethod(alwaysRun = true)
    final public void setupMethod(Object[] args) {
        try {
            if (null != args || String.valueOf(args[0]) != "") {
                System.out.println("Test Case Name : " + String.valueOf(args[0]));
                AssertManager.getReporter().setMethod(String.valueOf(args[0]));
                setTestCaseName(String.valueOf(args[0]));
            }
        } catch (Exception e) {
            System.out.println("Unable to set method name");
        }
        PropertiesUtil.getBundle().setProperty("testlink_tcname","");
    }

    @AfterMethod(alwaysRun = true)
    final public void afterMethod(ITestContext testContext, ITestResult tr, Object[] args) {
        if ((null != args[10] || String.valueOf(args[10]) != "") && !String.valueOf(args[10]).equalsIgnoreCase("null")) {
            Double timeout = new Double(String.valueOf(args[10]));
            System.out.println("Wait for timeout : " + timeout);
            try {
                //System.out.println("timeout1 : "+timeout);
                Thread.sleep(timeout.intValue());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (NumberFormatException e1) {
                e1.printStackTrace();
            }
            //AssertManager.getReporter().setMethod(String.valueOf(args[0]));
            //setTestCaseName(String.valueOf(args[0]));
        }

        Reporter.setCurrentTestResult(tr);
        Reporter.log(AssertManager.getReporter().getAssertionsLog());
        String verificationErrors = AssertManager.getReporter().getVerificationErrors();

        if ((verificationErrors.length() > 0) && (tr.getStatus() == ITestResult.SUCCESS)) {
            if ((null != testContext.getPassedTests())) {
                if (testContext.getPassedTests().getResults(tr.getMethod()).size() > 1) {
                    testContext.getPassedTests().getResults(tr.getMethod()).remove(tr);
                } else {
                    testContext.getPassedTests().removeResult(tr.getMethod());
                }
            }
            tr.setStatus(ITestResult.FAILURE);
            if (null != testContext.getFailedTests()) {
                testContext.getFailedTests().addResult(tr, tr.getMethod());
            }
        } else {
            tr.setStatus(ITestResult.FAILURE);
        }

    }
}
