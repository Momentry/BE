package com.momentry.BE.global.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.momentry.BE.global.config.CloudFrontProperties;

import lombok.RequiredArgsConstructor;

/**
 * CloudFront Custom Auth Cookie 서비스 (CloudFront Function 연동용)
 * 사용자 정의 쿠키를 발급하여 다중 앨범 접근 권한을 제어함.
 */
@Service
@RequiredArgsConstructor
public class CloudFrontSignedCookieService {

    private final CloudFrontProperties properties;
    
    // yml에서 관리: cloudfront.secret-key (32자 이상 강력한 문자열 권장)
    @Value("${cloudfront.secret-key}")
    private String secretKey; 

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String COOKIE_NAME = "momentry_album_access";

    public HttpHeaders buildSignedCookieHeaders(String userId, List<Long> allowedAlbumIds) {
        HttpHeaders headers = new HttpHeaders();
        
        if (!properties.isEnabled()) {
            return headers;
        }

        String cookieValue = generateCookieValue(userId, allowedAlbumIds);
        headers.add(HttpHeaders.SET_COOKIE, createCookie(COOKIE_NAME, cookieValue).toString());

        return headers;
    }

    private String generateCookieValue(String userId, List<Long> albumIds) {
        long exp = System.currentTimeMillis() / 1000 + properties.getCookieTtlSeconds();
        
        String albumsStr = albumIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String data = String.format("%s:%d:%s", userId, exp, albumsStr);

        String signature = sign(data);

        return data + ":" + signature;
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign cloudfront cookie", e);
        }
    }

    private ResponseCookie createCookie(String name, String value) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .path(properties.getCookiePath())
                .maxAge(Duration.ofSeconds(properties.getCookieTtlSeconds()))
                .secure(properties.isSecure())
                .httpOnly(properties.isHttpOnly());

        if (StringUtils.hasText(properties.getCookieDomain())) {
            builder.domain(properties.getCookieDomain());
        }
        if (StringUtils.hasText(properties.getSameSite())) {
            builder.sameSite(properties.getSameSite());
        }

        return builder.build();
    }
}
