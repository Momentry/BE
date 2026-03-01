package com.momentry.BE.domain.user.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Provider {
    GOOGLE,
    APPLE;

    @JsonCreator
    public static Provider from(String value) {
        return Provider.valueOf(value.toUpperCase());
    }
}
