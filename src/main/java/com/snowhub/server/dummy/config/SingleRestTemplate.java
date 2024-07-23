package com.snowhub.server.dummy.config;

import org.springframework.web.client.RestTemplate;

public class SingleRestTemplate { // 몇번 안쓰는데 굳이 빈에 등록???

    private static RestTemplate restTemplate;

    private SingleRestTemplate(){

    }

    public static RestTemplate getInstance(){
        if (restTemplate==null){
            restTemplate = new RestTemplate();
            return restTemplate;
        }
        return restTemplate;
    }
}
