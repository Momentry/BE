package com.momentry.BE.domain.user.exception;

import com.momentry.BE.global.exception.BusinessException;

public class TokenNotFoundException extends BusinessException {
    public TokenNotFoundException() {
        super("토큰이 존재하지 않습니다", 401);
    }
}
