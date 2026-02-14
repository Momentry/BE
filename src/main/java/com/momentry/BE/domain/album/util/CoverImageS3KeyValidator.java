package com.momentry.BE.domain.album.util;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 앨범 커버 S3 키 형식 검사 및 폴더명 제공.
 * 형식: {albumId}/{cover-image}/{filename}
 */
@Component
public class CoverImageS3KeyValidator {

    @Value("${app.s3.cover-image-folder:cover-image/}")
    private String coverImageFolder;

    // S3 키가 아닌 값(URL, 이상한 문자열)은 false 반환
    public boolean isS3KeyFormat(String value) {
        if (value == null || value.isBlank())
            return false;
        return Pattern.matches("\\d+/" + Pattern.quote(coverImageFolder) + ".+", value);
    }

    public String getCoverImageFolder() {
        return coverImageFolder;
    }
}
