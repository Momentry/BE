package com.momentry.BE.domain.search.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlbumSearchResponse {

    private Long albumId;
    private String albumName;
    private String coverImageUrl;
    private LocalDateTime created_at;
    private Integer memberCount;
    private List<String> memberProfileUrls; // 최대 12개, username 가나다순
}
