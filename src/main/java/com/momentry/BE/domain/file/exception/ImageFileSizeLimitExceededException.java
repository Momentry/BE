package com.momentry.BE.domain.file.exception;

import com.momentry.BE.global.exception.BusinessException;

public class ImageFileSizeLimitExceededException extends BusinessException {
    public ImageFileSizeLimitExceededException() {
        super("이미지 파일은 최대 10MB까지 업로드할 수 있습니다.", 400);
    }
}
