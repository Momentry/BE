package com.momentry.BE.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import com.momentry.BE.domain.user.dto.UpdateUserInfoRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.momentry.BE.domain.album.dto.AlbumCountDto;
import com.momentry.BE.domain.album.dto.AlbumHeaderDto;
import com.momentry.BE.domain.album.dto.AlbumUrlDto;
import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.album.repository.AlbumMemberRepository;
import com.momentry.BE.domain.album.service.AlbumService;
import com.momentry.BE.domain.album.util.CoverImageResolver;
import com.momentry.BE.domain.file.dto.LikedFileDto;
import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.file.entity.FileType;
import com.momentry.BE.domain.file.repository.FileLikeRepository;
import com.momentry.BE.domain.file.repository.FileRepository;
import com.momentry.BE.domain.user.dto.GetCurrentUserAlbumListResponse;
import com.momentry.BE.domain.user.dto.GetCurrentUserLikedFileListResponse;
import com.momentry.BE.domain.user.dto.UserUpdateResponse;
import com.momentry.BE.domain.user.entity.AccountPlan;
import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.domain.user.exception.UserNotFoundException;
import com.momentry.BE.domain.user.service.master.UserMasterService;
import com.momentry.BE.domain.user.service.sub.AlertPreferenceService;
import com.momentry.BE.domain.user.service.sub.UserService;

@ExtendWith(MockitoExtension.class)
public class UserMasterServiceTest {
    @InjectMocks
    private UserMasterService userMasterService;

    @Mock
    private UserService userService;
    @Mock
    private AlertPreferenceService alertPreferenceService;
    @Mock
    private AlbumService albumService;
    @Mock
    private AlbumMemberRepository albumMemberRepository;
    @Mock
    private FileRepository fileRepository;
    @Mock
    private FileLikeRepository fileLikeRepository;
    @Mock
    private CoverImageResolver coverImageResolver;

    @Test
    @DisplayName("1. 사용자 정보 수정 - 성공")
    void updateUser_Success() {
        // 1. Given
        Long userId = 1L;
        String newUsername = "new_name";
        MultipartFile mockFile = mock(MultipartFile.class);
        UpdateUserInfoRequest request = new UpdateUserInfoRequest(newUsername);
        UserUpdateResponse expectedResponse = new UserUpdateResponse(newUsername);

        // userService.update가 호출되면 준비한 응답을 반환하도록 설정 (matcher는 모두 사용)
        when(userService.update(anyLong(), any(UpdateUserInfoRequest.class)))
                .thenReturn(expectedResponse);

        // 2. When
        UserUpdateResponse actualResponse = userMasterService.updateUser(userId, request);

        // 3. Then
        assertThat(actualResponse.getUserName()).isEqualTo(expectedResponse.getUserName());

        verify(userService, times(1)).update(userId, request);
    }

    @Test
    @DisplayName("2. 사용자 정보 수정 - 실패 - 존재하지 않는 유저")
    void updateUser_Fail_UserNotFound() {
        // 1. Given
        Long notFoundUserId = 999L;
        String newUsername = "fail_name";
        MultipartFile mockFile = mock(MultipartFile.class);
        UpdateUserInfoRequest request = new UpdateUserInfoRequest(newUsername);

        // userService.update 호출 시 EntityNotFoundException이 발생하도록 설정
        when(userService.update(eq(notFoundUserId), any()))
                .thenThrow(new UserNotFoundException());

        // 2. When & Then
        assertThatThrownBy(() -> userMasterService.updateUser(notFoundUserId, request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("3. 내 앨범 목록 조회 - 성공")
    void getCurrentUserAlbums_Success() {
        // 1. Given
        AccountPlan plan = AccountPlan.builder().plan("FREE").build();
        Long testUserId = 1L;
        User testUser = User.builder()
                .email("test1@example.com")
                .username("testuser1")
                .profileImageUrl("https://example.com/profile1.jpg")
                .accountPlan(plan)
                .build();
        ReflectionTestUtils.setField(testUser, "id", testUserId);

        when(userService.getCurrentUser(testUserId)).thenReturn(testUser);

        Long testAlbum1Id = 1L;
        Long testAlbum2Id = 2L;
        Album testAlbum1 = Album.builder()
                .name("test album1")
                .coverImageUrl("https://example.com/cover1.jpg")
                .build();
        Album testAlbum2 = Album.builder()
                .name("test album2")
                .coverImageUrl("https://example.com/cover2.jpg")
                .build();
        ReflectionTestUtils.setField(testAlbum1, "id", testAlbum1Id);
        ReflectionTestUtils.setField(testAlbum2, "id", testAlbum2Id);
        ReflectionTestUtils.setField(testAlbum1, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(testAlbum2, "createdAt", LocalDateTime.now());

        when(albumService.getJoinedAlbums(testUser)).thenReturn(List.of(testAlbum1, testAlbum2));

        List<Long> albumIds = List.of(testAlbum1Id, testAlbum2Id);

        AlbumCountDto testAlbum1MemberCount = new AlbumCountDto(testAlbum1Id, 2);
        AlbumCountDto testAlbum2MemberCount = new AlbumCountDto(testAlbum2Id, 3);
        AlbumCountDto testAlbum1FileCount = new AlbumCountDto(testAlbum1Id, 3);
        AlbumCountDto testAlbum2FileCount = new AlbumCountDto(testAlbum2Id, 4);

        when(albumMemberRepository.countMembersByAlbumIds(albumIds)).thenReturn(
                List.of(
                        testAlbum1MemberCount, testAlbum2MemberCount));

        when(fileRepository.countFilesByAlbumIds(albumIds)).thenReturn(
                List.of(
                        testAlbum1FileCount, testAlbum2FileCount));

        AlbumUrlDto testAlbum1Member1ProfileUrl = new AlbumUrlDto(testAlbum1Id,
                "https://example.com/profile1.jpg");
        AlbumUrlDto testAlbum1Member2ProfileUrl = new AlbumUrlDto(testAlbum1Id,
                "https://example.com/profile2.jpg");
        AlbumUrlDto testAlbum2Member1ProfileUrl = new AlbumUrlDto(testAlbum2Id,
                "https://example.com/profile3.jpg");
        AlbumUrlDto testAlbum2Member2ProfileUrl = new AlbumUrlDto(testAlbum2Id,
                "https://example.com/profile4.jpg");
        AlbumUrlDto testAlbum2Member3ProfileUrl = new AlbumUrlDto(testAlbum2Id,
                "https://example.com/profile5.jpg");

        when(albumMemberRepository.findMemberProfilesByAlbumIds(albumIds)).thenReturn(
                List.of(
                        testAlbum1Member1ProfileUrl, testAlbum1Member2ProfileUrl,
                        testAlbum2Member1ProfileUrl, testAlbum2Member2ProfileUrl,
                        testAlbum2Member3ProfileUrl));

        when(coverImageResolver.resolve(any())).thenAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            return arg != null ? arg.toString() : null;
        });

        // 2. When
        GetCurrentUserAlbumListResponse response = userMasterService.getCurrentUserAlbums(testUserId);

        // 3. Then
        // 첫 번째 앨범(test album1) 검증
        AlbumHeaderDto dto1 = response.getAlbums().get(0);
        assertThat(dto1.getAlbumId()).isEqualTo(testAlbum1Id);
        assertThat(dto1.getAlbumName()).isEqualTo("test album1");
        assertThat(dto1.getThumbnailUrl()).isEqualTo("https://example.com/cover1.jpg");
        assertThat(dto1.getMemberCount()).isEqualTo(2);
        assertThat(dto1.getFileCount()).isEqualTo(3);
        assertThat(dto1.getMemberProfiles()).hasSize(2)
                .containsExactly("https://example.com/profile1.jpg",
                        "https://example.com/profile2.jpg");

        // 두 번째 앨범(test album2) 검증
        // 두 번째 앨범(test album2) 전수 조사
        AlbumHeaderDto dto2 = response.getAlbums().get(1);
        assertThat(dto2.getAlbumId()).isEqualTo(testAlbum2Id);
        assertThat(dto2.getAlbumName()).isEqualTo("test album2");
        assertThat(dto2.getThumbnailUrl()).isEqualTo("https://example.com/cover2.jpg");
        assertThat(dto2.getMemberCount()).isEqualTo(3);
        assertThat(dto2.getFileCount()).isEqualTo(4);
        // 앨범2 멤버 프로필 리스트 검증
        assertThat(dto2.getMemberProfiles())
                .hasSize(3)
                .containsExactly(
                        "https://example.com/profile3.jpg",
                        "https://example.com/profile4.jpg",
                        "https://example.com/profile5.jpg");

        // 메서드 호출 여부 검증 (verify)
        verify(userService).getCurrentUser(testUserId);
        verify(albumService).getJoinedAlbums(testUser);
        verify(albumMemberRepository).countMembersByAlbumIds(albumIds);
        verify(fileRepository).countFilesByAlbumIds(albumIds);
        verify(albumMemberRepository).findMemberProfilesByAlbumIds(albumIds);
    }

    @Test
    @DisplayName("4. 내 앨범 목록 조회 - 성공 - 앨범이 하나도 없음")
    void getCurrentUserAlbums_Success_No_Album() {
        // 1. Given
        AccountPlan plan = AccountPlan.builder().plan("FREE").build();
        Long testUserId = 1L;
        User testUser = User.builder()
                .email("test1@example.com")
                .username("testuser1")
                .profileImageUrl("https://example.com/profile1.jpg")
                .accountPlan(plan)
                .build();
        ReflectionTestUtils.setField(testUser, "id", testUserId);

        when(userService.getCurrentUser(testUserId)).thenReturn(testUser);

        when(albumService.getJoinedAlbums(testUser)).thenReturn(List.of());

        // 2. When
        GetCurrentUserAlbumListResponse response = userMasterService.getCurrentUserAlbums(testUserId);

        // 3. Then
        assertThat(response.getAlbums()).isEmpty();

        // 메서드 호출 여부 검증 (verify)
        verify(userService).getCurrentUser(testUserId);
        verify(albumService).getJoinedAlbums(testUser);
        verify(albumMemberRepository, never()).countMembersByAlbumIds(any());
        verify(fileRepository, never()).countFilesByAlbumIds(any());
        verify(albumMemberRepository, never()).findMemberProfilesByAlbumIds(any());
    }

    @Test
    @DisplayName("5. 내 앨범 목록 조회 - 실패 - 존재하지 않는 유저")
    void getCurrentUserAlbums_UserNotFound() {
        // 1. Given
        Long notFoundUserId = 999L;

        // userService.update 호출 시 EntityNotFoundException이 발생하도록 설정
        when(userService.getCurrentUser(notFoundUserId))
                .thenThrow(new UserNotFoundException());

        // 2. When & Then
        assertThatThrownBy(() -> userMasterService.getCurrentUserAlbums(notFoundUserId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("6. 내 좋아요 파일 목록 조회 - 성공")
    void getCurrentUserLikedFile_Success() {
        // 1. Given
        AccountPlan plan = AccountPlan.builder().plan("FREE").build();
        Long testUserId = 1L;
        User testUser = User.builder()
                .email("test1@example.com")
                .username("testuser1")
                .profileImageUrl("https://example.com/profile1.jpg")
                .accountPlan(plan)
                .build();
        ReflectionTestUtils.setField(testUser, "id", testUserId);

        when(userService.getCurrentUser(testUserId)).thenReturn(testUser);

        Long testAlbum1Id = 1L;
        Long testAlbum2Id = 2L;
        Album testAlbum1 = Album.builder()
                .name("test album1")
                .coverImageUrl("https://example.com/cover1.jpg")
                .build();
        Album testAlbum2 = Album.builder()
                .name("test album2")
                .coverImageUrl("https://example.com/cover2.jpg")
                .build();
        ReflectionTestUtils.setField(testAlbum1, "id", testAlbum1Id);
        ReflectionTestUtils.setField(testAlbum2, "id", testAlbum2Id);
        ReflectionTestUtils.setField(testAlbum1, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(testAlbum2, "createdAt", LocalDateTime.now());

        Long testFile1Id = 1L;
        Long testFile2Id = 2L;
        File testFile1 = File.builder()
                .album(testAlbum1)
                .fileType(FileType.IMAGE)
                .uploader(testUser)
                .thumbUrl("https://example.com/thumb1.jpg")
                .displayUrl("https://example.com/display1.jpg")
                .originUrl("https://example.com/origin1.jpg")
                .metadata("meta1")
                .fileKey("testKey1")
                .build();

        File testFile2 = File.builder()
                .album(testAlbum2)
                .fileType(FileType.IMAGE)
                .uploader(testUser)
                .thumbUrl("https://example.com/thumb2.jpg")
                .displayUrl("https://example.com/display2.jpg")
                .originUrl("https://example.com/origin2.jpg")
                .metadata("meta2")
                .fileKey("testKey2")
                .build();

        ReflectionTestUtils.setField(testFile1, "id", testFile1Id);
        ReflectionTestUtils.setField(testFile2, "id", testFile2Id);

        int page = 0;
        int size = 20;
        Pageable pageable = PageRequest.of(page, size + 1);
        List<File> likedFileSlice = List.of(testFile1, testFile2);

        when(fileLikeRepository.findLikedFileByUserId(testUserId, pageable)).thenReturn(
                likedFileSlice);

        // 2. When
        GetCurrentUserLikedFileListResponse response = userMasterService.getCurrentUserLikedFile(testUserId,
                null, size);

        // 3 Then
        assertThat(response.getLikedFiles()).hasSize(2);

        LikedFileDto responseFile1 = response.getLikedFiles().get(0);
        assertThat(responseFile1.getFileId()).isEqualTo(testFile1Id);
        assertThat(responseFile1.getThumbnailUrl()).isEqualTo("https://example.com/thumb1.jpg");
        assertThat(responseFile1.getType()).isEqualTo(FileType.IMAGE.name());
        assertThat(responseFile1.getAlbumId()).isEqualTo(testAlbum1Id);
        assertThat(responseFile1.getAlbumName()).isEqualTo("test album1");

        LikedFileDto responseFile2 = response.getLikedFiles().get(1);
        assertThat(responseFile2.getFileId()).isEqualTo(testFile2Id);
        assertThat(responseFile2.getThumbnailUrl()).isEqualTo("https://example.com/thumb2.jpg");
        assertThat(responseFile2.getType()).isEqualTo(FileType.IMAGE.name());
        assertThat(responseFile2.getAlbumId()).isEqualTo(testAlbum2Id);
        assertThat(responseFile2.getAlbumName()).isEqualTo("test album2");

        verify(userService).getCurrentUser(testUserId);
        verify(fileLikeRepository).findLikedFileByUserId(testUserId, pageable);
    }

    @Test
    @DisplayName("6. 내 좋아요 파일 목록 조회 - 실패 - 존재하지 않는 유저")
    void getCurrentUserLikedFile_UserNotFound() {
        // 1. Given
        Long notFoundUserId = 999L;
        int size = 20;

        // userService.update 호출 시 EntityNotFoundException이 발생하도록 설정
        when(userService.getCurrentUser(notFoundUserId))
                .thenThrow(new UserNotFoundException());

        // 2. When & Then
        assertThatThrownBy(() -> userMasterService.getCurrentUserLikedFile(notFoundUserId, null, size))
                .isInstanceOf(UserNotFoundException.class);
    }
}
