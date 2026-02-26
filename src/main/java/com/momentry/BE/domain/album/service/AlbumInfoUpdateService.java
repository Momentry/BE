package com.momentry.BE.domain.album.service;

import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.album.exception.AlbumNotFoundException;
import com.momentry.BE.domain.album.repository.AlbumRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlbumInfoUpdateService {
    private final AlbumRepository albumRepository;

    // 앨범의 수정사항을 DB에 반영
    // 메인 트랜잭션이 커밋된 상태에서 DB에 반영하기 위해 REQUIRES_NEW 옵션 추가
    // *** 비동기 메서드 내부에서만 호출할 것! ***
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAlbumInfoForAsync(Album album, String albumName, String coverImageS3FileKey){
        Album targetAlbum = albumRepository.findById(album.getId())
                .orElseThrow(AlbumNotFoundException::new);

        if(albumName != null){
            targetAlbum.setName(albumName);
        }

        if(coverImageS3FileKey != null){
            targetAlbum.setCoverImageUrl(coverImageS3FileKey);
        }

        albumRepository.save(targetAlbum);
    }
}
