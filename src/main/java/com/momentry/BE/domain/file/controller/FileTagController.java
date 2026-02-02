package com.momentry.BE.domain.file.controller;

import com.momentry.BE.domain.file.dto.FileTagRequestDto;
import com.momentry.BE.domain.file.service.FileTagService;
import com.momentry.BE.global.dto.ApiResponse;
import com.momentry.BE.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files/tags")
@RequiredArgsConstructor
public class FileTagController {

    private final FileTagService fileTagService;

    @PostMapping(value = "/{albumId}")
    public ResponseEntity<ApiResponse<Void>> addTagsToFiles(
            @RequestBody FileTagRequestDto fileTagRequestDto,
            @PathVariable Long albumId
            ){
        fileTagService.addTagsToFiles(
                fileTagRequestDto.getTagIds(),
                fileTagRequestDto.getFileIds(),
                SecurityUtil.getCurrentUserId(),
                albumId
        );
        return ApiResponse.ofSuccess(HttpStatus.OK, "태그 추가 성공", null);
    }

    @DeleteMapping(value = "/{albumId}")
    public ResponseEntity<ApiResponse<Void>> deleteTagsFromFiles(
            @RequestBody FileTagRequestDto fileTagRequestDto,
            @PathVariable Long albumId
    ){
        fileTagService.deleteTagsFromFiles(
                fileTagRequestDto.getTagIds(),
                fileTagRequestDto.getFileIds(),
                SecurityUtil.getCurrentUserId(),
                albumId
        );
        return ApiResponse.ofSuccess(HttpStatus.OK, "태그 제거 성공", null);
    }
}
