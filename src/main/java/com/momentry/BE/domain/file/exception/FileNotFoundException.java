package com.momentry.BE.domain.file.exception;

import com.momentry.BE.global.exception.BusinessException;

public class FileNotFoundException extends BusinessException {
    public FileNotFoundException() {
        super("해당하는 파일이 존재하지 않습니다.", 404);
    }
}
