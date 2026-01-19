package com.momentry.BE.domain.file.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.momentry.BE.domain.album.entity.AlbumTag;
import com.momentry.BE.domain.file.entity.FileTagInfo;

@Repository
public interface FileTagInfoRepository extends JpaRepository<FileTagInfo, Long> {
    
    @Transactional
    void deleteByAlbumTag(AlbumTag albumTag);

    @Query("""
            SELECT ft FROM FileTagInfo ft
            JOIN FETCH ft.file
            WHERE ft.tag.id = :tagId
            ORDER BY ft.file.createdAt DESC, ft.file.id DESC
            """)
    List<FileTagInfo> fetchByTag(Long tagId, Pageable pageable);

    @Query("""
            SELECT ft FROM FileTagInfo ft
            JOIN FETCH ft.file
            WHERE ft.tag.id = :tagId
              AND (ft.file.createdAt < :cursorCreatedAt
                   OR (ft.file.createdAt = :cursorCreatedAt AND ft.file.id < :cursorId))
            ORDER BY ft.file.createdAt DESC, ft.file.id DESC
            """)
    List<FileTagInfo> fetchByTagWithCursor(Long tagId,
                                           java.time.LocalDateTime cursorCreatedAt,
                                           Long cursorId,
                                           Pageable pageable);
}
