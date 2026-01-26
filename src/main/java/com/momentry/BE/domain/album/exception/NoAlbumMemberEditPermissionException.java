package com.momentry.BE.domain.album.exception;

import com.momentry.BE.global.exception.BusinessException;

public class NoAlbumMemberEditPermissionException extends BusinessException {
	public NoAlbumMemberEditPermissionException() {
		super("멤버 권한을 관리할 권한이 없습니다.", 403);
	}
}
