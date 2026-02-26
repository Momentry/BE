package com.momentry.BE.global.event.dto;

import com.momentry.BE.domain.album.entity.Album;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlbumCoverUploadEvent {
    private Long userId;
    private Album album;
    private String contentType;
    private String targetFilePath;
    private String prevAlbumCoverUrl;
}
