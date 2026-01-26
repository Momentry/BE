package com.momentry.BE.domain.search.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.momentry.BE.domain.album.dto.AlbumMemberProfileResult;

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
    private List<AlbumMemberProfileResult> memberProfiles; // 최대 12명, username 가나다순
}
