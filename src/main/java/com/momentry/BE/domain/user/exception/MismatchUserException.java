package com.momentry.BE.domain.user.exception;

import com.momentry.BE.global.exception.BusinessException;

public class MismatchUserException extends BusinessException {
    public MismatchUserException() {
        super("요청한 사용자와 토큰 소유자가 다릅니다.", 403);
    }
}
