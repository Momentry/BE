package com.momentry.BE.domain.user.validator;

import com.momentry.BE.domain.user.dto.OidcClaims;
import com.momentry.BE.domain.user.enums.Provider;
import com.momentry.BE.domain.user.exception.NotSupportedProviderException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class IdTokenValidator {
    private final Map<String, OauthValidator> validators;

    public OidcClaims validateToken(Provider provider, String idToken) {
        OauthValidator validator = validators.get(provider.name().toLowerCase());
        if (validator == null) {
            throw new NotSupportedProviderException();
        }
        return validator.validate(idToken);
    }
}
