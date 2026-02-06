package com.momentry.BE.domain.album.exception;

import com.momentry.BE.global.exception.BusinessException;

public class InvalidTagException extends BusinessException {
    public InvalidTagException() {
        super("요청에 잘못된 태그 아이디가 포함되어있습니다.", 400);
    }
}
