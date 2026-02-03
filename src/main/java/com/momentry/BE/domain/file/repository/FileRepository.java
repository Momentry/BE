package com.momentry.BE.domain.file.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.momentry.BE.domain.album.dto.AlbumCountDto;
import com.momentry.BE.domain.album.dto.AlbumUrlDto;
import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.user.entity.User;

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

    List<File> findByUploaderOrderByCreatedAtDescIdDesc(User user, Pageable pageable);

    @Query("""
            SELECT f FROM File f
            WHERE f.uploader = :user
              AND (f.createdAt < :cursorCreatedAt
                   OR (f.createdAt = :cursorCreatedAt AND f.id < :cursorId))
            ORDER BY f.createdAt DESC, f.id DESC
            """)
    List<File> findByUploaderWithCursor(@Param("user") User user,
                                    @Param("cursorCreatedAt") java.time.LocalDateTime cursorCreatedAt,
                                    @Param("cursorId") Long cursorId,
                                    Pageable pageable);

    @Query("""
            SELECT f FROM File f
            WHERE f.album.id IN :albumIds
            ORDER BY f.createdAt DESC, f.id DESC
            """)
    List<File> findByAlbumIdsOrderByCreatedAtDescIdDesc(@Param("albumIds") List<Long> albumIds,
                                                        Pageable pageable);

    @Query("""
            SELECT f FROM File f
            WHERE f.album.id IN :albumIds
              AND (f.createdAt < :cursorCreatedAt
                   OR (f.createdAt = :cursorCreatedAt AND f.id < :cursorId))
            ORDER BY f.createdAt DESC, f.id DESC
            """)
    List<File> findByAlbumIdsWithCursor(@Param("albumIds") List<Long> albumIds,
                                        @Param("cursorCreatedAt") java.time.LocalDateTime cursorCreatedAt,
                                        @Param("cursorId") Long cursorId,
                                        Pageable pageable);

    @Query("SELECT f.album.id AS albumId, COUNT(f) AS count FROM File f " +
            "WHERE f.album.id IN :albumIds GROUP BY f.album.id")
    List<AlbumCountDto> countFilesByAlbumIds(List<Long> albumIds);

    @Query("SELECT f.album.id AS albumId, f.thumbUrl AS contentUrl FROM File f " +
            "WHERE f.album.id IN :albumIds ORDER BY f.createdAt DESC")
    List<AlbumUrlDto> findThumbnailsByAlbumIds(List<Long> albumIds, Limit limit);

    /**
     * 앨범의 파일 개수를 조회
     *
     * @param album 앨범
     * @return 파일 개수
     */
    long countByAlbum(Album album);

    /**
     * 앨범의 모든 파일 삭제
     *
     * @param album 앨범
     */
    void deleteByAlbum(Album album);

    // UUID(fileKey)로 파일 엔티티 조회
    Optional<File> findByFileKey(String fileKey);

    // 업로더 정보와 함께 파일 엔티티 조회
    @Query("SELECT f FROM File f JOIN FETCH f.uploader WHERE f.id = :id")
    Optional<File> findByIdWithUploader(@Param("id") Long id);
}
