package com.momentry.BE.domain.user.validator;

import com.momentry.BE.domain.user.dto.OidcClaims;
import com.momentry.BE.domain.user.exception.InvalidTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.JWTClaimsSet;

import java.net.URI;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;


@Slf4j
@Component("apple")
public class AppleTokenValidator implements OauthValidator {
    @Value("${oauth.apple.client-id}")
    private String appleClientId;

    @Value("${oauth.apple.issuer}")
    private String appleIssuer;

    private static final URI APPLE_PUBLIC_KEY_URI = URI.create("https://appleid.apple.com/auth/keys");

    @Override
    public OidcClaims validate(String idToken) {
        // Apple ID Token 검증 로직
        try {
            SignedJWT signedJWT = SignedJWT.parse(idToken);

            // 1. kid 추출
            String kid = signedJWT.getHeader().getKeyID();

            // 2. Apple 공개키 조회
            // TODO: 성능 개선을 위해 jwkSet값을 캐싱해서 사용하기
            JWKSet jwkSet = JWKSet.load(APPLE_PUBLIC_KEY_URI.toURL());

            JWK jwk = jwkSet.getKeyByKeyId(kid);
            if (jwk == null) {
                throw new InvalidTokenException("유효하지 않은 Apple ID 입니다.");
            }

            RSAKey rsaKey = (RSAKey) jwk;
            RSAPublicKey publicKey = rsaKey.toRSAPublicKey();

            // 3. 서명 검증
            JWSVerifier verifier = new RSASSAVerifier(publicKey);

            if (!signedJWT.verify(verifier)) {
                throw new InvalidTokenException("Invalid Apple signature");
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            // 4. issuer 검증
            if (!appleIssuer.equals(claims.getIssuer())) {
                throw new InvalidTokenException("Invalid issuer");
            }

            // 5. audience 검증
            if (!claims.getAudience().contains(appleClientId)) {
                throw new InvalidTokenException("Invalid audience");
            }

            // 6. 만료 체크
            if (claims.getExpirationTime().before(new Date())) {
                throw new InvalidTokenException("Token expired");
            }

            log.debug("Apple ID Token validated successfully for user: {}",
                    claims.getSubject());

            // name 추출
            String name = claims.getStringClaim("name");
            if (name == null || name.isBlank()) {
                name = "AppleUser_" + claims.getSubject().substring(0, 6);
            }

            return OidcClaims.builder()
                    .sub(claims.getSubject())
                    .email(claims.getStringClaim("email"))
                    .name(name)
                    .build();

        } catch (Exception e) {
            log.error("Failed to validate Apple ID token", e);
            throw new InvalidTokenException("Apple ID token validation failed");
        }
    }
}