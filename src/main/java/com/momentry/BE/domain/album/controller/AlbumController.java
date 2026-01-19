package com.momentry.BE.domain.album.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.momentry.BE.domain.album.dto.AlbumTagResult;
import com.momentry.BE.domain.album.dto.TagCreationRequest;
import com.momentry.BE.domain.album.dto.TagUpdateRequest;
import com.momentry.BE.domain.album.service.AlbumService;
import com.momentry.BE.domain.file.dto.FilePageResult;
import com.momentry.BE.global.dto.ApiResponse;
import com.momentry.BE.global.service.CloudFrontSignedCookieService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/albums")
public class AlbumController {
    
    
    private final AlbumService albumService;
    private final CloudFrontSignedCookieService cloudFrontSignedCookieService;

    @PostMapping("/{albumId}/tags")
    public ResponseEntity<ApiResponse<Object>> createTag(@PathVariable Long albumId,
            @RequestBody TagCreationRequest request, Long userId) {
        albumService.createTag(albumId, request.getTagName(), userId);
        return ApiResponse.ofSuccess();
    }
    
    @DeleteMapping("/{albumId}/tags/{tagId}")
    public ResponseEntity<ApiResponse<Object>> deleteTag(@PathVariable Long albumId, @PathVariable Long tagId, Long userId) {
        albumService.deleteTag(albumId, tagId, userId);
        return ApiResponse.ofSuccess();
    }

    @PatchMapping("/{albumId}/tags/{tagId}")
    public ResponseEntity<ApiResponse<Object>> updateTag(@PathVariable Long albumId, @PathVariable Long tagId,
            @RequestBody TagUpdateRequest request, Long userId) {
        albumService.updateTag(albumId, tagId, request.getTagName(), userId);
        return ApiResponse.ofSuccess();
    }
    
    @GetMapping("/{albumId}/tags")
    public ResponseEntity<ApiResponse<List<AlbumTagResult>>> getTags(@PathVariable Long albumId, Long userId) {
        List<AlbumTagResult> tags = albumService.getTags(albumId, userId);
        return ApiResponse.ofSuccess(tags);
    }

    /**
     * 앨범의 파일 목록 조회
     * 사용자 접근 가능 여부 판단 필요
     * 현재는 이곳에서 앨범 접근 권한을 부여하고 있음.
     * 쿠키 부여 시점에 대한 추가 논의가 필요함.
     * 
     * @ImplNote 앨범의 모든 파일 목록 또는 태그에 해당하는 파일 목록을 반환합니다.
     * 
     * @param albumId 앨범 아이디
     * @param tagId 태그 아이디
     * @return 파일 목록(파일 아이디, 파일 URL, 썸네일 URL, 디스플레이 URL, 파일 타입)
     */
    @GetMapping("/{albumId}/files")
    public ResponseEntity<ApiResponse<FilePageResult>> getFiles(
            @PathVariable Long albumId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size, Long userId) {
        FilePageResult result = albumService.getFiles(albumId, tagId, cursor, size, userId);
        HttpHeaders headers = cloudFrontSignedCookieService.buildSignedCookieHeaders(albumId);
        return ApiResponse.ofSuccess(headers, result);
    }
}
