package com.momentry.BE.domain.file.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UploadFileDto {
    private Long uploaderId;
    private Long albumId;
    private MultipartFile file;
    private String metadata;
    private LocalDateTime createdAt;

    public static UploadFileDto of(Long uploaderId, Long albumId, MultipartFile file, String metadata, LocalDateTime createdAt){
        return new UploadFileDto(uploaderId, albumId, file, metadata, createdAt);
    }
}
