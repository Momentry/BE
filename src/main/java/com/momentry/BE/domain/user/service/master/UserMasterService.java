package com.momentry.BE.domain.user.service.master;

import com.momentry.BE.domain.album.dto.AlbumCountDto;
import com.momentry.BE.domain.album.dto.AlbumHeaderDto;
import com.momentry.BE.domain.album.dto.AlbumUrlDto;
import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.album.repository.AlbumMemberRepository;
import com.momentry.BE.domain.album.service.AlbumService;
import com.momentry.BE.domain.file.dto.LikedFileDto;
import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.file.repository.FileLikeRepository;
import com.momentry.BE.domain.file.repository.FileRepository;
import com.momentry.BE.domain.user.dto.GetCurrentUserAlbumListResponse;
import com.momentry.BE.domain.user.dto.GetCurrentUserLikedFileListResponse;
import com.momentry.BE.domain.user.dto.LoginResponse;
import com.momentry.BE.domain.user.dto.UserUpdateResponse;
import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.domain.user.service.sub.AlertPreferenceService;
import com.momentry.BE.domain.user.service.sub.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserMasterService {
    private final UserService userService;
    private final AlertPreferenceService alertPreferenceService;
    private final AlbumService albumService;
    
    // TODO : 나중에 서비스 레이어가 나오면 교체하기
    private final AlbumMemberRepository albumMemberRepository;
    private final FileRepository fileRepository;
    private final FileLikeRepository fileLikeRepository;

    @Transactional
    public UserUpdateResponse updateUser(Long userId, MultipartFile file, String newUsername){
        return userService.update(userId, file, newUsername);
    }

    // SOFT DELETE
    // TODO : 30일 이후에 관련 엔티티 삭제 로직 추가
    public void signOut(Long userId){
        User currentUser = userService.getCurrentUser(userId);

        userService.withdrawUser(currentUser);
    }

    @Transactional
    public void updateAlertPreference(LoginResponse.AlertDto request, Long userId){
        User user = userService.getCurrentUser(userId);
        alertPreferenceService.updateAlertPreference(request, user);
    }

    @Transactional(readOnly = true)
    public GetCurrentUserAlbumListResponse getCurrentUserAlbums(Long userId){
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

        // 각 앨범의 file.thumbUrl을 최신순으로 앨범 별로 가져오기
        Map<Long, List<String>> memberProfileImageMap = albumMemberRepository.findMemberProfilesByAlbumIds(albumIds, Limit.of(6))
                .stream()
                .collect(Collectors.groupingBy(
                        AlbumUrlDto::getAlbumId, // 그룹화 기준 (Key)
                        Collectors.mapping(AlbumUrlDto::getUrl, Collectors.toList()) // 벨류 변환 및 리스트 수집 (Value)
                ));

        Map<Long, List<String>> fileThumbnailImageMap = fileRepository.findThumbnailsByAlbumIds(albumIds, Limit.of(6))
                .stream()
                .collect(Collectors.groupingBy(
                        AlbumUrlDto::getAlbumId,
                        Collectors.mapping(AlbumUrlDto::getUrl, Collectors.toList())
                ));
        
        // 앨범 별로 albumHeaderDto를 생성해서 list를 만듬
        List<AlbumHeaderDto> albumHeaders = albums.stream().map(album -> {
            Long albumId = album.getId();

            return AlbumHeaderDto.builder()
                    .albumId(albumId)
                    .albumName(album.getName())
                    .thumbnailUrl(album.getCoverImageUrl())
                    .memberCount(memberCountMap.getOrDefault(albumId, 0))
                    .fileCount(fileCountMap.getOrDefault(albumId, 0))
                    .memberProfiles(memberProfileImageMap.get(albumId))
                    .fileThumbnails(fileThumbnailImageMap.get(albumId))
                    .createdAt(album.getCreatedAt().toLocalDate())
                    .build();
        }).toList();

        return new GetCurrentUserAlbumListResponse(albumHeaders);
    }

    @Transactional(readOnly = true)
    public GetCurrentUserLikedFileListResponse getCurrentUserLikedFile(Long userId, int page, int size){
        User user = userService.getCurrentUser(userId);

        Pageable pageable = PageRequest.of(page, size);

        // 사용자의 좋아요 목록에 있는 파일 리스트를 좋아요의 최신순으로 가져오기
        Slice<File> likedFiles = fileLikeRepository.findLikedFileByUserId(user.getId(), pageable);

        // Dto로 가공하기
        List<LikedFileDto> likedFileListDto = likedFiles.getContent().stream()
                .map(LikedFileDto::new)
                .toList();
        
        // 반환하기
        return new GetCurrentUserLikedFileListResponse(likedFileListDto, likedFiles.hasNext());
    }
}
