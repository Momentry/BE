package com.momentry.BE.domain.file.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.momentry.BE.domain.file.entity.File;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LikedFileDto {
    private Long fileId;
    private String thumbnailUrl;
    private String displayUrl;
    private String type; // "image" 또는 "video"

    @JsonFormat(pattern = "yyyy.MM.dd")
    @JsonProperty("created_at")
    private LocalDate createdAt;

    private Long albumId;
    private String albumName;

    public LikedFileDto(File file) {
        this.fileId = file.getId();
        this.thumbnailUrl = file.getThumbUrl();
        this.displayUrl = file.getDisplayUrl();
        this.type = file.getFileType().name();

        this.createdAt = file.getCreatedAt().toLocalDate();

        this.albumId = file.getAlbum().getId();
        this.albumName = file.getAlbum().getName();
    }
}
