package com.momentry.BE.domain.file.dto;

import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GetFileDetailResponseDto {
    private Long fileId;
    private String fileType;
    private LocalDateTime capturedAt;
    private Long uploaderId;
    private String uploaderEmail;
    private String uploaderName;
    private String uploaderProfileImageUrl;
    private List<Long> tags;
    private String metadata;
    private Boolean isLiked;

    public static GetFileDetailResponseDto of(File file, List<Long> tags, Boolean isLiked, String uploaderProfileImageUrl) {
        User uploaderInfo = file.getUploader();

        return new GetFileDetailResponseDto(
            file.getId(),
            file.getFileType().toString(),
            file.getCapturedAt(),
            uploaderInfo.getId(),
            uploaderInfo.getEmail(),
            uploaderInfo.getUsername(),
            uploaderProfileImageUrl,
            tags,
            file.getMetadata(),
            isLiked
        );
    }
}
