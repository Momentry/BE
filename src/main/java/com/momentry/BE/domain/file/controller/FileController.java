package com.momentry.BE.domain.file.controller;

import com.momentry.BE.domain.file.dto.FileResult;
import com.momentry.BE.domain.file.dto.GetFileDetailResponseDto;
import com.momentry.BE.domain.file.service.FileService;
import com.momentry.BE.domain.file.service.FileUploadService;
import com.momentry.BE.global.dto.ApiResponse;
import com.momentry.BE.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final FileUploadService fileUploadService;

    @GetMapping(value = "/{albumId}/upload-url")
    public ResponseEntity<ApiResponse<List<String>>> getUploadUrls(
            @PathVariable Long albumId,
            @RequestParam("fileNum") Long fileNum
    ){
        // 업로드할 파일 개수를 전달
        List<String> uploadUrlList = fileService.getFileUploadUrls(SecurityUtil.getCurrentUserId(), albumId, fileNum);
        return ApiResponse.ofSuccess(HttpStatus.OK, "파일 업로드용 url 생성", uploadUrlList);
    }

    @PutMapping(value = "/{albumId}")
    public ResponseEntity<ApiResponse<List<FileResult>>> uploadFiles(
            @PathVariable Long albumId,
            @RequestParam("file") List<MultipartFile> files
            ){
        // 유저, 앨범 기반으로 파일 업로드 메서드 호출
        List<FileResult> response = fileUploadService.uploadFiles(SecurityUtil.getCurrentUserId(), albumId, files);
        return ApiResponse.ofSuccess(HttpStatus.CREATED, "파일 업로드 성공", response);
    }

    @DeleteMapping(value = "/{albumId}")
    public ResponseEntity<ApiResponse<Void>> deleteFiles(
            @PathVariable Long albumId,
            @RequestParam("fileIds") List<Long> fileIds
    ){
        // 파일 삭제 메서드 호출
        fileService.deleteFiles(SecurityUtil.getCurrentUserId(), albumId, fileIds);
        return ApiResponse.ofSuccess(HttpStatus.OK, "파일 삭제 성공", null);
    }


    @PostMapping(value = "/{albumId}/{fileId}/like")
    public ResponseEntity<ApiResponse<Void>> likeFile(
            @PathVariable Long albumId,
            @PathVariable Long fileId
    ){
        // 파일 좋아요 메서드 호출
        fileService.likeFile(SecurityUtil.getCurrentUserId(), albumId, fileId);
        return ApiResponse.ofSuccess(HttpStatus.OK, "파일 좋아요 성공", null);
    }

    @DeleteMapping(value = "/{albumId}/{fileId}/like")
    public ResponseEntity<ApiResponse<Void>> unlikeFile(
            @PathVariable Long albumId,
            @PathVariable Long fileId
    ){
        // 파일 좋아요 취소 메서드 호출
        fileService.unlikeFile(SecurityUtil.getCurrentUserId(), albumId, fileId);
        return ApiResponse.ofSuccess(HttpStatus.OK, "파일 좋아요 취소 성공", null);
    }

    @GetMapping(value = "/{albumId}/{fileId}")
    public ResponseEntity<ApiResponse<GetFileDetailResponseDto>> getFileDetail(
            @PathVariable Long albumId,
            @PathVariable Long fileId
    ){
        // 파일 상세정보 조회 메서드 호출
        GetFileDetailResponseDto fileDetail = fileService.getFileDetail(SecurityUtil.getCurrentUserId(), albumId, fileId);
        return ApiResponse.ofSuccess(HttpStatus.OK, "파일 상세 정보 조회 성공", fileDetail);
    }
}