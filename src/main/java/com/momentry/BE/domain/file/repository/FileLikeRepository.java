package com.momentry.BE.domain.file.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.file.entity.FileLike;

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
        List<File> findLikedFileByUserId(Long userId, Pageable pageable);

        @Query("""
                        SELECT fl.file
                        FROM FileLike fl
                        JOIN fl.file f
                        LEFT JOIN FETCH f.album a
                        WHERE fl.user.id = :userId
                          AND (:cursorCreatedAt IS NULL OR (
                              f.createdAt < :cursorCreatedAt OR (f.createdAt = :cursorCreatedAt AND f.id < :cursorId)
                          ))
                        ORDER BY f.createdAt DESC, f.id DESC
                        """)
        List<File> findLikedFileByUserIdWithCursor(
                        Long userId,
                        LocalDateTime cursorCreatedAt,
                        Long cursorId,
                        Pageable pageable);

        boolean existsByFileIdAndUserId(Long fileId, Long userId);

        @Modifying
        @Query("DELETE FROM FileLike fl WHERE fl.file.id = :fileId AND fl.user.id = :userId")
        void deleteByFileIdAndUserId(@Param("fileId") Long fileId, @Param("userId") Long userId);
}
