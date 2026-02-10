package com.momentry.BE.domain.album.service;

import com.momentry.BE.domain.album.entity.MemberAlbumPermission;
import com.momentry.BE.domain.album.repository.AlbumMemberRepository;
import com.momentry.BE.global.exception.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlbumPermissionService {
    private final AlbumMemberRepository albumMemberRepository;

    @Transactional(readOnly = true)
    public void checkPermission(Long userId, Long albumId, MemberAlbumPermission requiredPermission){
        // 유저의 권한 확인을 위한 DB 조회
        MemberAlbumPermission userPermission = albumMemberRepository.findPermissionByAlbumIdAndUserId(albumId, userId)
                .orElseThrow(AccessDeniedException::new);

        // 요구 권한 수준 비교
        if(!userPermission.isAtLeast(requiredPermission)){
            throw new AccessDeniedException();
        }
    }
}
