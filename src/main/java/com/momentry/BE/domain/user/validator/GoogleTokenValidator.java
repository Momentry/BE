package com.momentry.BE.domain.user.validator;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.momentry.BE.domain.user.dto.OidcClaims;
import com.momentry.BE.domain.user.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Slf4j
@Component("google")
@RequiredArgsConstructor
public class GoogleTokenValidator implements OauthValidator {

    private final HttpTransport httpTransport;
    private final JsonFactory jsonFactory;
    
    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Override
    public OidcClaims validate(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                throw new InvalidTokenException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            
            // 추가 검증: issuer 확인
            String issuer = payload.getIssuer();
            if (!("accounts.google.com".equals(issuer) || "https://accounts.google.com".equals(issuer))) {
                throw new InvalidTokenException("Invalid issuer: " + issuer);
            }

            log.debug("Google ID Token validated successfully for user: {}", payload.getEmail());

            return OidcClaims.builder()
                    .sub(payload.getSubject())
                    .email(payload.getEmail())
                    .name((String) payload.get("name"))
                    .picture((String) payload.get("picture"))
                    .build();

        } catch (GeneralSecurityException | IOException e) {
            log.error("Failed to validate Google ID token : ", e);
            throw new InvalidTokenException("Google ID token validation failed");
        }
    }
}
