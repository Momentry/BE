package com.momentry.BE.domain.album.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AlbumCreationResponse {
    private Long albumId;
    private String albumName;
    // 커버 이미지 업로드 시도 후 실패했을 때 true (기본 이미지 유지됨)
    private Boolean coverUploadFailed;
}
