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
    private String urlPrefix; // /album/* (서명 필요)
    private String publicUrlPrefix; // /public/* 기본/공용 이미지용 (서명 필요 없음)
    private String defaultCoverFilename; // 앨범 커버 기본 이미지 파일명 (public-url-prefix + 파일명 = 기본 커버 URL)
}
