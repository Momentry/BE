package com.momentry.BE.domain.album.exception;

import com.momentry.BE.global.exception.BusinessException;

public class TagNotFoundException extends BusinessException {
    public TagNotFoundException() {
        super("태그를 찾을 수 없습니다.", 404);
    }
}
