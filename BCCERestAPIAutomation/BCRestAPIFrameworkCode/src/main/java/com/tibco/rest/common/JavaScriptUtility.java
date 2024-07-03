package com.tibco.rest.common;

import groovy.util.ScriptException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

/**
 * Load Script File Example
 *
 * @author Krishna
 *
 */
public class JavaScriptUtility
{

    public static String runJavaScript(String fileName, String functionName){
        // Create ScriptEngineManager
        ScriptEngineManager engineManager = new ScriptEngineManager();

        // Create ScriptEngine
        ScriptEngine engine = engineManager.getEngineByName("ECMAScript");

        //Create file and reader instance for reading the script file
        File file = new File("scripts/"+fileName+".js");
        System.out.println("exists : "+file.exists());
        Reader reader = null;
        try {
            reader = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //Pass the script file to the engine
        Object result = null;
        try {
            engine.eval(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("Java Program Output : " + result);
        //Create invocable instance
        Invocable invocable = (Invocable) engine;

        //Invoke the methods defined in the script file
        try {
            result = invocable.invokeFunction(functionName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Running javascript file : "+fileName+".js, Function : "+functionName+", return value : "+result);

        if(result != null && result.toString() != ""){
            return result.toString();
        }else{
            return  "";
        }
    }

    public static void main(String[] args){
        JavaScriptUtility.runJavaScript("Utility","getCurrentDate_New");
    }
}
