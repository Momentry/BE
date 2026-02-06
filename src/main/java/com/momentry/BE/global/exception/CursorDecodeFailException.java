package com.momentry.BE.global.exception;

public class CursorDecodeFailException extends BusinessException{
    public CursorDecodeFailException() {
        super("cursor 형식이 올바르지 않습니다.", 400);
    }
}
