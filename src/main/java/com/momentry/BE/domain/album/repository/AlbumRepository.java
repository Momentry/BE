package com.momentry.BE.domain.album.repository;

import com.momentry.BE.domain.album.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    /**
     * 앨범 이름으로 앨범을 조회
     * 
     * @param name 앨범 이름
     * @return 앨범 (없으면 Optional.empty())
     */
    Optional<Album> findByName(String name);

    @Query("SELECT a FROM Album a JOIN FETCH a.members m JOIN FETCH m.permission WHERE a.id = :albumId")
    Optional<Album> findByIdWithMembers(@Param("albumId") Long albumId);

    @Query("SELECT a FROM Album a JOIN FETCH a.tags t WHERE a.id = :albumId")
    Optional<Album> findByIdWithTags(@Param("albumId") Long albumId);

    /**
     * 앨범 이름으로 앨범 검색 (부분 일치, 대소문자 무시)
     * 
     * @param keyword 검색 키워드
     * @return 앨범 목록
     */
    @Query("SELECT a FROM Album a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Album> findByNameContainingIgnoreCase(@Param("keyword") String keyword);
}
