package com.momentry.BE.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "cloudfront")
public class CloudFrontProperties {
    private boolean enabled = false;
    private String keyPairId;
    private String privateKey;
    private String resourceUrl;
    private String cookieDomain;
    private String cookiePath = "/";
    private long cookieTtlSeconds = 3600;
    private boolean secure = true;
    private boolean httpOnly = true;
    private String sameSite = "None";
}
