package com.momentry.BE.domain.user.exception;

import com.momentry.BE.global.exception.BusinessException;

public class DuplicateUserException extends BusinessException {
    public DuplicateUserException() {
        super("이미 존재하는 닉네임입니다.", 400);
    }
}
