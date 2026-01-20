package com.momentry.BE.domain.album.exception;

import com.momentry.BE.global.exception.BusinessException;

public class NoAlbumEditPermissionException extends BusinessException {
    public NoAlbumEditPermissionException() {
        super("앨범 수정 권한이 없습니다.", 403);
    }
}
