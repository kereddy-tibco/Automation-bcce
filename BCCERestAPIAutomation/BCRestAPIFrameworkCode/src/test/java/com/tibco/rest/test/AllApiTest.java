package com.tibco.rest.test;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPathException;
import com.tibco.rest.common.PropertiesUtil;
import com.tibco.rest.restAssured.APIURL;
import com.tibco.rest.restAssured.AssertManager;
import com.tibco.rest.restAssured.Assertions;
import com.tibco.rest.restAssured.RestAssuredConfiguration;

import io.restassured.RestAssured;
import io.restassured.authentication.AuthenticationScheme;
import io.restassured.authentication.BasicAuthScheme;
import io.restassured.authentication.PreemptiveAuthProvider;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;

//import static com.tibco.rest.restAssured.ExtendedRestAssured.given;
import static io.restassured.RestAssured.authentication;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;
import static org.hamcrest.Matchers.*;

public class AllApiTest extends RestAssuredConfiguration {

    //@Test
    public void test1() {
        Response response = given().get(APIURL.getDomains);
        //response.then().assertThat().body("title", hasToString("sunt aut facere repellat provident occaecati excepturi optio reprehenderit"));
        AssertManager.getReporter().addAssertionLog(response.body().asString(), Assertions.MessageTypes.Pass);
        System.out.println("Response : " + response.getBody().asString());

        AssertManager.getReporter().storeResponseInFile("",response);
    }

    //@Test
    public static void main(String[] args) {
        RestAssured.baseURI = "https://devjira.tibco.com";
        //RestAssured.basePath = "";
        RestAssured.port = 80;
        RestAssured.basePath = "/rest/api/2";
        /*BasicAuthScheme scheme = new BasicAuthScheme();
        scheme.setUserName("rgangani");
        scheme.setPassword("Tibco20189");
        */
        //RestAssured.given().auth().preemptive().basic("rgangani","Tibco20189");

        RestAssured.authentication = preemptive().basic("rgangani", "Tibco20189");

        Response response = given().get(APIURL.JIRA_REST_ISSUE_DETAILS);
        JsonPath jsonPath = new JsonPath(response.body().asString());
        System.out.println("Response : "+response.asString());
        //AssertManager.getReporter().storeResponseInFile(response.body().asString());

        DocumentContext documentContext =  com.jayway.jsonpath.JsonPath.parse(response.body().asString());
        String lst = documentContext.read("$.key");
        System.out.println("key :" +lst);
    }


}
