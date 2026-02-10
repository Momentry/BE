package com.momentry.BE.domain.album.exception;

import com.momentry.BE.global.exception.BusinessException;

public class TagLimitExceededException extends BusinessException {
    public TagLimitExceededException() {
        super("앨범당 최대 태그 개수를 초과했습니다.", 409);
    }
}
