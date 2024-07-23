package com.snowhub.server.dummy.config.SupplierConfig;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties("kakao")
public class KakaoConfig {
    private final String Nativekey;
    private final String RestApiKey;
    private final String JavascriptKey;
    private final String AdminKey;
    private final String ClientSecret;
    private final String RedirectUri;
}
