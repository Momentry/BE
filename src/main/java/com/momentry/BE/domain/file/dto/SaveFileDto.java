package com.momentry.BE.domain.file.dto;

import com.momentry.BE.domain.file.entity.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaveFileDto {

    private Long uploaderId;
    private Long albumId;
    private FileType fileType;
    private String metadata;
    private String contentType;
    private LocalDateTime capturedAt;
    private String originalPath;
    private String thumbnailPath;
    private String displayPath;
    private String fileKey;
    private Long fileSize;

    // SQS 큐 메시지로 생성
    public static SaveFileDto of(MediaProcessingResultDto uploadMessage){
        // capturedAt null 체크
        LocalDateTime capturedAt = LocalDateTime.now();
        if(uploadMessage.getCapturedAt() != null && !uploadMessage.getCapturedAt().isEmpty()){
            capturedAt = LocalDateTime.parse(uploadMessage.getCapturedAt());
        }

        return SaveFileDto.builder()
                .uploaderId(uploadMessage.getUploaderId())
                .albumId(uploadMessage.getAlbumId())
                .fileType(uploadMessage.getFileType())
                .metadata(uploadMessage.getMetadata())
                .contentType(uploadMessage.getContentType())
                .capturedAt(capturedAt)
                .originalPath(uploadMessage.getOriginalPath())
                .thumbnailPath(uploadMessage.getThumbnailPath())
                .displayPath(uploadMessage.getDisplayPath())
                .fileKey(uploadMessage.getFileKey())
                .fileSize(Long.parseLong(uploadMessage.getFileSize()))
                .build();
    }
}
