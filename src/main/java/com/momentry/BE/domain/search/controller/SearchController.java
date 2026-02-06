package com.momentry.BE.domain.search.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.momentry.BE.domain.search.dto.SearchAlbumsResponse;
import com.momentry.BE.domain.search.dto.SearchTagsResponse;
import com.momentry.BE.domain.search.dto.SearchUsersResponse;
import com.momentry.BE.domain.search.service.SearchService;
import com.momentry.BE.global.dto.ApiResponse;
import com.momentry.BE.security.util.SecurityUtil;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
@Validated
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<SearchTagsResponse>> searchTags(
            @RequestParam String tagName,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        SearchTagsResponse tags = searchService.searchByTagName(tagName, userId, cursor, size);
        return ApiResponse.ofSuccess(tags);
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<SearchUsersResponse>> searchUsers(
            @RequestParam @NotBlank(message = "검색 키워드는 필수 값입니다.") String keyword,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        SearchUsersResponse users = searchService.searchUsersByKeyword(keyword, cursor, size);
        return ApiResponse.ofSuccess(users);
    }

    /**
     * 앨범 검색
     * 
     * @param keyword 검색할 앨범 제목 키워드 (선택적)
     * @return 앨범 검색 결과
     */
    @GetMapping("/albums")
    public ResponseEntity<ApiResponse<SearchAlbumsResponse>> searchAlbums(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        SearchAlbumsResponse albums = searchService.searchAlbums(userId, keyword, cursor, size);
        return ApiResponse.ofSuccess(albums);
    }
}