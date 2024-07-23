package com.snowhub.server.dummy.config.SupplierConfig;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@AllArgsConstructor
@Getter
@ConfigurationProperties("google")
public class GoogleConfig2 {
    private final String ClientId;
    private final String ClientSecret;
    private final String RedirectUri;
}

