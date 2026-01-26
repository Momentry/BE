package com.momentry.BE.domain.album.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.momentry.BE.domain.album.dto.AlbumCreationRequest;
import com.momentry.BE.domain.album.dto.AlbumCreationResponse;
import com.momentry.BE.domain.album.dto.AlbumDetailResponse;
import com.momentry.BE.domain.album.dto.AlbumTagResult;
import com.momentry.BE.domain.album.dto.TagCreationRequest;
import com.momentry.BE.domain.album.dto.TagUpdateRequest;
import com.momentry.BE.domain.album.service.AlbumService;
import com.momentry.BE.domain.file.dto.FilePageResult;
import com.momentry.BE.global.dto.ApiResponse;
import com.momentry.BE.global.service.CloudFrontSignedCookieService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumService albumService;
    private final CloudFrontSignedCookieService cloudFrontSignedCookieService;

    /**
     * 앨범 생성
     * 
     * @param request 앨범 생성 요청 (albumName, albumCoverImage)
     * @param userId  사용자 ID (추후 시큐리티 적용 시 @AuthenticationPrincipal로 변경)
     * @return 앨범 생성 응답 (albumId, albumName)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AlbumCreationResponse>> createAlbum(
            @ModelAttribute AlbumCreationRequest request,
            Long userId) {

        AlbumCreationResponse response = albumService.createAlbum(
                request.getAlbumName(),
                request.getAlbumCoverImage(),
                userId);
        return ApiResponse.ofSuccess(HttpStatus.CREATED, "앨범 생성 성공", response);
    }

    /**
     * 앨범 상세 정보 조회
     * 
     * @param albumId 앨범 ID
     * @param userId  사용자 ID (추후 시큐리티 적용 시 @AuthenticationPrincipal로 변경)
     * @return 앨범 상세 정보 (앨범 이름, 커버 이미지, 파일 개수, 멤버 목록, 태그 목록)
     */
    @GetMapping("/{albumId}")
    public ResponseEntity<ApiResponse<AlbumDetailResponse>> getAlbum(
            @PathVariable Long albumId,
            Long userId) {
        AlbumDetailResponse response = albumService.getAlbumDetail(albumId, userId);
        return ApiResponse.ofSuccess(HttpStatus.OK, "앨범 정보 조회 성공", response);
    }

    /**
     * 앨범 정보 수정
     * 
     * @param albumId 앨범 ID
     * @param request 앨범 수정 요청 (albumName, albumCoverImage) - 선택적
     * @param userId  사용자 ID (추후 시큐리티 적용 시 @AuthenticationPrincipal로 변경)
     * @return 앨범 수정 응답
     */
    @PostMapping("/{albumId}")
    public ResponseEntity<ApiResponse<Object>> updateAlbum(
            @PathVariable Long albumId,
            @ModelAttribute AlbumCreationRequest request,
            Long userId) {

        albumService.updateAlbum(
                albumId,
                request.getAlbumName(),
                request.getAlbumCoverImage(),
                userId);
        return ApiResponse.ofSuccess(HttpStatus.OK, "앨범 정보 수정 성공", null);
    }

    @PostMapping("/{albumId}/leave")
    public ResponseEntity<ApiResponse<Object>> leaveAlbum(
            @PathVariable Long albumId, Long userId) {

        boolean isAlbumDeleted = albumService.leaveAlbum(albumId, userId);

        String message = isAlbumDeleted
                ? "앨범 나가기 성공, 잔여 멤버 0으로 앨범 삭제"
                : "앨범 나가기 성공";

        return ApiResponse.ofSuccess(HttpStatus.OK, message, null);
    }

    @PostMapping("/{albumId}/tags")
    public ResponseEntity<ApiResponse<Object>> createTag(@PathVariable Long albumId,
            @RequestBody TagCreationRequest request, Long userId) {
        albumService.createTag(albumId, request.getTagName(), userId);
        return ApiResponse.ofSuccess();
    }

    @DeleteMapping("/{albumId}/tags/{tagId}")
    public ResponseEntity<ApiResponse<Object>> deleteTag(@PathVariable Long albumId, @PathVariable Long tagId,
            Long userId) {
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
     * @param tagId   태그 아이디
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
