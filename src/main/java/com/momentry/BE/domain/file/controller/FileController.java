package com.momentry.BE.domain.file.controller;

import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.album.repository.AlbumRepository;
import com.momentry.BE.domain.file.dto.FileResult;
import com.momentry.BE.domain.file.service.FileService;
import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.domain.user.repository.UserRepository;
import com.momentry.BE.global.dto.ApiResponse;
import com.momentry.BE.security.dto.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PutMapping(value = "/{albumId}")
    public ResponseEntity<ApiResponse<FileResult>> uploadFile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long albumId,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value="metadata", required = false) String metadata,
            @RequestPart(value="createdAt", required = false) LocalDateTime createdAt
            ){
        // 유저, 앨범 기반으로 파일 업로드 메서드 호출
        FileResult response = fileService.uploadFile(userDetails.getUserId(), albumId, file, metadata, createdAt);
        return ApiResponse.ofSuccess(HttpStatus.CREATED, "파일 업로드 성공", response);
    }
}