package com.momentry.BE.domain.file.entity;

import com.momentry.BE.domain.album.entity.Album;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @Column(nullable = false)
    private String originUrl; // 원본 이미지 URL

    private String thumbUrl;  // 썸네일 URL

    private String displayUrl; // 최적화된 보기용 URL

    @Column(columnDefinition = "json")
    private String metadata; // 촬영 장비, 해상도 등 JSON 데이터

    @Column(nullable = false)
    private Long likesCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType fileType;

    @Column(nullable = false)
    private Long uploaderId; // 업로드한 사용자의 ID

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public File(Album album, String originUrl, String thumbUrl, String displayUrl,
                String metadata, FileType fileType, Long uploaderId, LocalDateTime createdAt) {
        this.album = album;
        this.originUrl = originUrl;
        this.thumbUrl = thumbUrl;
        this.displayUrl = displayUrl;
        this.metadata = metadata;
        this.fileType = fileType;
        this.uploaderId = uploaderId;
        this.createdAt = (createdAt==null) ? LocalDateTime.now() : createdAt;

        this.likesCount = 0L; // 기본값은 0
    }
}
