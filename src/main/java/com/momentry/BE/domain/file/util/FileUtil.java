package com.momentry.BE.domain.file.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class FileUtil {

    private static final Map<String, String> MIME_TYPE_MAP = Map.of(
            "image/jpeg", ".jpg",
            "image/jpg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif",
            "image/webp", ".webp",
            "video/mp4", ".mp4",
            "video/quicktime", ".mov",
            "video/x-msvideo", ".avi");

    // 커버 이미지용 — 정적 이미지 한 장만. GIF(여러 프레임), WebP(애니 가능) 제외
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png",
            "image/heic", "image/heif");

    public String getExtension(String contentType) {
        if (contentType == null)
            return "";

        // Map에서 찾고, 없으면 기본값으로 빈 문자열 혹은 예외 처리
        return MIME_TYPE_MAP.getOrDefault(contentType.toLowerCase(), "");
    }

    // contentType이 허용된 이미지 타입인지 검사. 커버 이미지 등 이미지 전용 업로드 시 사용.
    public boolean isAllowedCoverImageType(String contentType) {
        if (contentType == null)
            return false;
        return ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase());
    }
}
