package com.momentry.BE.domain.file.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FileDownloadRequestDto {
    private List<Long> downloadFileIdList;
}
