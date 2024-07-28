package com.snowhub.server.dummy.module;

import org.springframework.web.reactive.function.client.WebClient;

public class CustomHttpClient {

    private CustomHttpClient(){

    }

    private static class SingleInstanceHolder {
        private static final WebClient webClient = WebClient.create();
    }

    public static WebClient getInstance() {
        return SingleInstanceHolder.webClient;
    }

}
