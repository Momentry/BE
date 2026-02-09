package com.momentry.BE.domain.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class DownloadUrlResponseDto {
    private Long fileId;
    private String downloadUrl;
    private String contentType;
}
