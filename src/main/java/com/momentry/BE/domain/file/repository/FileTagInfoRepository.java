package com.momentry.BE.domain.file.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.momentry.BE.domain.album.entity.AlbumTag;
import com.momentry.BE.domain.file.dto.TagThumbnailRowDto;
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

    @Query(value = """
            SELECT t.tag_id AS tagId, t.thumb_url AS thumbUrl
            FROM (
                SELECT fti.tag_id,
                       f.thumb_url,
                       ROW_NUMBER() OVER (PARTITION BY fti.tag_id ORDER BY f.created_at DESC, f.id DESC) AS rn
                FROM file_tag_infos fti
                JOIN files f ON f.id = fti.file_id
                WHERE fti.tag_id IN (:tagIds)
            ) t
            WHERE t.rn <= 3
            ORDER BY t.tag_id ASC, t.rn ASC
            """, nativeQuery = true)
    List<TagThumbnailRowDto> findThumbnailRowsByTagIds(List<Long> tagIds);


    // 특정 파일 리스트에 해당하는 태그 정보 조회
    @Query("SELECT ft FROM FileTagInfo ft " +
            "JOIN FETCH ft.file f " +
            "JOIN FETCH ft.tag t " +
            "WHERE f.id IN :fileIds")
    List<FileTagInfo> findAllByFileIdIn(List<Long> fileIds);

    // 특정 파일 리스트와 태그 리스트 조합을 일괄 삭제
    @Modifying // 벌크 연산임을 명시
    @Query("DELETE FROM FileTagInfo ft WHERE ft.file.id IN :fileIds AND ft.tag.id IN :tagIds")
    void deleteByFileIdsAndTagIds(List<Long> fileIds, List<Long> tagIds);

    // 특정 파일 ID에 연결된 모든 태그 ID 리스트 조회
    @Query("SELECT ft.tag.id FROM FileTagInfo ft WHERE ft.file.id = :fileId")
    List<Long> findTagIdsByFileId(Long fileId);
}
