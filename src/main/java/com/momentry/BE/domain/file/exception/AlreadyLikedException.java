package com.momentry.BE.domain.file.exception;

import com.momentry.BE.global.exception.BusinessException;

public class AlreadyLikedException extends BusinessException {
    public AlreadyLikedException() {
        super("이미 좋아요를 누른 파일입니다.", 400);
    }
}
