package com.momentry.BE.domain.file.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.Assert;

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
    private Tag tag;

    @Builder
    public FileTagInfo(File file, Tag tag){
        // 유효성 체크: 필수 값이 없으면 객체 생성 자체를 막음
        Assert.notNull(file, "파일은 필수 값입니다.");
        Assert.notNull(tag, "태그는 필수 값입니다.");

        this.file = file;
        this.tag = tag;
    }
}
