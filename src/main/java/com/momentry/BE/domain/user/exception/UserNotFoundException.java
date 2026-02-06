package com.momentry.BE.domain.user.exception;

import com.momentry.BE.global.exception.BusinessException;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super("존재하지 않는 사용자입니다.", 400);
    }
}
