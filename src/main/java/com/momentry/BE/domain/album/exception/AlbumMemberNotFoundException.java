package com.momentry.BE.domain.album.exception;

import com.momentry.BE.global.exception.BusinessException;

public class AlbumMemberNotFoundException extends BusinessException {
    public AlbumMemberNotFoundException() {
        super("앨범 멤버를 찾을 수 없습니다.", 404);
    }
}
