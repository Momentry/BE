package com.momentry.BE.domain.album.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlbumUrlDto {
    private Long albumId;
    private String url;
}
