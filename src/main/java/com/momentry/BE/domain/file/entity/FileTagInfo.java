package com.momentry.BE.domain.file.entity;

import org.springframework.util.Assert;

import com.momentry.BE.domain.album.entity.AlbumTag;

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

@Entity
@Table(
        name = "file_tag_infos",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_file_tag",
                        columnNames = {"file_id", "tag_id"}
                )
        }
)
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileTagInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private AlbumTag tag;

    @Builder
    public FileTagInfo(File file, AlbumTag tag){
        // 유효성 체크: 필수 값이 없으면 객체 생성 자체를 막음
        Assert.notNull(file, "파일은 필수 값입니다.");
        Assert.notNull(tag, "태그는 필수 값입니다.");

        this.file = file;
        this.tag = tag;
    }
}
