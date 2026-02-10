package com.momentry.BE.global.exception;

public class AccessDeniedException extends BusinessException {
    public AccessDeniedException() {
        super("해당 리소스에 대한 접근 권한이 없습니다.", 403);
    }
}