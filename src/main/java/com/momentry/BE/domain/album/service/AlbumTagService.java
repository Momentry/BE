package com.momentry.BE.domain.album.service;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.momentry.BE.domain.album.entity.AlbumTag;
import com.momentry.BE.domain.album.exception.InvalidTagException;
import com.momentry.BE.domain.album.repository.AlbumTagRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlbumTagService {

    private final AlbumTagRepository albumTagRepository;

    @Transactional
    public void checkTagsInAlbum(List<Long> tagIdList, Long albumId) {
        List<AlbumTag> validTags = albumTagRepository.findAllById(tagIdList);

        // 요청한 태그 개수와 DB에서 찾은 태그 개수가 같은지 확인 (존재 하지 않는 ID 방지)
        if (validTags.size() != tagIdList.size()) {
            // 존재 하지 않는 태그가 포함 되어 있음
            throw new InvalidTagException();
        }

        // 모든 태그가 해당 앨범 소유 인지 확인
        boolean allTagsInAlbum = validTags.stream()
                .allMatch(tag -> tag.getAlbum().getId().equals(albumId));

        if (!allTagsInAlbum) {
            // 앨범에 속하지 않은 태그가 포함 되어 있음
            throw new InvalidTagException();
        }
    }

    @Transactional
    public void updateTagCount(Collection<Long> tagIds){
        albumTagRepository.updateCounts(tagIds);
    }
}
