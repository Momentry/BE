package com.momentry.BE.domain.user.dto;

import java.util.List;

import com.momentry.BE.domain.file.dto.FileResult;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetCurrentUserFileListResponse {
    private List<FileResult> files;
    private String nextCursor;
}
