package com.momentry.BE.domain.album.repository;

import com.momentry.BE.domain.album.entity.AlbumMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumMemberRepository extends JpaRepository<AlbumMember, Long> {
}
