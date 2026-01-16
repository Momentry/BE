package com.momentry.BE.domain.album.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.Assert;

@Entity
@Table(name = "album_permissions")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlbumPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String permission;

    @Builder
    public AlbumPermission(String permission){
        // 유효성 체크: 필수 값이 없으면 객체 생성 자체를 막음
        Assert.hasText(permission, "사용자는 필수 값입니다.");
        this.permission = permission;
    }
}
