package com.momentry.BE.domain.file.exception;

import com.momentry.BE.global.exception.BusinessException;

public class InvalidFileSizeException extends BusinessException {
    public InvalidFileSizeException() {
        super("파일 크기 정보가 올바르지 않습니다.", 400);
    }
}
