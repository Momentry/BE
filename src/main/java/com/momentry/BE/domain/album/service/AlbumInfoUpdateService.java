package com.momentry.BE.domain.album.service;

import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.album.repository.AlbumRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlbumInfoUpdateService {
    private final AlbumRepository albumRepository;

    // 앨범의 수정사항을 DB에 반영
    @Transactional
    public void updateAlbumInfo(Album album, String albumName, String coverImageS3FileKey){
        if(albumName != null){
            album.setName(albumName);
        }

        if(coverImageS3FileKey != null){
            album.setCoverImageUrl(coverImageS3FileKey);
        }

        albumRepository.save(album);
    }
}
