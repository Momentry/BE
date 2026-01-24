package com.momentry.BE.domain.search.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.momentry.BE.domain.album.dto.AlbumTagDetailResult;
import com.momentry.BE.domain.search.service.SearchService;
import com.momentry.BE.global.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<AlbumTagDetailResult>>> searchTags(@RequestParam String tagName) {
        List<AlbumTagDetailResult> tags = searchService.searchByTagName(tagName);
        return ApiResponse.ofSuccess(tags);
    }
}