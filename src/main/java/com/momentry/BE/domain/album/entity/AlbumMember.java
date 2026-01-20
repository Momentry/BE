package com.momentry.BE.domain.album.entity;

import com.momentry.BE.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.Assert;

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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
        // 유효성 체크: 필수 값이 없으면 객체 생성 자체를 막음
        Assert.notNull(user, "사용자는 필수 값입니다.");
        Assert.notNull(album, "앨범은 필수 값입니다.");
        Assert.notNull(permission, "권한 정보는 필수 값입니다.");

        this.user = user;
        this.album = album;
        this.permission = permission;
    }

    public void changePermission(AlbumPermission permission) {
        Assert.notNull(permission, "권한 정보는 필수 값입니다.");
        this.permission = permission;
    }
}
