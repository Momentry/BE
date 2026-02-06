package com.momentry.BE.domain.user.service.master;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.momentry.BE.domain.album.dto.AlbumCountDto;
import com.momentry.BE.domain.album.dto.AlbumHeaderDto;
import com.momentry.BE.domain.album.dto.AlbumUrlDto;
import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.album.repository.AlbumMemberRepository;
import com.momentry.BE.domain.album.service.AlbumService;
import com.momentry.BE.domain.file.dto.FileResult;
import com.momentry.BE.domain.file.dto.LikedFileDto;
import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.file.repository.FileLikeRepository;
import com.momentry.BE.domain.file.repository.FileRepository;
import com.momentry.BE.domain.user.dto.GetCurrentUserAlbumListResponse;
import com.momentry.BE.domain.user.dto.GetCurrentUserFileListResponse;
import com.momentry.BE.domain.user.dto.GetCurrentUserLikedFileListResponse;
import com.momentry.BE.domain.user.dto.LoginResponse;
import com.momentry.BE.domain.user.dto.UserUpdateResponse;
import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.domain.user.service.sub.AlertPreferenceService;
import com.momentry.BE.domain.user.service.sub.UserService;
import com.momentry.BE.global.dto.FileCursor;
import com.momentry.BE.global.exception.CursorDecodeFailException;
import com.momentry.BE.global.service.CloudFrontSignedCookieService;
import com.momentry.BE.global.util.CursorUtil;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserMasterService {
    private final UserService userService;
    private final AlertPreferenceService alertPreferenceService;
    private final AlbumService albumService;
    private final CloudFrontSignedCookieService cloudFrontSignedCookieService;

    // TODO : 나중에 서비스 레이어가 나오면 교체하기
    private final AlbumMemberRepository albumMemberRepository;
    private final FileRepository fileRepository;
    private final FileLikeRepository fileLikeRepository;

    @Transactional
    public UserUpdateResponse updateUser(Long userId, MultipartFile file, String newUsername) {
        return userService.update(userId, file, newUsername);
    }

    // SOFT DELETE
    // TODO : 30일 이후에 관련 엔티티 삭제 로직 추가
    public void signOut(Long userId) {
        User currentUser = userService.getCurrentUser(userId);

        userService.withdrawUser(currentUser);
    }

    @Transactional
    public void updateAlertPreference(LoginResponse.AlertDto request, Long userId) {
        User user = userService.getCurrentUser(userId);
        alertPreferenceService.updateAlertPreference(request, user);
    }

    @Transactional(readOnly = true)
    public void refreshCloudFrontCookie(Long userId, HttpServletResponse response) {
        User user = userService.getCurrentUser(userId);
        List<Long> albumIds = albumService.getAlbumIds(user);
        HttpHeaders headers = cloudFrontSignedCookieService
                .buildSignedCookieHeaders(String.valueOf(user.getId()), albumIds);
        headers.forEach((name, values) -> values.forEach(value -> response.addHeader(name, value)));
    }


    @Transactional(readOnly = true)
    public GetCurrentUserAlbumListResponse getCurrentUserAlbums(Long userId) {
        User user = userService.getCurrentUser(userId);

        // 사용자가 속한 앨범 list 가져오기
        List<Album> albums = albumService.getJoinedAlbums(user);
        List<Long> albumIds = albums.stream().map(Album::getId).toList();

        if (albumIds.isEmpty()) {
            // 앨범이 없는 사람
            return new GetCurrentUserAlbumListResponse(List.of());
        }

        // 각 앨범의 멤버 수, 파일 수를 한번에 가져오기
        Map<Long, Integer> memberCountMap = albumMemberRepository.countMembersByAlbumIds(albumIds)
                .stream().collect(Collectors.toMap(
                        AlbumCountDto::getAlbumId,
                        AlbumCountDto::getCount));

        Map<Long, Integer> fileCountMap = fileRepository.countFilesByAlbumIds(albumIds)
                .stream().collect(Collectors.toMap(
                        AlbumCountDto::getAlbumId,
                        AlbumCountDto::getCount));

        // 각 앨범의 멤버 프로필 이미지 URL (앨범당 최대 6명, username 순)
        Map<Long, List<String>> memberProfileImageMap = albumMemberRepository.findMemberProfilesByAlbumIds(albumIds)
                .stream()
                .collect(Collectors.groupingBy(
                        AlbumUrlDto::getAlbumId,
                        Collectors.mapping(AlbumUrlDto::getUrl, Collectors.toList())));

        // 앨범 별로 albumHeaderDto를 생성해서 list를 만듬
        List<AlbumHeaderDto> albumHeaders = albums.stream().map(album -> {
            Long albumId = album.getId();
            List<String> profiles = memberProfileImageMap.getOrDefault(albumId, List.of()).stream()
                    .limit(6)
                    .toList();
            return AlbumHeaderDto.builder()
                    .albumId(albumId)
                    .albumName(album.getName())
                    .thumbnailUrl(album.getCoverImageUrl())
                    .memberCount(memberCountMap.getOrDefault(albumId, 0))
                    .fileCount(fileCountMap.getOrDefault(albumId, 0))
                    .memberProfiles(profiles)
                    .createdAt(album.getCreatedAt().toLocalDate())
                    .build();
        }).toList();

        return new GetCurrentUserAlbumListResponse(albumHeaders);
    }

    @Transactional(readOnly = true)
    public GetCurrentUserLikedFileListResponse getCurrentUserLikedFile(Long userId, String cursor, int pageSize) {
        User user = userService.getCurrentUser(userId);

        FileCursor decodedCursor = parseCursor(cursor);
        Pageable pageable = PageRequest.of(0, pageSize + 1);

        // 사용자의 좋아요 목록에 있는 파일 리스트를 좋아요의 최신순으로 가져오기
        List<File> likedFiles = (decodedCursor == null)
                ? fileLikeRepository.findLikedFileByUserId(user.getId(), pageable)
                : fileLikeRepository.findLikedFileByUserIdWithCursor(user.getId(), decodedCursor.getCreatedAt(),
                        decodedCursor.getId(), pageable);

        boolean hasNext = likedFiles.size() > pageSize;

        if (hasNext) {
            likedFiles = likedFiles.subList(0, pageSize);
        }

        String nextCursor = null;
        if (!likedFiles.isEmpty()) {
            File lastFile = likedFiles.get(likedFiles.size() - 1);
            nextCursor = CursorUtil.encodeCursor(lastFile.getCreatedAt(), lastFile.getId());
        }

        // Dto로 가공하기
        List<LikedFileDto> likedFileListDto = likedFiles.stream()
                .map(LikedFileDto::new)
                .toList();

        // 반환하기
        return new GetCurrentUserLikedFileListResponse(likedFileListDto, nextCursor);
    }

    @Transactional(readOnly = true)
    public GetCurrentUserFileListResponse getCurrentUserFileList(Long userId, String cursor) {
        User user = userService.getCurrentUser(userId);

        List<Album> albums = albumService.getJoinedAlbums(user);
        List<Long> albumIds = albums.stream().map(Album::getId).toList();

        if (albumIds.isEmpty()) {
            return new GetCurrentUserFileListResponse(List.of(), null);
        }

        FileCursor decodedCursor = parseCursor(cursor);
        int pageSize = 20;
        Pageable pageable = PageRequest.of(0, pageSize + 1);

        List<File> files = (decodedCursor == null)
                ? fileRepository.findByAlbumIdsOrderByCreatedAtDescIdDesc(albumIds, pageable)
                : fileRepository.findByAlbumIdsWithCursor(albumIds, decodedCursor.getCreatedAt(), decodedCursor.getId(),
                        pageable);

        boolean hasNext = files.size() > pageSize;
        if (hasNext) {
            files = files.subList(0, pageSize);
        }

        String nextCursor = null;
        if (!files.isEmpty()) {
            File lastFile = files.get(files.size() - 1);
            nextCursor = CursorUtil.encodeCursor(lastFile.getCreatedAt(), lastFile.getId());
        }

        return new GetCurrentUserFileListResponse(files.stream().map(FileResult::of).toList(), nextCursor);
    }

    private FileCursor parseCursor(String cursor) {
        String[] parts = CursorUtil.decodeCursorParts(cursor);
        if (parts == null) {
            return null;
        }
        try {
            LocalDateTime createdAt = LocalDateTime.parse(parts[0]);
            Long id = Long.parseLong(parts[1]);
            return new FileCursor(createdAt, id);
        } catch (DateTimeParseException | NumberFormatException e) {
            throw new CursorDecodeFailException();
        }
    }
}
