package com.momentry.BE.domain.album.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "albums")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    private String coverImageUrl;

    @CreatedDate
    @Column(nullable = false, updatable = false) // 생성 시간 보호?
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "album", fetch = FetchType.LAZY)
    private List<AlbumMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "album", fetch = FetchType.LAZY)
    private List<AlbumTag> tags = new ArrayList<>();

    @Builder
    public Album(String name, String coverImageUrl) {
        // 유효성 체크: 필수 값이 없으면 객체 생성 자체를 막음
        Assert.hasText(name, "앨범의 이름은 필수값입니다.");

        this.name = name;
        this.coverImageUrl = coverImageUrl;
    }
}
