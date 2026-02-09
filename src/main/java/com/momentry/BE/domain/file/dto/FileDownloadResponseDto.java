package com.momentry.BE.domain.file.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FileDownloadResponseDto {
    private List<DownloadUrlResponseDto> downloadUrlList;

    public static FileDownloadResponseDto of(List<DownloadUrlResponseDto> downloadUrlList){
        return new FileDownloadResponseDto(downloadUrlList);
    }
}
