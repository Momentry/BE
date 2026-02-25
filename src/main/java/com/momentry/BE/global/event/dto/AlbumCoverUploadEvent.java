package com.momentry.BE.global.event.dto;

import com.momentry.BE.domain.album.entity.Album;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AlbumCoverUploadEvent {
    private Long albumId;
    private Long userId;
    private MultipartFile file;
    private String prevAlbumCoverUrl;
    private Album album;
}
