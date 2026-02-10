package com.momentry.BE.domain.file.dto;

import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.file.entity.FileType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FileResult {    
    private Long id;
    private String url;
    private String thumbnailUrl;
    private String displayUrl;
    private FileType fileType;

    public static FileResult of(File file) {
        return new FileResult(file.getId(), file.getOriginUrl(), file.getThumbUrl(), file.getDisplayUrl(), file.getFileType());
    }
}
