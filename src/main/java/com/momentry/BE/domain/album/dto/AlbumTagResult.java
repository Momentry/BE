package com.momentry.BE.domain.album.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlbumTagResult {
    
    private Long id;
    private String tagName;
    private Long count;
    private List<String> thumbnailUrls;
}
