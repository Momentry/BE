package com.momentry.BE.domain.file.exception;

import com.momentry.BE.global.exception.BusinessException;

public class InvalidFileTypeException extends BusinessException {
    public InvalidFileTypeException() {
        super("허용되지 않는 파일 확장자거나 형식입니다.", 400);
    }
}
