package com.momentry.BE.domain.search.controller;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.momentry.BE.domain.user.dto.UserSearchResult;
import com.momentry.BE.domain.album.dto.AlbumTagDetailResult;
import com.momentry.BE.domain.search.dto.AlbumSearchResponse;
import com.momentry.BE.domain.search.service.SearchService;
import com.momentry.BE.global.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
@Validated
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<AlbumTagDetailResult>>> searchTags(@RequestParam String tagName) {
        List<AlbumTagDetailResult> tags = searchService.searchByTagName(tagName);
        return ApiResponse.ofSuccess(tags);
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserSearchResult>>> searchUsers(
            @RequestParam @NotBlank(message = "검색 키워드는 필수 값입니다.") String keyword) {
        List<UserSearchResult> users = searchService.searchUsersByKeyword(keyword);
        return ApiResponse.ofSuccess(users);
    }

    /**
     * 앨범 검색
     * 
     * @param keyword 검색할 앨범 제목 키워드 (선택적)
     * @return 앨범 검색 결과
     */
    @GetMapping("/albums")
    public ResponseEntity<ApiResponse<List<AlbumSearchResponse>>> searchAlbums(
            @RequestParam(required = false) String keyword) {
        List<AlbumSearchResponse> albums = searchService.searchAlbums(keyword);
        return ApiResponse.ofSuccess(HttpStatus.OK, "앨범 검색 조회 성공", albums);
    }
}