package com.momentry.BE.domain.file.entity;

import com.momentry.BE.domain.album.entity.Album;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.Assert;

@Entity
@Table(
        name = "tags",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_album_tag_name",
                        columnNames = {"album_id", "tag_name"}
                )
        }
)
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="album_id", nullable = false)
    private Album album;

    @Column(nullable = false, length = 50)
    private String tagName;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long count = 0L;

    @Builder
    public Tag(Album album, String tagName){
        // 유효성 체크: 필수 값이 없으면 객체 생성 자체를 막음
        Assert.notNull(album, "앨범은 필수 값입니다.");
        Assert.hasText(tagName, "태그 이름은 필수 값입니다.");

        this.album = album;
        this.tagName = tagName;
        this.count = 0L;
    }
}
