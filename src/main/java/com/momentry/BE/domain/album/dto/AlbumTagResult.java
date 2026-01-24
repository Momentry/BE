package com.momentry.BE.domain.album.dto;

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
}
