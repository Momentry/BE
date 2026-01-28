package com.momentry.BE.domain.album.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.album.entity.AlbumTag;

@Repository
public interface AlbumTagRepository extends JpaRepository<AlbumTag, Long> {

    Optional<AlbumTag> findByAlbumAndTagName(Album album, String tagName);

    List<AlbumTag> findByAlbum(Album album);

    Optional<AlbumTag> findByIdAndAlbumId(Long id, Long albumId);

    long countByAlbum(Album album);

    @Query("SELECT t FROM AlbumTag t JOIN FETCH t.album WHERE LOWER(t.tagName) LIKE LOWER(CONCAT('%', :tagName, '%'))")
    List<AlbumTag> findByTagNameContainingIgnoreCase(@Param("tagName") String tagName);
    
}
