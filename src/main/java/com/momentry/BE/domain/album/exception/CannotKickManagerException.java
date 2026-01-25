package com.momentry.BE.domain.album.exception;

import com.momentry.BE.global.exception.BusinessException;

public class CannotKickManagerException extends BusinessException {
	public CannotKickManagerException() {
		super("매니저는 다른 매니저를 강퇴할 수 없습니다.", 403);
	}
}

