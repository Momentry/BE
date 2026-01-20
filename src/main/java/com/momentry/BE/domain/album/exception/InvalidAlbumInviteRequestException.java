package com.momentry.BE.domain.album.exception;

import com.momentry.BE.global.exception.BusinessException;

public class InvalidAlbumInviteRequestException extends BusinessException {
    public InvalidAlbumInviteRequestException() {
        super("비정상적인 초대 요청입니다.", 400);
    }
}
