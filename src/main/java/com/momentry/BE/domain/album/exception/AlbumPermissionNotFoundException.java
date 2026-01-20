package com.momentry.BE.domain.album.exception;

import com.momentry.BE.global.exception.BusinessException;

public class AlbumPermissionNotFoundException extends BusinessException {
	public AlbumPermissionNotFoundException() {
		super("앨범 권한을 확인할 수 없습니다.", 404);
	}
}