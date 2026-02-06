package com.momentry.BE.domain.album.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDetailResponse {

    private String albumName;
    private String coverImageUrl;
    private Long fileCount;
}
