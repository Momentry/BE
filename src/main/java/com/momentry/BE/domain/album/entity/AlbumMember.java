package com.momentry.BE.domain.album.entity;

import com.momentry.BE.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "album_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_user_album",
                        columnNames = {"user_id", "album_id"}   // 두 컬럼의 조합은 유일해야 함
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlbumMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private AlbumPermission permission;

    @Builder
    public AlbumMember(User user, Album album, AlbumPermission permission) {
        this.user = user;
        this.album = album;
        this.permission = permission;
    }
}
