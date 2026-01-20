package com.momentry.BE.domain.album.repository;

import java.util.Optional;

import com.momentry.BE.domain.album.entity.AlbumPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumPermissionRepository extends JpaRepository<AlbumPermission, Long> {
	Optional<AlbumPermission> findByPermission(String permission);
}
