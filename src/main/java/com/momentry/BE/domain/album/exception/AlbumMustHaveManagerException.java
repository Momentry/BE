package com.momentry.BE.domain.album.exception;

import com.momentry.BE.global.exception.BusinessException;

public class AlbumMustHaveManagerException extends BusinessException {
    public AlbumMustHaveManagerException() {
        super("다른 멤버에게 MANAGER 권한 위임이 필요합니다", 409);
    }
}