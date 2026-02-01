package com.momentry.BE.domain.file.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.momentry.BE.domain.album.entity.AlbumTag;
import com.momentry.BE.domain.file.entity.FileTagInfo;

@Repository
public interface FileTagInfoRepository extends JpaRepository<FileTagInfo, Long> {
    
    @Transactional
    void deleteByTag(AlbumTag tag);

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


    // 특정 파일 리스트에 해당하는 태그 정보 조회
    @Query("SELECT ft FROM FileTagInfo ft " +
            "JOIN FETCH ft.file f " +
            "JOIN FETCH ft.tag t " +
            "WHERE f.id IN :fileIds")
    List<FileTagInfo> findAllByFileIdIn(List<Long> fileIds);

    // 특정 파일 리스트와 태그 리스트 조합을 일괄 삭제
    @Transactional
    @Modifying // 벌크 연산임을 명시
    @Query("DELETE FROM FileTagInfo ft WHERE ft.file.id IN :fileIds AND ft.tag.id IN :tagIds")
    void deleteByFileIdsAndTagIds(List<Long> fileIds, List<Long> tagIds);
}
