package com.momentry.BE.domain.album.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlbumCountDto {
    private Long albumId;
    private Integer count;
}
