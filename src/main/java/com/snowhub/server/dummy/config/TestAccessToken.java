package com.snowhub.server.dummy.config;

public class TestAccessToken {
    private static TestAccessToken accessToken;
    private static String val;

    private TestAccessToken(){

    }

    public static TestAccessToken getInstance(){
        if(accessToken==null){
            accessToken = new TestAccessToken();
            return accessToken;
        }
        return accessToken;
    }

    public void setVal(String value){
        val = value;
    }
    public String getVal(){
        return val;
    }
}
