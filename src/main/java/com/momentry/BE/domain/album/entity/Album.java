package com.momentry.BE.domain.album.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "albums")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    private String coverImageUrl;

    @CreatedDate
    @Column(nullable = false, updatable = false)    // 생성 시간 보호?
    private LocalDateTime createdAt;

    @Builder
    public Album(String name, String coverImageUrl){
        this.name = name;
        this.coverImageUrl = coverImageUrl;
    }
}
