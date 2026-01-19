package com.momentry.BE.domain.album.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.momentry.BE.domain.album.dto.AlbumTagResult;
import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.album.entity.AlbumMember;
import com.momentry.BE.domain.album.entity.AlbumPermission;
import com.momentry.BE.domain.album.entity.AlbumTag;
import com.momentry.BE.domain.album.exception.NoAlbumEditPermissionException;
import com.momentry.BE.domain.album.exception.NoAlbumPermissionException;
import com.momentry.BE.domain.album.exception.TagNotFoundException;
import com.momentry.BE.domain.album.repository.AlbumMemberRepository;
import com.momentry.BE.domain.album.repository.AlbumTagRepository;
import com.momentry.BE.domain.file.dto.FilePageResult;
import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.file.entity.FileType;
import com.momentry.BE.domain.file.repository.FileRepository;
import com.momentry.BE.domain.file.repository.FileTagInfoRepository;
import com.momentry.BE.domain.user.entity.AccountPlan;
import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.global.exception.CursorDecodeFailException;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private AlbumTagRepository albumTagRepository;

    @Mock
    private AlbumMemberRepository albumMemberRepository;

    @Mock
    private FileTagInfoRepository fileTagInfoRepository;

    @Mock
    private FileRepository fileRepository;

    private AlbumService albumService;
    private Album album;
    private AlbumMember albumMember;

    @BeforeEach
    void setUp() {
        albumService = new AlbumService(albumTagRepository, albumMemberRepository, fileTagInfoRepository, fileRepository);
        album = Album.builder().name("test").build();
        AlbumPermission permission = AlbumPermission.builder().permission("EDITOR").build();
        AccountPlan plan = AccountPlan.builder().plan("FREE").build();
        User user = User.builder().email("a@b.com").username("user").accountPlan(plan).build();
        albumMember = AlbumMember.builder().album(album).user(user).permission(permission).build();
    }

    @Test
    void getFiles_firstPage_returnsCursorAndHasNext() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 19, 12, 0);
        File file1 = buildFile(album, createdAt, 200L);
        File file2 = buildFile(album, createdAt.minusMinutes(1), 199L);

        when(albumMemberRepository.findByAlbumIdAndUserIdWithAlbum(1L, 10L))
                .thenReturn(java.util.Optional.of(albumMember));
        when(fileRepository.findByAlbumOrderByCreatedAtDescIdDesc(eq(album), any(Pageable.class)))
                .thenReturn(List.of(file1, file2));

        FilePageResult result = albumService.getFiles(1L, null, null, 1, 10L);

        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotBlank();

        String payload = new String(Base64.getUrlDecoder().decode(result.getNextCursor()), StandardCharsets.UTF_8);
        assertThat(payload).isEqualTo(createdAt + "|" + 200L);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(fileRepository).findByAlbumOrderByCreatedAtDescIdDesc(eq(album), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(2);
    }

    @Test
    void getFiles_withCursor_usesCursorQuery() {
        LocalDateTime cursorCreatedAt = LocalDateTime.of(2026, 1, 19, 12, 0);
        String cursor = encodeCursor(cursorCreatedAt, 200L);
        File file = buildFile(album, cursorCreatedAt.minusMinutes(1), 199L);

        when(albumMemberRepository.findByAlbumIdAndUserIdWithAlbum(1L, 10L))
                .thenReturn(java.util.Optional.of(albumMember));
        when(fileRepository.findByAlbumWithCursor(eq(album), eq(cursorCreatedAt), eq(200L), any(Pageable.class)))
                .thenReturn(List.of(file));

        FilePageResult result = albumService.getFiles(1L, null, cursor, 2, 10L);

        assertThat(result.isHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNotBlank();
        verify(fileRepository).findByAlbumWithCursor(eq(album), eq(cursorCreatedAt), eq(200L), any(Pageable.class));
    }

    @Test
    void getFiles_invalidCursor_throwsCursorDecodeFailException() {
        when(albumMemberRepository.findByAlbumIdAndUserIdWithAlbum(1L, 10L))
                .thenReturn(java.util.Optional.of(albumMember));

        assertThatThrownBy(() -> albumService.getFiles(1L, null, "invalid-cursor", 10, 10L))
                .isInstanceOf(CursorDecodeFailException.class);
    }

    @Test
    void createTag_requiresEditPermission() {
        AlbumMember viewerMember = memberWithPermission("VIEWER");
        when(albumMemberRepository.findByAlbumIdAndUserIdWithAlbum(1L, 10L))
                .thenReturn(java.util.Optional.of(viewerMember));

        assertThatThrownBy(() -> albumService.createTag(1L, "tag", 10L))
                .isInstanceOf(NoAlbumEditPermissionException.class);
    }

    @Test
    void createTag_savesTagWithAlbum() {
        when(albumMemberRepository.findByAlbumIdAndUserIdWithAlbum(1L, 10L))
                .thenReturn(java.util.Optional.of(albumMember));

        albumService.createTag(1L, "tag", 10L);

        ArgumentCaptor<AlbumTag> tagCaptor = ArgumentCaptor.forClass(AlbumTag.class);
        verify(albumTagRepository).save(tagCaptor.capture());
        assertThat(tagCaptor.getValue().getAlbum()).isEqualTo(album);
        assertThat(tagCaptor.getValue().getTagName()).isEqualTo("tag");
    }

    @Test
    void deleteTag_withoutPermission_throwsException() {
        when(albumMemberRepository.findByAlbumIdAndUserId(1L, 10L))
                .thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> albumService.deleteTag(1L, 2L, 10L))
                .isInstanceOf(NoAlbumPermissionException.class);
    }

    @Test
    void deleteTag_tagMismatch_throwsTagNotFound() {
        when(albumMemberRepository.findByAlbumIdAndUserId(1L, 10L))
                .thenReturn(java.util.Optional.of(albumMember));
        when(albumTagRepository.findByIdAndAlbumId(2L, 1L))
                .thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> albumService.deleteTag(1L, 2L, 10L))
                .isInstanceOf(TagNotFoundException.class);
    }

    @Test
    void updateTag_requiresEditPermission() {
        AlbumMember viewerMember = memberWithPermission("VIEWER");
        when(albumMemberRepository.findByAlbumIdAndUserId(1L, 10L))
                .thenReturn(java.util.Optional.of(viewerMember));

        assertThatThrownBy(() -> albumService.updateTag(1L, 2L, "new", 10L))
                .isInstanceOf(NoAlbumEditPermissionException.class);
    }

    @Test
    void getTags_returnsResults() {
        when(albumMemberRepository.findByAlbumIdAndUserIdWithAlbum(1L, 10L))
                .thenReturn(java.util.Optional.of(albumMember));
        AlbumTag tag = AlbumTag.builder().album(album).tagName("tag").build();
        when(albumTagRepository.findByAlbum(album)).thenReturn(List.of(tag));

        List<AlbumTagResult> results = albumService.getTags(1L, 10L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTagName()).isEqualTo("tag");
    }

    private File buildFile(Album album, LocalDateTime createdAt, Long id) {
        AccountPlan plan = AccountPlan.builder().plan("FREE").build();
        User user = User.builder().email("test@a.com").username("user").accountPlan(plan).build();
        File file = File.builder()
                .album(album)
                .originUrl("https://example.com/original.jpg")
                .thumbUrl("https://example.com/thumb.jpg")
                .displayUrl("https://example.com/display.jpg")
                .fileType(FileType.IMAGE)
                .uploader(user)
                .createdAt(createdAt)
                .build();
        ReflectionTestUtils.setField(file, "id", id);
        return file;
    }

    private String encodeCursor(LocalDateTime createdAt, Long id) {
        String payload = createdAt + "|" + id;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    private AlbumMember memberWithPermission(String permissionValue) {
        AlbumPermission permission = AlbumPermission.builder().permission(permissionValue).build();
        AccountPlan plan = AccountPlan.builder().plan("FREE").build();
        User user = User.builder().email("viewer@a.com").username("viewer").accountPlan(plan).build();
        return AlbumMember.builder().album(album).user(user).permission(permission).build();
    }
}
