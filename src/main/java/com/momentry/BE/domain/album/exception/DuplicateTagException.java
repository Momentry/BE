package com.momentry.BE.domain.album.exception;

import com.momentry.BE.global.exception.BusinessException;

public class DuplicateTagException extends BusinessException {
    public DuplicateTagException() {
        super("이미 존재하는 태그입니다.", 409);
    }
}
