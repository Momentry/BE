package com.momentry.BE.domain.album.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.momentry.BE.domain.album.dto.AlbumCountDto;
import com.momentry.BE.domain.album.dto.AlbumUrlDto;
import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.album.entity.AlbumMember;
import com.momentry.BE.domain.album.entity.MemberAlbumPermission;

@Repository
public interface AlbumMemberRepository extends JpaRepository<AlbumMember, Long> {

    @Query("SELECT am FROM AlbumMember am WHERE am.album.id = :albumId AND am.user.id = :userId")
    Optional<AlbumMember> findByAlbumIdAndUserId(@Param("albumId") Long albumId, @Param("userId") Long userId);

    @Query("SELECT am FROM AlbumMember am JOIN FETCH am.album WHERE am.album.id = :albumId AND am.user.id = :userId")
    Optional<AlbumMember> findByAlbumIdAndUserIdWithAlbum(@Param("albumId") Long albumId,
            @Param("userId") Long userId);

    @Query("SELECT am FROM AlbumMember am JOIN FETCH am.user WHERE am.album.id = :albumId")
    List<AlbumMember> findByAlbumIdWithUser(@Param("albumId") Long albumId);

    @Query("SELECT am.album FROM AlbumMember am WHERE am.user.id = :userId")
    List<Album> findAlbumsByUserId(Long userId);

    @Query("SELECT am.album.id FROM AlbumMember am WHERE am.user.id = :userId")
    List<Long> findAlbumIdsByUserId(Long userId);


    @Query("SELECT new com.momentry.BE.domain.album.dto.AlbumCountDto(am.album.id, CAST(COUNT(am) AS integer)) FROM AlbumMember am "
            +
            "WHERE am.album.id IN :albumIds GROUP BY am.album.id")
    List<AlbumCountDto> countMembersByAlbumIds(List<Long> albumIds);

    @Query("SELECT new com.momentry.BE.domain.album.dto.AlbumUrlDto(am.album.id, am.user.profileImageUrl) FROM AlbumMember am "
            +
            "WHERE am.album.id IN :albumIds ORDER BY am.album.id, am.user.username ASC")
    List<AlbumUrlDto> findMemberProfilesByAlbumIds(@Param("albumIds") List<Long> albumIds);

    @Query("SELECT am.permission FROM AlbumMember am " +
            "WHERE am.album.id = :albumId AND am.user.id = :userId")
    Optional<MemberAlbumPermission> findPermissionByAlbumIdAndUserId(Long albumId, Long userId);
}
