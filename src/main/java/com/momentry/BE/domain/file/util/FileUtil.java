package com.momentry.BE.domain.file.util;

import org.springframework.stereotype.Component;

import java.util.Map;

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
            "video/x-msvideo", ".avi"
    );

    public String getExtension(String contentType) {
        if (contentType == null) return "";

        // Map에서 찾고, 없으면 기본값으로 빈 문자열 혹은 예외 처리
        return MIME_TYPE_MAP.getOrDefault(contentType.toLowerCase(), "");
    }
}
