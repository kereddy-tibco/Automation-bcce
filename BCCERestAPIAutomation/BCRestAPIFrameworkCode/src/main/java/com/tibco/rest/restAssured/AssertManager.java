package com.tibco.rest.restAssured;

public class AssertManager {

    private static ThreadLocal<Assertions> assertThreadLocal = new ThreadLocal<Assertions>();

    public static Assertions getReporter() {
        if(assertThreadLocal.get() == null){
            Assertions restAssert = new Assertions();
            setReporter(restAssert);
        }
        return assertThreadLocal.get();
    }

    public static void setReporter(Assertions restAssert){
        assertThreadLocal.set(restAssert);
    }
}
