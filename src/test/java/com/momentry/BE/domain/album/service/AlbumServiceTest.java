package com.momentry.BE.domain.album.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.momentry.BE.domain.album.dto.AlbumMemberInviteResult;
import com.momentry.BE.domain.album.dto.AlbumTagResult;
import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.album.entity.AlbumMember;
import com.momentry.BE.domain.album.entity.AlbumTag;
import com.momentry.BE.domain.album.entity.MemberAlbumPermission;
import com.momentry.BE.domain.album.exception.AlbumMemberNotFoundException;
import com.momentry.BE.domain.album.exception.AlbumNotFoundException;
import com.momentry.BE.domain.album.exception.CannotKickManagerException;
import com.momentry.BE.domain.album.exception.InvalidAlbumInviteRequestException;
import com.momentry.BE.domain.album.exception.NoAlbumEditPermissionException;
import com.momentry.BE.domain.album.exception.NoAlbumMemberEditPermissionException;
import com.momentry.BE.domain.album.exception.NoAlbumPermissionException;
import com.momentry.BE.domain.album.exception.TagNotFoundException;
import com.momentry.BE.domain.album.repository.AlbumMemberRepository;
import com.momentry.BE.domain.album.repository.AlbumRepository;
import com.momentry.BE.domain.album.repository.AlbumRepository;
import com.momentry.BE.domain.album.repository.AlbumTagRepository;
import com.momentry.BE.domain.user.repository.UserRepository;
import com.momentry.BE.domain.file.dto.FilePageResult;
import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.file.entity.FileType;
import com.momentry.BE.domain.file.repository.FileRepository;
import com.momentry.BE.domain.file.repository.FileTagInfoRepository;
import com.momentry.BE.domain.user.entity.AccountPlan;
import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.domain.user.repository.UserRepository;
import com.momentry.BE.global.exception.CursorDecodeFailException;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

        @Mock
        private AlbumRepository albumRepository;

        @Mock
        private AlbumTagRepository albumTagRepository;

        @Mock
        private AlbumMemberRepository albumMemberRepository;

        @Mock
        private FileTagInfoRepository fileTagInfoRepository;

        @Mock
        private FileRepository fileRepository;

        @Mock
        private UserRepository userRepository;

        private AlbumService albumService;
        private Album album;
        private AlbumMember albumMember;

        @BeforeEach
        void setUp() {
                albumService = new AlbumService(albumRepository, albumTagRepository, albumMemberRepository,
                                fileTagInfoRepository, fileRepository, userRepository);
                album = Album.builder().name("test").build();
                AccountPlan plan = AccountPlan.builder().plan("FREE").build();
                User user = User.builder().email("a@b.com").username("user").accountPlan(plan).build();
                albumMember = AlbumMember.builder().album(album).user(user).permission(MemberAlbumPermission.EDITOR)
                                .build();
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

                String payload = new String(Base64.getUrlDecoder().decode(result.getNextCursor()),
                                StandardCharsets.UTF_8);
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
                when(fileRepository.findByAlbumWithCursor(eq(album), eq(cursorCreatedAt), eq(200L),
                                any(Pageable.class)))
                                .thenReturn(List.of(file));

                FilePageResult result = albumService.getFiles(1L, null, cursor, 2, 10L);

                assertThat(result.isHasNext()).isFalse();
                assertThat(result.getNextCursor()).isNotBlank();
                verify(fileRepository).findByAlbumWithCursor(eq(album), eq(cursorCreatedAt), eq(200L),
                                any(Pageable.class));
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
                verify(albumTagRepository).saveAndFlush(tagCaptor.capture());
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

        @Test
        @DisplayName("멤버 초대 - Manager가 멤버를 성공적으로 초대한다")
        void inviteMembers_asManager_success() {
                // given: Manager 권한을 가진 사용자가 2명의 사용자를 초대하는 상황 설정
                Long albumId = 1L;
                Long requesterId = 10L;
                List<Long> inviteeIds = List.of(20L, 30L);

                User managerUser = User.builder().email("manager@test.com").username("manager")
                                .accountPlan(AccountPlan.builder().plan("FREE").build()).build();
                ReflectionTestUtils.setField(managerUser, "id", requesterId);
                AlbumMember managerMember = AlbumMember.builder()
                                .album(album)
                                .user(managerUser)
                                .permission(MemberAlbumPermission.MANAGER)
                                .build();

                ReflectionTestUtils.setField(album, "id", albumId);

                User invitee1 = User.builder()
                                .email("invitee1@test.com")
                                .username("invitee1")
                                .profileImageUrl("https://example.com/profile1.jpg")
                                .accountPlan(AccountPlan.builder().plan("FREE").build())
                                .build();
                ReflectionTestUtils.setField(invitee1, "id", 20L);

                User invitee2 = User.builder()
                                .email("invitee2@test.com")
                                .username("invitee2")
                                .profileImageUrl("https://example.com/profile2.jpg")
                                .accountPlan(AccountPlan.builder().plan("FREE").build())
                                .build();
                ReflectionTestUtils.setField(invitee2, "id", 30L);

                when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
                when(albumMemberRepository.findByAlbumIdAndUserId(albumId, requesterId))
                                .thenReturn(Optional.of(managerMember));
                when(userRepository.findAllById(inviteeIds))
                                .thenReturn(List.of(invitee1, invitee2));

                // when: 멤버 초대 서비스 메서드 호출
                AlbumMemberInviteResult result = albumService.inviteMembers(albumId, inviteeIds, requesterId);

                // then: 초대 결과 검증 및 저장된 멤버 정보 확인
                assertThat(result.getAlbumId()).isEqualTo(albumId);
                assertThat(result.getInvited()).hasSize(2);
                assertThat(result.getInvited().get(0).getEmail()).isEqualTo("invitee1@test.com");
                assertThat(result.getInvited().get(0).getUserId()).isEqualTo(20L);
                assertThat(result.getInvited().get(1).getEmail()).isEqualTo("invitee2@test.com");
                assertThat(result.getInvited().get(1).getUserId()).isEqualTo(30L);

                ArgumentCaptor<AlbumMember> memberCaptor = ArgumentCaptor.forClass(AlbumMember.class);
                verify(albumMemberRepository, times(2)).save(memberCaptor.capture());
                List<AlbumMember> capturedMembers = memberCaptor.getAllValues();
                assertThat(capturedMembers).hasSize(2);
                assertThat(capturedMembers.get(0).getAlbum()).isEqualTo(album);
                assertThat(capturedMembers.get(0).getPermission()).isEqualTo(MemberAlbumPermission.VIEWER);
                assertThat(capturedMembers.get(1).getAlbum()).isEqualTo(album);
                assertThat(capturedMembers.get(1).getPermission()).isEqualTo(MemberAlbumPermission.VIEWER);
        }

        @Test
        @DisplayName("멤버 초대 - Editor가 멤버 초대를 시도하면 권한 예외가 발생한다")
        void inviteMembers_asEditor_throwsNoAlbumMemberEditPermissionException() {
                // given: Editor 권한을 가진 사용자가 멤버 초대를 시도하는 상황 설정
                Long albumId = 1L;
                Long requesterId = 10L;
                List<Long> inviteeIds = List.of(20L);

                User editorUser = User.builder().email("editor@test.com").username("editor")
                                .accountPlan(AccountPlan.builder().plan("FREE").build()).build();
                ReflectionTestUtils.setField(editorUser, "id", requesterId);
                AlbumMember editorMember = AlbumMember.builder()
                                .album(album)
                                .user(editorUser)
                                .permission(MemberAlbumPermission.EDITOR)
                                .build();

                ReflectionTestUtils.setField(album, "id", albumId);

                when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
                when(albumMemberRepository.findByAlbumIdAndUserId(albumId, requesterId))
                                .thenReturn(Optional.of(editorMember));

                // when & then
                assertThatThrownBy(() -> albumService.inviteMembers(albumId, inviteeIds, requesterId))
                                .isInstanceOf(NoAlbumMemberEditPermissionException.class);
        }

        @Test
        @DisplayName("멤버 초대 - Viewer가 멤버 초대를 시도하면 권한 예외가 발생한다")
        void inviteMembers_asViewer_throwsNoAlbumMemberEditPermissionException() {
                // given: Viewer 권한을 가진 사용자가 멤버 초대를 시도하는 상황 설정
                Long albumId = 1L;
                Long requesterId = 10L;
                List<Long> inviteeIds = List.of(20L);

                AlbumMember viewerMember = memberWithPermission("VIEWER");

                ReflectionTestUtils.setField(album, "id", albumId);

                when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
                when(albumMemberRepository.findByAlbumIdAndUserId(albumId, requesterId))
                                .thenReturn(Optional.of(viewerMember));

                // when & then
                assertThatThrownBy(() -> albumService.inviteMembers(albumId, inviteeIds, requesterId))
                                .isInstanceOf(NoAlbumMemberEditPermissionException.class);
        }

        @Test
        @DisplayName("멤버 초대 - 존재하지 않는 앨범에 초대를 시도하면 예외가 발생한다")
        void inviteMembers_albumNotFound_throwsAlbumNotFoundException() {
                // given: 존재하지 않는 앨범 ID로 초대를 시도하는 상황 설정
                Long albumId = 999L;
                Long requesterId = 10L;
                List<Long> inviteeIds = List.of(20L);

                when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> albumService.inviteMembers(albumId, inviteeIds, requesterId))
                                .isInstanceOf(AlbumNotFoundException.class);
        }

        @Test
        @DisplayName("멤버 초대 - 존재하지 않는 사용자를 초대하면 예외가 발생한다")
        void inviteMembers_userNotFound_throwsInvalidAlbumInviteRequestException() {
                // given: 존재하지 않는 사용자 ID로 초대를 시도하는 상황 설정
                Long albumId = 1L;
                Long requesterId = 10L;
                List<Long> inviteeIds = List.of(999L);

                User managerUser = User.builder().email("manager@test.com").username("manager")
                                .accountPlan(AccountPlan.builder().plan("FREE").build()).build();
                ReflectionTestUtils.setField(managerUser, "id", requesterId);
                AlbumMember managerMember = AlbumMember.builder()
                                .album(album)
                                .user(managerUser)
                                .permission(MemberAlbumPermission.MANAGER)
                                .build();

                ReflectionTestUtils.setField(album, "id", albumId);

                when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
                when(albumMemberRepository.findByAlbumIdAndUserId(albumId, requesterId))
                                .thenReturn(Optional.of(managerMember));
                when(userRepository.findAllById(inviteeIds))
                                .thenReturn(new ArrayList<>());

                // when & then
                assertThatThrownBy(() -> albumService.inviteMembers(albumId, inviteeIds, requesterId))
                                .isInstanceOf(InvalidAlbumInviteRequestException.class);
        }

        @Test
        @DisplayName("멤버 초대 - 이미 멤버인 사용자를 초대하면 예외가 발생한다")
        void inviteMembers_duplicateMember_throwsInvalidAlbumInviteRequestException() {
                // given: 이미 앨범 멤버인 사용자를 다시 초대하려는 상황 설정 (중복 저장 예외 발생)
                Long albumId = 1L;
                Long requesterId = 10L;
                Long inviteeId = 20L;
                List<Long> inviteeIds = List.of(inviteeId);

                User managerUser = User.builder().email("manager@test.com").username("manager")
                                .accountPlan(AccountPlan.builder().plan("FREE").build()).build();
                ReflectionTestUtils.setField(managerUser, "id", requesterId);
                AlbumMember managerMember = AlbumMember.builder()
                                .album(album)
                                .user(managerUser)
                                .permission(MemberAlbumPermission.MANAGER)
                                .build();

                User invitee = User.builder()
                                .email("invitee@test.com")
                                .username("invitee")
                                .accountPlan(AccountPlan.builder().plan("FREE").build())
                                .build();
                ReflectionTestUtils.setField(invitee, "id", inviteeId);

                ReflectionTestUtils.setField(album, "id", albumId);

                when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
                when(albumMemberRepository.findByAlbumIdAndUserId(albumId, requesterId))
                                .thenReturn(Optional.of(managerMember));
                when(userRepository.findAllById(inviteeIds))
                                .thenReturn(List.of(invitee));
                when(albumMemberRepository.save(any(AlbumMember.class)))
                                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

                // when & then
                assertThatThrownBy(() -> albumService.inviteMembers(albumId, inviteeIds, requesterId))
                                .isInstanceOf(InvalidAlbumInviteRequestException.class);
        }

        @Test
        @DisplayName("멤버 권한 변경 - Manager가 멤버 권한을 성공적으로 변경한다")
        void updateMemberPermission_asManager_success() {
                // given: Manager 권한을 가진 사용자가 다른 멤버의 권한을 변경하는 상황 설정
                Long albumId = 1L;
                Long requesterId = 10L;
                Long targetMemberId = 20L;
                MemberAlbumPermission newPermission = MemberAlbumPermission.EDITOR;

                User managerUser = User.builder().email("manager@test.com").username("manager")
                                .accountPlan(AccountPlan.builder().plan("FREE").build()).build();
                ReflectionTestUtils.setField(managerUser, "id", requesterId);
                AlbumMember requester = AlbumMember.builder()
                                .album(album)
                                .user(managerUser)
                                .permission(MemberAlbumPermission.MANAGER)
                                .build();

                User targetUser = User.builder().email("target@test.com").username("target")
                                .accountPlan(AccountPlan.builder().plan("FREE").build()).build();
                ReflectionTestUtils.setField(targetUser, "id", targetMemberId);
                AlbumMember targetMember = AlbumMember.builder()
                                .album(album)
                                .user(targetUser)
                                .permission(MemberAlbumPermission.VIEWER)
                                .build();

                when(albumMemberRepository.findByAlbumIdAndUserId(albumId, requesterId))
                                .thenReturn(Optional.of(requester));
                when(albumMemberRepository.findByAlbumIdAndUserId(albumId, targetMemberId))
                                .thenReturn(Optional.of(targetMember));

                // when: 멤버 권한 변경 서비스 메서드 호출
                albumService.updateMemberPermission(albumId, targetMemberId, newPermission, requesterId);

                // then: 대상 멤버의 권한이 변경되었는지 확인
                assertThat(targetMember.getPermission()).isEqualTo(MemberAlbumPermission.EDITOR);
        }

        @Test
        @DisplayName("멤버 권한 변경 - Editor가 멤버 권한 변경을 시도하면 권한 예외가 발생한다")
        void updateMemberPermission_asEditor_throwsNoAlbumMemberEditPermissionException() {
                // given: Editor 권한을 가진 사용자가 멤버 권한 변경을 시도하는 상황 설정
                Long albumId = 1L;
                Long requesterId = 10L;
                Long targetMemberId = 20L;
                MemberAlbumPermission newPermission = MemberAlbumPermission.EDITOR;

                AlbumMember editorMember = memberWithPermission("EDITOR");

                when(albumMemberRepository.findByAlbumIdAndUserId(albumId, requesterId))
                                .thenReturn(Optional.of(editorMember));

                // when & then
                assertThatThrownBy(() -> albumService.updateMemberPermission(albumId, targetMemberId, newPermission,
                                requesterId))
                                .isInstanceOf(NoAlbumMemberEditPermissionException.class);
        }

        @Test
        @DisplayName("멤버 권한 변경 - Viewer가 멤버 권한 변경을 시도하면 권한 예외가 발생한다")
        void updateMemberPermission_asViewer_throwsNoAlbumMemberEditPermissionException() {
                // given: Viewer 권한을 가진 사용자가 멤버 권한 변경을 시도하는 상황 설정
                Long albumId = 1L;
                Long requesterId = 10L;
                Long targetMemberId = 20L;
                MemberAlbumPermission newPermission = MemberAlbumPermission.EDITOR;

                AlbumMember viewerMember = memberWithPermission("VIEWER");

                when(albumMemberRepository.findByAlbumIdAndUserId(albumId, requesterId))
                                .thenReturn(Optional.of(viewerMember));

                // when & then
                assertThatThrownBy(() -> albumService.updateMemberPermission(albumId, targetMemberId, newPermission,
                                requesterId))
                                .isInstanceOf(NoAlbumMemberEditPermissionException.class);
        }

        @Test
        @DisplayName("멤버 권한 변경 - 존재하지 않는 멤버의 권한을 변경하려고 하면 예외가 발생한다")
        void updateMemberPermission_memberNotFound_throwsAlbumMemberNotFoundException() {
                // given: 존재하지 않는 멤버의 권한을 변경하려는 상황 설정
                Long albumId = 1L;
                Long requesterId = 10L;
                Long targetMemberId = 999L;
                MemberAlbumPermission newPermission = MemberAlbumPermission.EDITOR;

                User managerUser = User.builder().email("manager@test.com").username("manager")
                                .accountPlan(AccountPlan.builder().plan("FREE").build()).build();
                ReflectionTestUtils.setField(managerUser, "id", requesterId);
                AlbumMember requester = AlbumMember.builder()
                                .album(album)
                                .user(managerUser)
                                .permission(MemberAlbumPermission.MANAGER)
                                .build();

                when(albumMemberRepository.findByAlbumIdAndUserId(albumId, requesterId))
                                .thenReturn(Optional.of(requester));
                when(albumMemberRepository.findByAlbumIdAndUserId(albumId, targetMemberId))
                                .thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> albumService.updateMemberPermission(albumId, targetMemberId, newPermission,
                                requesterId))
                                .isInstanceOf(AlbumMemberNotFoundException.class);
        }

        @Test
        @DisplayName("멤버 강퇴 - Manager가 멤버를 성공적으로 강퇴한다")
        void kickMember_asManager_success() {
                // given: Manager 권한을 가진 사용자가 다른 멤버를 강퇴하는 상황 설정
                Long albumId = 1L;
                Long requesterId = 10L;
                Long targetMemberId = 20L;

                User managerUser = User.builder().email("manager@test.com").username("manager")
                                .accountPlan(AccountPlan.builder().plan("FREE").build()).build();
                ReflectionTestUtils.setField(managerUser, "id", requesterId);
                AlbumMember requester = AlbumMember.builder()
                                .album(album)
                                .user(managerUser)
                                .permission(MemberAlbumPermission.MANAGER)
                                .build();

                User targetUser = User.builder().email("target@test.com").username("target")
                                .accountPlan(AccountPlan.builder().plan("FREE").build()).build();
                ReflectionTestUtils.setField(targetUser, "id", targetMemberId);
                AlbumMember targetMember = AlbumMember.builder()
                                .album(album)
                                .user(targetUser)
                                .permission(MemberAlbumPermission.VIEWER)
                                .build();

                when(albumMemberRepository.findByAlbumIdAndUserId(albumId, requesterId))
                                .thenReturn(Optional.of(requester));
                when(albumMemberRepository.findByAlbumIdAndUserId(albumId, targetMemberId))
                                .thenReturn(Optional.of(targetMember));

                // when: 멤버 강퇴 서비스 메서드 호출
                albumService.kickMember(albumId, targetMemberId, requesterId);

                // then: 멤버 삭제가 호출되었는지 확인
                verify(albumMemberRepository).delete(targetMember);
        }

        @Test
        @DisplayName("멤버 강퇴 - Editor가 멤버 강퇴를 시도하면 권한 예외가 발생한다")
        void kickMember_asEditor_throwsNoAlbumMemberEditPermissionException() {
                // given: Editor 권한을 가진 사용자가 멤버 강퇴를 시도하는 상황 설정
                Long albumId = 1L;
                Long requesterId = 10L;
                Long targetMemberId = 20L;

                AlbumMember editorMember = memberWithPermission("EDITOR");

                when(albumMemberRepository.findByAlbumIdAndUserId(albumId, requesterId))
                                .thenReturn(Optional.of(editorMember));

                // when & then
                assertThatThrownBy(() -> albumService.kickMember(albumId, targetMemberId, requesterId))
                                .isInstanceOf(NoAlbumMemberEditPermissionException.class);
        }

        @Test
        @DisplayName("멤버 강퇴 - Viewer가 멤버 강퇴를 시도하면 권한 예외가 발생한다")
        void kickMember_asViewer_throwsNoAlbumMemberEditPermissionException() {
                // given: Viewer 권한을 가진 사용자가 멤버 강퇴를 시도하는 상황 설정
                Long albumId = 1L;
                Long requesterId = 10L;
                Long targetMemberId = 20L;

                AlbumMember viewerMember = memberWithPermission("VIEWER");

                when(albumMemberRepository.findByAlbumIdAndUserId(albumId, requesterId))
                                .thenReturn(Optional.of(viewerMember));

                // when & then
                assertThatThrownBy(() -> albumService.kickMember(albumId, targetMemberId, requesterId))
                                .isInstanceOf(NoAlbumMemberEditPermissionException.class);
        }

        @Test
        @DisplayName("멤버 강퇴 - 존재하지 않는 멤버를 강퇴하려고 하면 예외가 발생한다")
        void kickMember_memberNotFound_throwsAlbumMemberNotFoundException() {
                // given: 존재하지 않는 멤버를 강퇴하려는 상황 설정
                Long albumId = 1L;
                Long requesterId = 10L;
                Long targetMemberId = 999L;

                User managerUser = User.builder().email("manager@test.com").username("manager")
                                .accountPlan(AccountPlan.builder().plan("FREE").build()).build();
                ReflectionTestUtils.setField(managerUser, "id", requesterId);
                AlbumMember requester = AlbumMember.builder()
                                .album(album)
                                .user(managerUser)
                                .permission(MemberAlbumPermission.MANAGER)
                                .build();

                when(albumMemberRepository.findByAlbumIdAndUserId(albumId, requesterId))
                                .thenReturn(Optional.of(requester));
                when(albumMemberRepository.findByAlbumIdAndUserId(albumId, targetMemberId))
                                .thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> albumService.kickMember(albumId, targetMemberId, requesterId))
                                .isInstanceOf(AlbumMemberNotFoundException.class);
        }

        @Test
        @DisplayName("멤버 강퇴 - Manager가 다른 Manager를 강퇴하려고 하면 예외가 발생한다")
        void kickMember_managerKickingManager_throwsCannotKickManagerException() {
                // given: Manager 권한을 가진 사용자가 다른 Manager를 강퇴하려는 상황 설정
                Long albumId = 1L;
                Long requesterId = 10L;
                Long targetMemberId = 20L;

                User requesterUser = User.builder().email("manager1@test.com").username("manager1")
                                .accountPlan(AccountPlan.builder().plan("FREE").build()).build();
                ReflectionTestUtils.setField(requesterUser, "id", requesterId);
                AlbumMember requester = AlbumMember.builder()
                                .album(album)
                                .user(requesterUser)
                                .permission(MemberAlbumPermission.MANAGER)
                                .build();

                User targetUser = User.builder().email("manager2@test.com").username("manager2")
                                .accountPlan(AccountPlan.builder().plan("FREE").build()).build();
                ReflectionTestUtils.setField(targetUser, "id", targetMemberId);
                AlbumMember targetMember = AlbumMember.builder()
                                .album(album)
                                .user(targetUser)
                                .permission(MemberAlbumPermission.MANAGER)
                                .build();

                when(albumMemberRepository.findByAlbumIdAndUserId(albumId, requesterId))
                                .thenReturn(Optional.of(requester));
                when(albumMemberRepository.findByAlbumIdAndUserId(albumId, targetMemberId))
                                .thenReturn(Optional.of(targetMember));

                // when & then
                assertThatThrownBy(() -> albumService.kickMember(albumId, targetMemberId, requesterId))
                                .isInstanceOf(CannotKickManagerException.class);
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
                MemberAlbumPermission permission = MemberAlbumPermission.valueOf(permissionValue);
                AccountPlan plan = AccountPlan.builder().plan("FREE").build();
                User user = User.builder().email("viewer@a.com").username("viewer").accountPlan(plan).build();
                return AlbumMember.builder().album(album).user(user).permission(permission).build();
        }
}
