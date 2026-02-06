package com.momentry.BE.domain.file.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FileUploadResponseDto {
    private List<UploadUrlResponseDto> uploadUrlList;

    public static FileUploadResponseDto of(List<UploadUrlResponseDto> uploadUrlList){
        return new FileUploadResponseDto(uploadUrlList);
    }
}
