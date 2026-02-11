package com.momentry.BE.domain.file.dto;

import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.file.entity.FileType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResult {    
    private Long id;
    private String thumbnailUrl;
    private String displayUrl;
    private FileType fileType;
    private Long albumId;

    public static FileResult of(File file) {
        return FileResult.builder()
                .id(file.getId())
                .displayUrl(file.getDisplayUrl())
                .thumbnailUrl(file.getThumbUrl())
                .fileType(file.getFileType())
                .albumId(file.getAlbum().getId())
                .build();
    }
}
