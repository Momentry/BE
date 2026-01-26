package com.momentry.BE.domain.user.exception;

import com.momentry.BE.global.exception.BusinessException;

public class NotSupportedProviderException extends BusinessException {
    public NotSupportedProviderException() {
        super("지원하지 않는 소셜 로그인 방식입니다.", 400);
    }
}
