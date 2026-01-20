package com.momentry.BE.global.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.momentry.BE.global.config.CloudFrontProperties;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCustomPolicy;
import software.amazon.awssdk.services.cloudfront.model.CustomSignerRequest;


/**
 * CloudFront Signed Cookie 서비스
 * 클라우드프론트를 통해 S3에 권한 검사 없이 접근하기 위한 쿠키를 발급하는 서비스.
 * 
 * @author koojawon
 * @since 2026-01-19
 * @version 1.0.0
 * @see <a href="https://docs.aws.amazon.com/cloudfront/latest/APIReference/API_CreateCloudFrontOriginAccessIdentity.html">CreateCloudFrontOriginAccessIdentity</a>
 * @see <a href="https://docs.aws.amazon.com/cloudfront/latest/APIReference/API_CreateCloudFrontOriginAccessIdentity.html">CreateCloudFrontOriginAccessIdentity</a>
 */
@Service
@RequiredArgsConstructor
public class CloudFrontSignedCookieService {

    private final CloudFrontProperties properties;
    private volatile PrivateKey cachedPrivateKey;
    private final CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();

    public HttpHeaders buildSignedCookieHeaders(Long albumId) {
        HttpHeaders headers = new HttpHeaders();
        if (!properties.isEnabled()) {
            return headers;
        }

        validateConfig();

        Instant expiresAt = Instant.now().plusSeconds(properties.getCookieTtlSeconds());
        CustomSignerRequest request = CustomSignerRequest.builder()
                .resourceUrl(resolveResourceUrl(albumId))
                .privateKey(loadPrivateKey())
                .keyPairId(properties.getKeyPairId())
                .expirationDate(expiresAt)
                .build();
        CookiesForCustomPolicy cookies = cloudFrontUtilities.getCookiesForCustomPolicy(request);

        headers.add(HttpHeaders.SET_COOKIE, createCookie("CloudFront-Policy", cookies.policyHeaderValue()).toString());
        headers.add(HttpHeaders.SET_COOKIE, createCookie("CloudFront-Signature", cookies.signatureHeaderValue()).toString());
        headers.add(HttpHeaders.SET_COOKIE, createCookie("CloudFront-Key-Pair-Id", cookies.keyPairIdHeaderValue()).toString());

        return headers;
    }

    private void validateConfig() {
        if (!StringUtils.hasText(properties.getKeyPairId())
                || !StringUtils.hasText(properties.getPrivateKey())
                || !StringUtils.hasText(properties.getResourceUrl())) {
            throw new IllegalStateException("CloudFront 설정이 누락되었습니다. (keyPairId/privateKey/resourceUrl)");
        }
    }

    private String resolveResourceUrl(Long albumId) {
        String resourceUrl = properties.getResourceUrl();
        if (albumId == null) {
            return resourceUrl;
        }
        return resourceUrl.replace("{albumId}", albumId.toString());
    }

    private PrivateKey loadPrivateKey() {
        if (cachedPrivateKey != null) {
            return cachedPrivateKey;
        }

        synchronized (this) {
            if (cachedPrivateKey == null) {
                cachedPrivateKey = parsePrivateKey(normalizePem(properties.getPrivateKey()));
            }
        }
        return cachedPrivateKey;
    }

    private PrivateKey parsePrivateKey(String pem) {
        try {
            String sanitized = pem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(sanitized.getBytes(StandardCharsets.UTF_8));
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (IllegalArgumentException | GeneralSecurityException e) {
            throw new IllegalStateException("PrivateKey는 PKCS#8 PEM 형식이어야 합니다.", e);
        }
    }

    private String normalizePem(String pem) {
        return pem.replace("\\n", "\n").trim();
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
