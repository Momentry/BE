package com.momentry.BE.domain.file.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MediaProcessingResultDto {
    private Long albumId;
    private String fileKey;
    private String thumbnailPath; // 예: "thumbnail/1/uuid.jpg"
    private String displayPath;   // 예: "display/1/uuid.jpg"
    private String status;        // "SUCCESS" 또는 "FAILURE"
}
