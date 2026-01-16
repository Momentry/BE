package com.momentry.BE.domain.album.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "album_permissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlbumPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String permission;

    @Builder
    public AlbumPermission(String permission){
        this.permission = permission;
    }
}
