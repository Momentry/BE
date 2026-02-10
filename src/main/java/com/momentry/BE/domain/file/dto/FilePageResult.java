package com.momentry.BE.domain.file.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FilePageResult {
    private List<FileResult> files;
    private String nextCursor;
    private boolean hasNext;
}
