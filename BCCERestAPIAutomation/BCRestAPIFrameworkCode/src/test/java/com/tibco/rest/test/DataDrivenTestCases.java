package com.tibco.rest.test;

import com.google.gson.*;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.tibco.rest.common.GetDataFromExcel;
import com.tibco.rest.common.JavaScriptUtility;
import com.tibco.rest.common.PropertiesUtil;
import com.tibco.rest.restAssured.AssertManager;
import com.tibco.rest.restAssured.Assertions;
import com.tibco.rest.restAssured.RestAssuredConfiguration;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;

import static com.tibco.rest.restAssured.ExtendedRestAssured.given;

public class DataDrivenTestCases extends RestAssuredConfiguration {

    @Test(dataProvider = "GetTCData", dataProviderClass = GetDataFromExcel.class)
    public void testcase(String tcName,
                         String restMethodType,
                         String restApi,
                         String headers,
                         String contentType,
                         String jsonBody,
                         String jsonPathForValidation,
                         String operatorForComparison,
                         String expectedResponse,
                         String storeValue,
                         String timeout,
                         String responseCode) {

        PropertiesUtil.getBundle().setProperty("testlink_tcname",tcName);
        //System.out.println("tcName : "+tcName);
        //System.out.println("description : "+description);
        //System.out.println("restMethodType : "+restMethodType);
        //System.out.println("restApi : "+restApi);
        //System.out.println("headers : "+headers);
        //System.out.println("contentType : "+contentType);
        //System.out.println("jsonBody : "+jsonBody);
        //System.out.println("jsonPathForValidation : "+jsonPathForValidation);
        //System.out.println("operatorForComparison : "+operatorForComparison);
        //System.out.println("expectedResponse : "+expectedResponse);
        //System.out.println("storeValue : "+storeValue);
        //System.out.println("timeout : "+timeout);
        //System.out.println("responseCode : "+responseCode);

        RequestSpecification given = null;

        HashMap<String, Object> headerMap = new HashMap<String, Object>();

        if (headers != null && !headers.equalsIgnoreCase("null")) {
            if(headers.startsWith("{")) {
                headerMap = new Gson().fromJson(headers, HashMap.class);
                Set<String> keys = headerMap.keySet();

                for (String key : keys) {
                    String k = key;
                    String value = (String) headerMap.get(k);
                    if(value.contains("{") && value.contains("}")){
                        String variable = value.substring(value.indexOf("{") + 1, value.indexOf("}"));
                        //System.out.println("variable : "+variable);
                        String variableValue = AssertManager.getReporter().getValueFromFile(variable.replace(".", "/"));

                        value = value.replaceAll("\\$\\{"+variable+"}", variableValue);
                    }
                    headerMap.put(k, value);
                    ///System.out.println("json key : " + k);
                    //System.out.println("json value : " + value);
                }
            }else{
                String[] headerArrays = headers.split(",");
                for (String header : headerArrays) {
                    String[] headerArray = header.split("=");
                    String value = headerArray[1];
                    if(value.contains("{") && value.contains("}")){
                        String variable = value.substring(value.indexOf("{") + 1, value.indexOf("}"));
                        //System.out.println("variable : "+variable);
                        String variableValue = AssertManager.getReporter().getValueFromFile(variable.replace(".", "/"));
                        value = value.replaceAll("\\$\\{"+variable+"}", variableValue);
                    }
                    //System.out.println("header key : "+headerArray[0]+" , value : "+value);
                    headerMap.put(headerArray[0], value);
                }
            }
        }

        if (jsonBody != null && !jsonBody.equalsIgnoreCase("null") && jsonBody.contains("${")) {
            String[] vars = jsonBody.split("\\$");
            for (String var : vars) {
                //System.out.println("var : "+var);
                if (var.contains("{") && var.contains("}")) {
                    String key = var.substring(var.indexOf("{") + 1, var.indexOf("}"));
                    //System.out.println("key : "+key);
                    String value = AssertManager.getReporter().getValueFromFile(key.replace(".", "/"));
                    //System.out.println("key : " + key);
                    //System.out.println("value : " + value);

                    jsonBody = jsonBody.replaceAll("\\$\\{" + key + "}", value);
                    //restKeyValueMap.put(key,value);
                }
            }
        }
        System.out.println("jsonBody : " + jsonBody);
        System.out.println("contentType : " + contentType);

        if (contentType != null && !contentType.equalsIgnoreCase("null") && contentType.equalsIgnoreCase("multipart/form-data")) {
            //System.out.println("contentType  multipart form data : " + contentType);
            given = given().headers(headerMap).contentType("multipart/form-data");
            if (jsonBody != null && !jsonBody.equalsIgnoreCase("null")) {
                String[] multipartFiles = jsonBody.split(",");
                int filepostfix = 0;
                for (String filepath : multipartFiles) {
                    if (filepath != null && filepath.contains("=")) {
                        String fileKey = filepath.split("=")[0];
                        String filePathKey = filepath.split("=")[1];
                        //System.out.println("in equal in filepath");
                        //System.out.println("fileKey : "+fileKey);
                        //System.out.println("filePathKey : "+filePathKey);
                        given.multiPart(fileKey, new File(filePathKey));
                    } else {
                        //System.out.println("in else equal in filepath");
                        String fileKey = "file";
                        if (filepostfix > 0) {
                            fileKey = "file" + String.valueOf(filepostfix);
                        }
                        String filePathKey = filepath;
                        filepostfix++;
                        //System.out.println("fileKey : "+fileKey);
                        //System.out.println("filePathKey : "+filePathKey);
                        given.multiPart(fileKey, new File(filePathKey));
                    }
                }
            }
        }
        else if(contentType != null && !contentType.equalsIgnoreCase("null") && contentType.equalsIgnoreCase("application/x-www-form-urlencoded")){
            //System.out.println("contentType application/x-www-form-urlencoded : "+contentType);
            if (jsonBody != null && !jsonBody.equalsIgnoreCase("null")) {
                //given = given().headers(headerMap).contentType(contentType).body(jsonBody);
                HashMap<String, String> bodyMap = new Gson().fromJson(jsonBody, HashMap.class);
                given = given().headers(headerMap).contentType(contentType).formParams(bodyMap);
            } else {
                given = given().contentType(contentType).headers(headerMap);
            }
        }
        else if(contentType != null && contentType.equalsIgnoreCase("application/json")){
            //System.out.println("content-type is application/json");
            if (jsonBody != null && !jsonBody.equalsIgnoreCase("null")) {
                given = given().headers(headerMap).contentType(contentType).body(jsonBody);
            } else {
                given = given().headers(headerMap).contentType(contentType);
            }
        }else{
            //System.out.println("content-type is not used");
            if (jsonBody != null && !jsonBody.equalsIgnoreCase("null")) {
                given = given().headers(headerMap).body(jsonBody);
            } else {
                given = given().headers(headerMap);
            }
        }

        //HashMap<String,String> restKeyValueMap = new HashMap<String, String>();
        if (restApi != null && !restApi.equalsIgnoreCase("null") && restApi.contains("$")) {
            String[] vars = restApi.split("\\$");
            for (String var : vars) {
                //System.out.println("var : "+var);
                if (var.contains("{")) {
                    String key = var.substring(var.indexOf("{") + 1, var.lastIndexOf("}"));
                    String value = AssertManager.getReporter().getValueFromFile(var.substring(var.indexOf("{") + 1, var.lastIndexOf("}")).replace(".", "/"));
                    //System.out.println("key : " + key);
                    //System.out.println("value : " + value);

                    restApi = restApi.replaceAll("\\$\\{" + key + "}", value);

                    //restKeyValueMap.put(key,value);
                }
            }
        }

        if(restApi.contains("?")) {
            restApi = restApi.replaceAll("\\?", "%3F");
        }

        System.out.println("restApi : " + restApi);
        boolean storecookie = PropertiesUtil.getBundle().getBoolean("storecookie");
        HashMap<String, String> map = new HashMap<String, String>();

        if(storecookie) {
            try {
                Object cookieArray = PropertiesUtil.getBundle().getPropertyValue("cookie");
                System.out.println("cookieMap in request : " + cookieArray);
                //System.out.println("is a map object");
                HashMap<String, String> m = (HashMap<String, String>) cookieArray;
                if (!m.isEmpty()) {
                    map.putAll((Map<String, String>) m);
                    //given.cookies(map);
                    //System.out.println("cookieMap is not empty : "+ map);
                }
            }catch(ClassCastException e){
                System.err.println("Unable to get cookie...");
            }
        }
        Response response = null;

            if (restMethodType.toUpperCase().equalsIgnoreCase("GET")) {
                response = given.cookies(map).get(restApi);
            } else if (restMethodType.toUpperCase().equalsIgnoreCase("POST")) {
                response = given.cookies(map).post(restApi);
            } else if (restMethodType.toUpperCase().equalsIgnoreCase("PUT")) {
                response = given.cookies(map).put(restApi);
            } else if (restMethodType.toUpperCase().equalsIgnoreCase("DELETE")) {
                response = given.cookies(map).delete(restApi);
            } else {
                System.err.println("Please use valid REST Method type for Test Case : " + tcName);
                AssertManager.getReporter().addAssertionLog("Please use valid REST Method type for Test Case : " + tcName, Assertions.MessageTypes.Fail);
            }

        if (storecookie) {
            try {
                Map<String, String> cookieMap = (Map<String, String>) response.getCookies();
                HashMap<String, String> cookies = (HashMap<String, String>) PropertiesUtil.getBundle().getProperty("cookie");
                //System.out.println("size : "+cookieMap.size());
                cookies.putAll(cookieMap);
                PropertiesUtil.getBundle().setProperty("cookie", cookies);
                //System.out.println("merged cookie : " +cookies);
            }catch(ClassCastException e){
                e.printStackTrace();
            }
        }

        System.out.println("response : "+response.body().asString());
        AssertManager.getReporter().storeResponseInFile(tcName, response.body().asString());
        AssertManager.getReporter().addAssertionLog("Response Code : " + response.getStatusCode(), Assertions.MessageTypes.Info);

        // added code to validate response code.
        if(PropertiesUtil.getBundle().getBoolean("validate.response.code")){
            if(responseCode != null && !responseCode.equalsIgnoreCase("") && !responseCode.equalsIgnoreCase("null")) {
                int code = 0;
                if(responseCode.contains(".")) {
                    code = Integer.parseInt(responseCode.trim().split("\\.")[0]);
                }else{
                    code = Integer.parseInt(responseCode);
                }
                if(response.getStatusCode() == code){
                    AssertManager.getReporter().assertTrue(true, "Status code does not match.",
                            "Status code match.");
                }else{
                    AssertManager.getReporter().assertTrue(false, "Status code does not match.",
                            "Status code match.");
                }
            }
        }

        JsonElement actualRefinedResponse = removeIgnoreElements(response.body().asString());
        //System.out.println("actualRefinedResponse : "+actualRefinedResponse.toString());

        JsonParser parser = new JsonParser();

        //System.out.println("after remove element : "+response);

        if (jsonPathForValidation != null && !jsonPathForValidation.equalsIgnoreCase("null")) {

            String[] validationStringArray = jsonPathForValidation.split(",");
            String[] operatorArray = null;

            if(operatorForComparison != null && !operatorForComparison.equalsIgnoreCase("null")) {
                operatorArray = operatorForComparison.split(",");
            }

            int i = 0;
            for (String validationString : validationStringArray) {
                System.out.println("validationString : " + validationString);
                try {
                    DocumentContext documentContext = JsonPath.parse(actualRefinedResponse.toString());
                    Object lst = documentContext.read(validationString);

                    //JsonObject object = actualRefinedResponse.getAsJsonObject();

                    System.out.println("response : " + lst.toString());

                    if(expectedResponse != null && !expectedResponse.equalsIgnoreCase("null")) {

                        String operator = "=";
                        try {
                            operator = operatorArray[i].trim();
                            System.out.println("Operator : " + operator);
                        } catch (Exception e) {
                            System.out.println("No Operator assigned so using default operator : " + operator);
                        }

                        JsonArray array = parser.parse(expectedResponse).getAsJsonArray();
                        ArrayList<String> expectedResponseArray = new ArrayList<String>();
                        for (Object element : array) {
                            try {
                                JsonElement jsonElement = ((JsonElement) element);
                                JsonArray jsonArray = null;
                                if(jsonElement.toString().startsWith("[")) {
                                    jsonArray = jsonElement.getAsJsonArray();
                                    expectedResponseArray.add(jsonArray.toString());
                                }else{
                                    expectedResponseArray.add(jsonElement.getAsString());
                                }
                            }catch(Exception e){
                                try{
                                    JsonArray jsonArray = ((JsonArray) element);
                                    expectedResponseArray.add(jsonArray.getAsString());
                                }catch(Exception e1){
                                    System.err.println("Unable to convert to Json Array or Json Element.");
                                    AssertManager.getReporter().addAssertionLog("Unable to convert to Json Array or Json Element.", Assertions.MessageTypes.Fail);
                                }
                            }
                        }

                        if (storeValue != null && !storeValue.equalsIgnoreCase("null")) {
                            String[] storeValues = storeValue.split(",");
                            try {
                                //System.out.println("in try 1");
                                JsonArray storeVarJson = parser.parse(lst.toString()).getAsJsonArray();
                                //System.out.println("response : "+storeVarJson.getAsString());
                                AssertManager.getReporter().storeValueInFile(tcName + "/" + storeValues[i], storeVarJson.getAsString());
                                verifyResponse(operator, storeVarJson.getAsString(), expectedResponseArray.get(i));
                            } catch (Exception e) {
                                //System.out.println("in catch 1");
                                try {
                                    //System.out.println("in try 2");
                                    JsonObject storeJsonElement = parser.parse(lst.toString()).getAsJsonObject();
                                    //System.out.println("response : " + storeJsonElement.getAsString());
                                    AssertManager.getReporter().storeValueInFile(tcName + "/" + storeValues[i], storeJsonElement.getAsString());
                                    verifyResponse(operator, storeJsonElement.getAsString(), expectedResponseArray.get(i));
                                } catch (Exception e1) {
                                    //System.out.println("in catch 2");
                                    String value = "";
                                    if (lst.toString().startsWith("\"") && lst.toString().endsWith("\"")) {
                                        value = lst.toString().substring(1, lst.toString().length() - 1);
                                    } else {
                                        value = lst.toString();
                                    }
                                    System.out.println("value : "+value);
                                    AssertManager.getReporter().storeValueInFile(tcName + "/" + storeValues[i], value);
                                    verifyResponse(operator, value, expectedResponseArray.get(i));
                                }
                            }
                        } else {
                            try {
                                //System.out.println("in try 3 : "+lst.toString());
                                JsonArray storeVarJson = parser.parse(lst.toString()).getAsJsonArray();
                                //System.out.println("response : "+storeVarJson.getAsString());
                                verifyResponse(operator, storeVarJson.getAsString(), expectedResponseArray.get(i));
                            } catch (Exception e) {
                                //System.out.println("in catch 3");
                                try {
                                    //System.out.println("in try 4");
                                    JsonObject storeJsonElement = parser.parse(lst.toString()).getAsJsonObject();
                                    //System.out.println("response : " + storeJsonElement.getAsString());
                                    verifyResponse(operator, storeJsonElement.getAsString(), expectedResponseArray.get(i));
                                } catch (Exception e1) {
                                    //System.out.println("in catch 4");
                                    String value = "";
                                    if (lst.toString().startsWith("\"") && lst.toString().endsWith("\"")) {
                                        //System.out.println("in if");
                                        value = lst.toString().substring(1, lst.toString().length() - 1);
                                    } else {
                                        if(lst.toString().startsWith("[") && lst.toString().length() == 2){
                                            //System.out.println("in if size : "+lst.toString().length());
                                            value = "";
                                        }else{
                                            //System.out.println("in if size : "+lst.toString().length());
                                            value = lst.toString();
                                        }
                                    }
                                    System.out.println("actual : "+value);
                                    System.out.println("expected : "+expectedResponseArray.get(i));
                                    verifyResponse(operator, value, expectedResponseArray.get(i));
                                }
                            }
                        }
                    }else{
                        if (storeValue != null && !storeValue.equalsIgnoreCase("null")) {
                            String[] storeValues = storeValue.split(",");
                            try {
                                //System.out.println("in try 1");
                                JsonArray storeVarJson = parser.parse(lst.toString()).getAsJsonArray();
                                //System.out.println("response : "+storeVarJson.getAsString());
                                AssertManager.getReporter().storeValueInFile(tcName + "/" + storeValues[i], storeVarJson.getAsString());
                            } catch (Exception e) {
                                //System.out.println("in catch 1");
                                try {
                                    //System.out.println("in try 2");
                                    JsonObject storeJsonElement = parser.parse(lst.toString()).getAsJsonObject();
                                    //System.out.println("response : " + storeJsonElement.getAsString());
                                    AssertManager.getReporter().storeValueInFile(tcName + "/" + storeValues[i], storeJsonElement.getAsString());
                                } catch (Exception e1) {
                                    //System.out.println("in catch 2");
                                    String value = "";
                                    if (lst.toString().startsWith("\"") && lst.toString().endsWith("\"")) {
                                        value = lst.toString().substring(1, lst.toString().length() - 1);
                                    } else {
                                        if(lst.toString().startsWith("[") && lst.toString().length() == 2){
                                            value = "";
                                        }else{
                                            value = lst.toString();
                                        }
                                    }
                                    //System.out.println("value : "+value);
                                    AssertManager.getReporter().storeValueInFile(tcName + "/" + storeValues[i], value);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    AssertManager.getReporter().assertTrue(false, "Unable to store response string to variable or couldn't received response.", "");
                    //throw new AssertionError("Unable to store validation response string to variable");
                }
                i++;
            }
        } else {
            Object actualJsonObject = null;
            Object expectedJsonObject = removeIgnoreElements(expectedResponse);

            if(response.getStatusCode() == 200 || response.getStatusCode() == 204) {
                if (!response.body().asString().equalsIgnoreCase("")) {
                    if (expectedResponse != null && !expectedResponse.equalsIgnoreCase("null")) {
                        try {
                            actualJsonObject = parser.parse(actualRefinedResponse.toString()).getAsJsonObject();
                        } catch (Exception e) {
                            try {
                                actualJsonObject = parser.parse(actualRefinedResponse.toString()).getAsJsonArray();
                            }catch(Exception e1){
                                AssertManager.getReporter().addAssertionLog("Unable to convert actual response into JSON Object OR JSON Array.", Assertions.MessageTypes.Fail);
                            }
                        }

                        try {
                            expectedJsonObject = parser.parse(expectedJsonObject.toString()).getAsJsonObject();
                        } catch (Exception e) {
                            try {
                                expectedJsonObject = parser.parse(expectedJsonObject.toString()).getAsJsonArray();
                            }catch(Exception e1){
                                AssertManager.getReporter().addAssertionLog("Unable to convert expected response into JSON Object OR JSON Array.", Assertions.MessageTypes.Fail);
                            }
                        }

                        //System.out.println("actual : "+actualJsonObject.toString());
                        //System.out.println("expected : "+expectedJsonObject.toString());
                        try {
                            AssertManager.getReporter().assertTrue(actualJsonObject.equals(expectedJsonObject), "Expected and Actual Responses should be same.", "Expected and Actual Responses are same.");
                        } catch (NullPointerException e1) {
                            AssertManager.getReporter().addAssertionLog("Expected response should not be empty.", Assertions.MessageTypes.Fail);
                            System.err.println("Expected response should not be empty.");
                        }
                    } else {
                        AssertManager.getReporter().addAssertionLog("Expected and Actual Responses are same.", Assertions.MessageTypes.Pass);
                    }
                }
            }else{
                AssertManager.getReporter().assertTrue(false,"Response Code is "+response.getStatusCode(), "");
            }
        }

    }

    public void verifyResponse(String operator, Object actualValue, String expectedResponse) {
        //System.out.println("operator : "+operator);
        System.out.println("expectedResponse : "+expectedResponse);

        if (expectedResponse != null && !expectedResponse.equalsIgnoreCase("null") && expectedResponse.contains("${")) {
            String[] vars = expectedResponse.split("\\$");
            for (String var : vars) {
                //System.out.println("var : "+var);
                if (var.contains("{JS.")) {
                    String utilityFunction = var.substring(var.indexOf("{") + 4, var.lastIndexOf("}"));
                    //System.out.println("key : " + utilityFunction);
                    String value = "";

                    try {
                        if (utilityFunction.contains(".")) {
                            String callingJSFile =utilityFunction.split("\\.")[0];
                            String callingFunction =utilityFunction.split("\\.")[1].substring(0, utilityFunction.split("\\.")[1].indexOf("("));
                            value = JavaScriptUtility.runJavaScript(callingJSFile, callingFunction);
                        }
                    }catch(Exception e){
                        System.err.println("Unable to execute JS function.");
                        AssertManager.getReporter().addAssertionLog("Unable to execute JS function.", Assertions.MessageTypes.Fail);
                        value = "";
                    }
                    //System.out.println("value : " + value);

                    String temp = utilityFunction.replaceAll("\\(","\\\\(").replaceAll("\\)","\\\\)");

                    //System.out.println("utilityFunction : "+temp);
                    expectedResponse = expectedResponse.replaceAll("\\$\\{JS\\." + temp + "\\}", value);
                }
            }
        }

        //System.out.println("expectedResponse : "+expectedResponse);



        if (operator != null && operator.equalsIgnoreCase("=")) {
            //System.out.println("in equals");
            if (actualValue instanceof String) {
                //System.out.println("in string");
                AssertManager.getReporter().assertEquals(String.valueOf(actualValue), expectedResponse, "value");
            } else if (actualValue instanceof List) {
                System.out.println("in list");
                String[] expectedArray = expectedResponse.split(",");
                List<String> expectedList = Arrays.asList(expectedArray);
                List<String> actualList = (List<String>) actualValue;
                AssertManager.getReporter().assertEquals(actualList.toString(), expectedList.toString(), "value");
            }
        } else if (operator != null && operator.equalsIgnoreCase("!=")) {
            //System.out.println("in not equals");
            if (actualValue instanceof String) {
                //System.out.println("in if ");
                AssertManager.getReporter().assertNotEquals(String.valueOf(actualValue), expectedResponse, "value");
            } else if (actualValue instanceof List) {
                //System.out.println("in else if ");
                String[] expectedArray = expectedResponse.split(",");
                List<String> expectedList = Arrays.asList(expectedArray);
                List<String> actualList = (List<String>) actualValue;
                AssertManager.getReporter().assertNotEquals(actualList.toString(), expectedList.toString(), "value");
            }
        } else if (operator != null && operator.equalsIgnoreCase("~")) {
            //System.out.println("in contains");
            if (actualValue instanceof String) {
                //System.out.println("actual : "+String.valueOf(actualValue));
                //System.out.println("expected : "+expectedResponse);
                AssertManager.getReporter().assertContains(String.valueOf(actualValue), expectedResponse, "value");
            } else if (actualValue instanceof List) {
                String[] expectedArray = expectedResponse.split(",");
                List<String> expectedList = Arrays.asList(expectedArray);
                List<String> actualList = (List<String>) actualValue;
                AssertManager.getReporter().assertContains(actualList, expectedList, "value");
            }
        } else if (operator != null && operator.equalsIgnoreCase("!~")) {
            //System.out.println("in not contains");
            if (actualValue instanceof String) {
                AssertManager.getReporter().assertNotContains(String.valueOf(actualValue), expectedResponse, "value");
            } else if (actualValue instanceof List) {
                String[] expectedArray = expectedResponse.split(",");
                List<String> expectedList = Arrays.asList(expectedArray);
                List<String> actualList = (List<String>) actualValue;
                AssertManager.getReporter().assertNotContains(actualList.toString(), expectedList.toString(), "value");
            }
        } else if (operator != null && operator.equalsIgnoreCase(">")) {
            //System.out.println("in >");
            AssertManager.getReporter().assertGreaterThan(actualValue, expectedResponse, "value");
        } else if (operator != null && operator.equalsIgnoreCase(">=")) {
            //System.out.println("in >=");
            AssertManager.getReporter().assertGreaterOrEqual(actualValue, expectedResponse, "value");
        } else if (operator != null && operator.equalsIgnoreCase("<")) {
            //System.out.println("in <");
            AssertManager.getReporter().assertLessThan(actualValue, expectedResponse, "value");
        } else if (operator != null && operator.equalsIgnoreCase("<=")) {
            //System.out.println("in <=");
            AssertManager.getReporter().assertLessOrEqual(actualValue, expectedResponse, "value");
        }
    }


    public JsonElement removeIgnoreElements(String response){

        JsonParser parser = new JsonParser();
        JsonElement refinedResponse = null;

        String[] ignoreElements = PropertiesUtil.getBundle().getStringArray("ignoreelements");

        try {
            //System.out.println("convert response to jsonArray");
            refinedResponse = parser.parse(response).getAsJsonArray();
        }catch(Exception e){
            try {
                //System.out.println("convert response to jsonObject");
                refinedResponse = parser.parse(response).getAsJsonObject();
            }
            catch(Exception e1){
                System.err.println("Unable to caste response object to JsonObject.");
            }
        }
        if(refinedResponse instanceof JsonObject){
            for(String ele : ignoreElements) {
                ((JsonObject) refinedResponse).remove(ele);
            }
            //System.out.println("refined response : "+refinedResponse.toString());
        }else if(refinedResponse instanceof JsonArray){
            JsonArray array = (JsonArray) refinedResponse;
            for(JsonElement ele : array){
                JsonObject obj = ele.getAsJsonObject();
                for(String ignoreElement : ignoreElements) {
                    if (obj.get(ignoreElement) != null) {
                        obj.remove(ignoreElement);
                    }
                }
            }
            refinedResponse = array;
        }else{
            System.err.println("Unable to caste response object to JsonObject.");
        }

        return refinedResponse;
    }
}
