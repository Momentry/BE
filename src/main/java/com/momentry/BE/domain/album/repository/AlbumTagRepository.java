package com.momentry.BE.domain.album.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query("SELECT t FROM AlbumTag t " +
            "JOIN FETCH t.album a " +
            "JOIN AlbumMember am ON am.album = a " +
            "WHERE am.user.id = :userId " +
            "AND LOWER(t.tagName) LIKE LOWER(CONCAT('%', :tagName, '%')) " +
            "ORDER BY t.id DESC")
    List<AlbumTag> findByTagNameContainingIgnoreCase(@Param("tagName") String tagName,
            @Param("userId") Long userId,
            Pageable pageable);

    @Query("SELECT t FROM AlbumTag t " +
            "JOIN FETCH t.album a " +
            "JOIN AlbumMember am ON am.album = a " +
            "WHERE am.user.id = :userId " +
            "AND LOWER(t.tagName) LIKE LOWER(CONCAT('%', :tagName, '%')) " +
            "AND t.id < :cursorId " +
            "ORDER BY t.id DESC")
    List<AlbumTag> findByTagNameContainingIgnoreCaseWithCursor(@Param("tagName") String tagName,
            @Param("userId") Long userId,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE AlbumTag t SET t.count = " +
            "(SELECT COUNT(ft) FROM FileTagInfo ft WHERE ft.tag.id = t.id) " +
            "WHERE t.id IN :tagIds")
    void updateCounts(@Param("tagIds") Collection<Long> tagIds);
}
