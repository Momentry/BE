package com.momentry.BE.domain.album.exception;

import com.momentry.BE.global.exception.BusinessException;

public class DuplicateAlbumNameException extends BusinessException {
    public DuplicateAlbumNameException() {
        super("이미 존재하는 앨범 이름입니다.", 409);
    }
}
