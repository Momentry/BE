package com.momentry.BE.global.exception;

public class FcmTokenEmptyException extends BusinessException {
    public FcmTokenEmptyException() {
        super("FCM 토큰 리스트가 비어 있습니다.", 400);
    }
}


