package com.momentry.BE.domain.file.entity;

import com.momentry.BE.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.Assert;

@Entity
@Table(
        name = "file_likes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_user_file_like",
                        columnNames = {"user_id", "file_id"}
                )
        }
)
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    @Builder
    public FileLike(User user, File file){
        // 유효성 체크: 필수 값이 없으면 객체 생성 자체를 막음
        Assert.notNull(user, "사용자는 필수 값입니다.");
        Assert.notNull(file, "파일은 필수 값입니다.");

        this.user = user;
        this.file = file;
    }

}
