package com.momentry.BE.domain.album.exception;

import com.momentry.BE.global.exception.BusinessException;

public class AlbumCoverUploadException extends BusinessException {
    public AlbumCoverUploadException() {
        super("앨범 커버 업로드 중 에러가 발생했습니다.", 500);
    }
    public AlbumCoverUploadException(String message) {
        super(message, 500);
    }
}
