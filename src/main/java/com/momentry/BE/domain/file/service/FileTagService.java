package com.momentry.BE.domain.file.service;

import com.momentry.BE.domain.album.entity.AlbumTag;
import com.momentry.BE.domain.album.entity.MemberAlbumPermission;
import com.momentry.BE.domain.album.repository.AlbumTagRepository;
import com.momentry.BE.domain.album.service.AlbumPermissionService;
import com.momentry.BE.domain.album.service.AlbumTagService;
import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.file.entity.FileTagInfo;
import com.momentry.BE.domain.file.repository.FileRepository;
import com.momentry.BE.domain.file.repository.FileTagInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileTagService {
    private final AlbumPermissionService albumPermissionService;
    private final AlbumTagService albumTagService;
    private final FileRepository fileRepository;
    private final FileTagInfoRepository fileTagInfoRepository;
    private final AlbumTagRepository albumTagRepository;

    @Transactional
    public void addTagsToFiles(List<Long> tagIdList, List<Long> fileIdList, Long userId, Long albumId){
        // EDITOR 이상의 권한을 가진 경우에만 태그 추가 가능
        albumPermissionService.checkPermission(userId, albumId, MemberAlbumPermission.EDITOR);

        // 앨범의 태그 소유권 검증
        albumTagService.checkTagsInAlbum(tagIdList, albumId);

        // 현재 파일들에 이미 연결된 태그 정보를 한 번에 조회 (최소한의 쿼리)
        List<FileTagInfo> existingInfos = fileTagInfoRepository.findAllByFileIdIn(fileIdList);

        // 중복 체크를 위한 Set 구성 ("파일ID:태그ID" 문자열 조합)
        Set<String> existingPairs = existingInfos.stream()
                .map(ft -> ft.getFile().getId() + ":" + ft.getTag().getId())
                .collect(Collectors.toSet());

        // 새로 추가해야하는 태그 정보 추리기
        List<FileTagInfo> newTagInfos = new ArrayList<>();
        for (Long fileId : fileIdList) {
            for (Long tagId : tagIdList) {
                // Set에 없는 조합인 경우에만 추가
                if (!existingPairs.contains(fileId + ":" + tagId)) {
                    File fileRef = fileRepository.getReferenceById(fileId);
                    AlbumTag tagRef = albumTagRepository.getReferenceById(tagId);

                    newTagInfos.add(FileTagInfo.builder()
                            .file(fileRef)
                            .tag(tagRef)
                            .build());
                }
            }
        }

        // 새로운 정보만 일괄 저장
        if (!newTagInfos.isEmpty()) {
            fileTagInfoRepository.saveAll(newTagInfos);
        }
    }

    @Transactional
    public void deleteTagsFromFiles(List<Long> tagIdList, List<Long> fileIdList, Long userId, Long albumId){
        // EDITOR 이상의 권한을 가진 경우에만 태그 삭제 가능
        albumPermissionService.checkPermission(userId, albumId, MemberAlbumPermission.EDITOR);

        // 앨범의 태그 소유권 검증
        albumTagService.checkTagsInAlbum(tagIdList, albumId);

        // 삭제 실행
        fileTagInfoRepository.deleteByFileIdsAndTagIds(fileIdList, tagIdList);
    }
}
