package com.momentry.BE.domain.file.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FileUploadResponseDto {
    private List<UploadUrlDto> uploadUrlList;

    public static FileUploadResponseDto of(List<UploadUrlDto> uploadUrlList){
        return new FileUploadResponseDto(uploadUrlList);
    }
}
