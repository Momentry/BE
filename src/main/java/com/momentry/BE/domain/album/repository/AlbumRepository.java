package com.momentry.BE.domain.album.repository;

import com.momentry.BE.domain.album.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
