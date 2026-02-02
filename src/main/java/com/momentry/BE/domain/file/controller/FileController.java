package com.momentry.BE.domain.file.controller;

import com.momentry.BE.domain.file.dto.FileResult;
import com.momentry.BE.domain.file.dto.GetFileDetailResponseDto;
import com.momentry.BE.domain.file.service.FileService;
import com.momentry.BE.global.dto.ApiResponse;
import com.momentry.BE.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PutMapping(value = "/{albumId}")
    public ResponseEntity<ApiResponse<List<FileResult>>> uploadFiles(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long albumId,
            @RequestParam("file") List<MultipartFile> files
            ){
        // 유저, 앨범 기반으로 파일 업로드 메서드 호출
        List<FileResult> response = fileService.uploadFiles(userDetails.getUserId(), albumId, files);
        return ApiResponse.ofSuccess(HttpStatus.CREATED, "파일 업로드 성공", response);
    }

    @DeleteMapping(value = "/{albumId}")
    public ResponseEntity<ApiResponse<Void>> deleteFiles(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long albumId,
            @RequestParam("fileIds") List<Long> fileIds
    ){
        // 파일 삭제 메서드 호출
        fileService.deleteFiles(userDetails.getUserId(), albumId, fileIds);
        return ApiResponse.ofSuccess(HttpStatus.OK, "파일 삭제 성공", null);
    }


    @PostMapping(value = "/{albumId}/{fileId}/like")
    public ResponseEntity<ApiResponse<Void>> likeFile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long albumId,
            @PathVariable Long fileId
    ){
        // 파일 좋아요 메서드 호출
        fileService.likeFile(userDetails.getUserId(), albumId, fileId);
        return ApiResponse.ofSuccess(HttpStatus.OK, "파일 좋아요 성공", null);
    }

    @DeleteMapping(value = "/{albumId}/{fileId}/like")
    public ResponseEntity<ApiResponse<Void>> unlikeFile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long albumId,
            @PathVariable Long fileId
    ){
        // 파일 좋아요 취소 메서드 호출
        fileService.unlikeFile(userDetails.getUserId(), albumId, fileId);
        return ApiResponse.ofSuccess(HttpStatus.OK, "파일 좋아요 취소 성공", null);
    }

    @GetMapping(value = "/{albumId}/{fileId}")
    public ResponseEntity<ApiResponse<GetFileDetailResponseDto>> getFileDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long albumId,
            @PathVariable Long fileId
    ){
        // 파일 상세정보 조회 메서드 호출
        GetFileDetailResponseDto fileDetail = fileService.getFileDetail(userDetails.getUserId(), albumId, fileId);
        return ApiResponse.ofSuccess(HttpStatus.OK, "파일 상세 정보 조회 성공", fileDetail);
    }
}