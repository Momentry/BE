package com.momentry.BE.domain.file.entity;

import com.momentry.BE.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
        this.user = user;
        this.file = file;
    }

}
