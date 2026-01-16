package com.momentry.BE.domain.file.entity;

import com.momentry.BE.domain.album.entity.Album;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(nullable = false)
    private Long count = 0L;

    @Builder
    public Tag(Album album, String tagName){
        this.album = album;
        this.tagName = tagName;
        this.count = 0L;
    }
}
