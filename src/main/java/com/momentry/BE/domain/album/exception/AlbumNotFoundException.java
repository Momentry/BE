package com.momentry.BE.domain.album.exception;

import com.momentry.BE.global.exception.BusinessException;

public class AlbumNotFoundException extends BusinessException {
    public AlbumNotFoundException() {
        super("앨범을 찾을 수 없습니다.", 404);
    }
}