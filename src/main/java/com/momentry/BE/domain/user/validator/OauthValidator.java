package com.momentry.BE.domain.user.validator;

import com.momentry.BE.domain.user.dto.OidcClaims;

public interface OauthValidator {
    public OidcClaims validate(String idToken);
}
