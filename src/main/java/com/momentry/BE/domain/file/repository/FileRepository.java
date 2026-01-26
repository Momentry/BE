package com.momentry.BE.domain.file.repository;

import java.util.List;

import com.momentry.BE.domain.album.dto.AlbumCountDto;
import com.momentry.BE.domain.album.dto.AlbumUrlDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.file.entity.File;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    List<File> findByAlbumOrderByCreatedAtDescIdDesc(Album album, Pageable pageable);

    @Query("""
            SELECT f FROM File f
            WHERE f.album = :album
              AND (f.createdAt < :cursorCreatedAt
                   OR (f.createdAt = :cursorCreatedAt AND f.id < :cursorId))
            ORDER BY f.createdAt DESC, f.id DESC
            """)
    List<File> findByAlbumWithCursor(@Param("album") Album album,
                                     @Param("cursorCreatedAt") java.time.LocalDateTime cursorCreatedAt,
                                     @Param("cursorId") Long cursorId,
                                     Pageable pageable);

    @Query("SELECT f.album.id AS albumId, COUNT(f) AS count FROM File f " +
            "WHERE f.album.id IN :albumIds GROUP BY f.album.id")
    List<AlbumCountDto> countFilesByAlbumIds(List<Long> albumIds);

    @Query("SELECT f.album.id AS albumId, f.thumbUrl AS contentUrl FROM File f " +
            "WHERE f.album.id IN :albumIds ORDER BY f.createdAt DESC")
    List<AlbumUrlDto> findThumbnailsByAlbumIds(List<Long> albumIds);
}
