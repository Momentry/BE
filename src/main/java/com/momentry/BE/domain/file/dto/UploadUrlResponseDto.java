package com.momentry.BE.domain.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UploadUrlResponseDto {
    private Long fileNo;
    private String uploadUrl;
}
