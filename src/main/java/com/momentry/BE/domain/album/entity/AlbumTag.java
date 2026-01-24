package com.momentry.BE.domain.album.entity;

import org.springframework.util.Assert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AlbumTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="album_id", nullable = false)
    private Album album;


    @Setter
    @Column(nullable = false, length = 50)
    private String tagName;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long count = 0L;

    @Builder
    public AlbumTag (Album album, String tagName){
        Assert.notNull(album, "앨범은 필수 값입니다.");
        Assert.hasText(tagName, "태그 이름은 필수 값입니다.");

        this.album = album;
        this.tagName = tagName;
        this.count = 0L;
    }
}
