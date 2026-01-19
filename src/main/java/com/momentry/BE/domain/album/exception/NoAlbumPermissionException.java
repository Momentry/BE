package com.momentry.BE.domain.album.exception;

import com.momentry.BE.global.exception.BusinessException;

public class NoAlbumPermissionException extends BusinessException {
    public NoAlbumPermissionException() {
        super("앨범 접근 권한이 없습니다.", 403);
    }
}
