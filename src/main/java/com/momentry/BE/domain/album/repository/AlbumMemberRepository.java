package com.momentry.BE.domain.album.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.momentry.BE.domain.album.entity.AlbumMember;

@Repository
public interface AlbumMemberRepository extends JpaRepository<AlbumMember, Long> {

    @Query("SELECT am FROM AlbumMember am WHERE am.album.id = :albumId AND am.user.id = :userId")
    Optional<AlbumMember> findByAlbumIdAndUserId(@Param("albumId") Long albumId, @Param("userId") Long userId);

    @Query("SELECT am FROM AlbumMember am JOIN FETCH am.album WHERE am.album.id = :albumId AND am.user.id = :userId")
    Optional<AlbumMember> findByAlbumIdAndUserIdWithAlbum(@Param("albumId") Long albumId, @Param("userId") Long userId);

    @Query("SELECT am FROM AlbumMember am JOIN FETCH am.user WHERE am.album.id = :albumId")
    List<AlbumMember> findByAlbumIdWithUser(@Param("albumId") Long albumId);
}
