package com.momentry.BE.domain.file.repository;

import java.util.List;

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
}
