package com.momentry.BE.domain.album.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlbumCountDto {
    private Long albumId;
    private Integer count;
}
