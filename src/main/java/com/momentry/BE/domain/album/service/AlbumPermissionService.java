package com.momentry.BE.domain.album.service;

import com.momentry.BE.domain.album.entity.MemberAlbumPermission;
import com.momentry.BE.domain.album.repository.AlbumMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlbumPermissionService {
    private final AlbumMemberRepository albumMemberRepository;

    public void checkPermission(Long userId, Long albumId, MemberAlbumPermission requiredPermission){
        // 유저의 권한 확인을 위한 DB 조회
        MemberAlbumPermission userPermission = albumMemberRepository.findPermissionByAlbumIdAndUserId(albumId, userId)
                .orElseThrow(() -> new AccessDeniedException("해당 앨범의 멤버가 아닙니다."));

        // 요구 권한 수준 비교
        if(!userPermission.isAtLeast(requiredPermission)){
            throw new AccessDeniedException(
                    String.format("권한이 부족합니다. (요구 권한: %s, 사용자 권한: %s)", requiredPermission, userPermission)
            );
        }
    }
}
