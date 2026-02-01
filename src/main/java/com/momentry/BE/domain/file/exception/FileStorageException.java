package com.momentry.BE.domain.file.exception;

import com.momentry.BE.global.exception.BusinessException;

public class FileStorageException extends BusinessException {
    public FileStorageException() {
        super("파일 업로드 도중 알 수 없는 에러가 발생했습니다.", 500);
    }
}
