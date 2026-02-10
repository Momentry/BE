package com.momentry.BE.domain.file.entity;

import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // S3 파일명으로 사용되는 UUID 저장
    @Column(nullable = false, unique = true)
    private String fileKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @Column(nullable = false)
    private String originUrl; // 원본 이미지 URL

    private String thumbUrl;  // 썸네일 URL

    private String displayUrl; // 최적화된 보기용 URL

    @Column(columnDefinition = "json")
    private String metadata; // 촬영 장비, 해상도 등 JSON 데이터

    private String contentType;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long likesCount = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType fileType;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime capturedAt;

    @Builder
    public File(Album album, String fileKey, String originUrl, String thumbUrl, String displayUrl,
                String metadata, String contentType, FileType fileType, User uploader, LocalDateTime capturedAt) {
        // 유효성 체크: 필수 값이 없으면 객체 생성 자체를 막음
        Assert.notNull(album, "앨범은 필수 값입니다.");
        Assert.hasText(fileKey, "파일 키는 필수 값입니다.");
        Assert.hasText(originUrl, "원본 url은 필수 값입니다.");
        Assert.notNull(fileType, "파일 타입은 필수 정보입니다.");
        Assert.notNull(uploader, "게시자 정보는 필수 값입니다.");

        this.album = album;
        this.fileKey = fileKey;
        this.originUrl = originUrl;
        this.thumbUrl = thumbUrl;
        this.displayUrl = displayUrl;
        this.metadata = metadata;
        this.contentType = contentType;
        this.fileType = fileType;
        this.uploader = uploader;
        this.capturedAt = capturedAt;

        this.createdAt = LocalDateTime.now();
        this.likesCount = 0L; // 기본값은 0
    }

    public void incrementLikesCount(){
        if(this.likesCount == null){
            this.likesCount = 0L;
        }
        this.likesCount++;
    }

    public void decrementLikesCount(){
        if (this.likesCount == null || this.likesCount <= 0L) {
            this.likesCount = 0L;
        } else {
            this.likesCount--;
        }
    }

    public void updatePostProcessingResults(String thumbUrl, String displayUrl, String metadata, LocalDateTime createdAt) {
        this.thumbUrl = thumbUrl;
        this.displayUrl = displayUrl;
        this.metadata = metadata;
        this.createdAt = createdAt;
    }
}
