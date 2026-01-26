package com.momentry.BE.domain.file.repository;

import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.file.entity.FileLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileLikeRepository extends JpaRepository<FileLike, Long> {
    @Query("""
            SELECT f
            FROM FileLike fl
            JOIN fl.file f
            LEFT JOIN FETCH f.album a
            WHERE fl.user.id = :userId
            ORDER BY fl.file.createdAt ASC
            """)
    Page<File> findLikedFileByUserId(Long userId, Pageable pageable);
}
