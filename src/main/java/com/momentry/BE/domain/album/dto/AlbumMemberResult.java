package com.momentry.BE.domain.album.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlbumMemberResult {
    
    private Long userId;
    private String username;
    private String email;
    private String profileUrl;
}
