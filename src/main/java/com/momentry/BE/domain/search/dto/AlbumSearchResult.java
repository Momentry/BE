package com.momentry.BE.domain.search.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlbumSearchResult {
    
    private Long albumId;
    private String albumName;
    private String coverImageUrl;
}
