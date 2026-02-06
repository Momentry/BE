package com.momentry.BE.domain.file.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FileUploadRequestDto {
    private List<UploadFileInfoDto> uploadFileInfoList;
}
