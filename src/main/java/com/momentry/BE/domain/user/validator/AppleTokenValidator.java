package com.momentry.BE.domain.user.validator;

import com.momentry.BE.domain.user.dto.OidcClaims;
import org.springframework.stereotype.Component;

@Component("apple")
public class AppleTokenValidator implements OauthValidator {

    @Override
    public OidcClaims validate(String idToken) {
        // Apple ID Token 검증 로직 구현
        // TODO: Apple 공개키로 JWT 서명 검증, claims 추출
        return new OidcClaims();
    }
}