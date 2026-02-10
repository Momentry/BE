package com.momentry.BE.domain.file.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.momentry.BE.domain.album.entity.AlbumTag;
import com.momentry.BE.domain.album.entity.MemberAlbumPermission;
import com.momentry.BE.domain.album.repository.AlbumTagRepository;
import com.momentry.BE.domain.album.service.AlbumPermissionService;
import com.momentry.BE.domain.album.service.AlbumTagService;
import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.file.entity.FileTagInfo;
import com.momentry.BE.domain.file.repository.FileRepository;
import com.momentry.BE.domain.file.repository.FileTagInfoRepository;

@ExtendWith(MockitoExtension.class)
class FileTagServiceTest {

    @InjectMocks
    private FileTagService fileTagService;

    @Mock private AlbumPermissionService albumPermissionService;
    @Mock private AlbumTagService albumTagService;
    @Mock private FileRepository fileRepository;
    @Mock private FileTagInfoRepository fileTagInfoRepository;
    @Mock private AlbumTagRepository albumTagRepository;

    private final Long userId = 1L;
    private final Long albumId = 1L;

    @Nested
    @DisplayName("태그 추가 테스트")
    class AddTags {

        @Test
        @DisplayName("성공: 새로운 태그 조합만 선별하여 저장한다.")
        void addTagsSuccess() {
            // given
            List<Long> tagIdList = List.of(10L, 11L);
            List<Long> fileIdList = List.of(100L);

            // 기존에 100번 파일에 10번 태그는 이미 달려있다고 가정
            File mockFile = mock(File.class);
            AlbumTag mockTag = mock(AlbumTag.class);
            when(mockFile.getId()).thenReturn(100L);
            when(mockTag.getId()).thenReturn(10L);

            FileTagInfo existingInfo = mock(FileTagInfo.class);
            when(existingInfo.getFile()).thenReturn(mockFile);
            when(existingInfo.getTag()).thenReturn(mockTag);

            when(fileTagInfoRepository.findAllByFileIdIn(fileIdList)).thenReturn(List.of(existingInfo));

            // Reference 조회 모킹
            when(fileRepository.getReferenceById(100L)).thenReturn(mockFile);
            when(albumTagRepository.getReferenceById(11L)).thenReturn(mock(AlbumTag.class));

            // when
            fileTagService.addTagsToFiles(tagIdList, fileIdList, userId, albumId);

            // then
            // 10번은 중복이라 제외되고, 11번 태그에 대해서만 1번 저장되어야 함
            verify(fileTagInfoRepository, times(1)).saveAll(anyList());
            verify(albumPermissionService).checkPermission(userId, albumId, MemberAlbumPermission.EDITOR);
            verify(albumTagService).checkTagsInAlbum(tagIdList, albumId);
        }

        @Test
        @DisplayName("성공: 모든 태그가 이미 존재하면 저장하지 않는다.")
        void addTagsAllDuplicate() {
            // given
            List<Long> tagIds = List.of(10L);
            List<Long> fileIds = List.of(100L);

            File mockFile = mock(File.class);
            AlbumTag mockTag = mock(AlbumTag.class);
            when(mockFile.getId()).thenReturn(100L);
            when(mockTag.getId()).thenReturn(10L);

            FileTagInfo existing = mock(FileTagInfo.class);
            when(existing.getFile()).thenReturn(mockFile);
            when(existing.getTag()).thenReturn(mockTag);

            when(fileTagInfoRepository.findAllByFileIdIn(fileIds)).thenReturn(List.of(existing));

            // when
            fileTagService.addTagsToFiles(tagIds, fileIds, userId, albumId);

            // then
            verify(fileTagInfoRepository, never()).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("태그 삭제 테스트")
    class DeleteTags {

        @Test
        @DisplayName("성공: 태그 삭제 요청 시 Repository의 벌크 삭제 메서드를 호출한다.")
        void deleteTagsSuccess() {
            // given
            List<Long> tagIds = List.of(10L, 11L);
            List<Long> fileIds = List.of(100L, 101L);

            // when
            fileTagService.deleteTagsFromFiles(tagIds, fileIds, userId, albumId);

            // then
            verify(albumPermissionService).checkPermission(userId, albumId, MemberAlbumPermission.EDITOR);
            verify(albumTagService).checkTagsInAlbum(tagIds, albumId);
            verify(fileTagInfoRepository).deleteByFileIdsAndTagIds(fileIds, tagIds);
        }
    }
}
