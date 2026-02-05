package com.momentry.BE.domain.album.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.momentry.BE.domain.album.dto.AlbumCreationResponse;
import com.momentry.BE.domain.album.dto.AlbumDetailResponse;
import com.momentry.BE.domain.album.dto.AlbumMemberInviteResult;
import com.momentry.BE.domain.album.dto.AlbumTagResult;
import com.momentry.BE.domain.album.dto.InvitedMemberResult;
import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.album.entity.AlbumMember;
import com.momentry.BE.domain.album.entity.AlbumTag;
import com.momentry.BE.domain.album.entity.MemberAlbumPermission;
import com.momentry.BE.domain.album.exception.AlbumMemberNotFoundException;
import com.momentry.BE.domain.album.exception.AlbumMustHaveManagerException;
import com.momentry.BE.domain.album.exception.AlbumNotFoundException;
import com.momentry.BE.domain.album.exception.CannotKickManagerException;
import com.momentry.BE.domain.album.exception.DuplicateAlbumNameException;
import com.momentry.BE.domain.album.exception.DuplicateTagException;
import com.momentry.BE.domain.album.exception.InvalidAlbumInviteRequestException;
import com.momentry.BE.domain.album.exception.NoAlbumEditPermissionException;
import com.momentry.BE.domain.album.exception.NoAlbumMemberEditPermissionException;
import com.momentry.BE.domain.album.exception.NoAlbumPermissionException;
import com.momentry.BE.domain.album.exception.TagLimitExceededException;
import com.momentry.BE.domain.album.exception.TagNotFoundException;
import com.momentry.BE.domain.album.repository.AlbumMemberRepository;
import com.momentry.BE.domain.album.repository.AlbumRepository;
import com.momentry.BE.domain.album.repository.AlbumTagRepository;
import com.momentry.BE.domain.file.dto.FilePageResult;
import com.momentry.BE.domain.file.dto.FileResult;
import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.file.entity.FileTagInfo;
import com.momentry.BE.domain.file.repository.FileRepository;
import com.momentry.BE.domain.file.repository.FileTagInfoRepository;
import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.domain.user.repository.UserRepository;
import com.momentry.BE.global.dto.FileCursor;
import com.momentry.BE.global.exception.CursorDecodeFailException;
import com.momentry.BE.global.util.CursorUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final AlbumTagRepository albumTagRepository;
    private final AlbumMemberRepository albumMemberRepository;
    private final FileTagInfoRepository fileTagInfoRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    // 나중에 s3에서 가져오도록 변경 필요
    private static final String DEFAULT_COVER_IMAGE_URL = "https://images.unsplash.com/photo-1511497584788-876760111969?w=800";

    /**
     * 앨범 생성
     * 
     * @param albumName  앨범 이름
     * @param coverImage 커버 이미지 파일 (null이면 default 이미지 사용)
     * @param userId     사용자 ID (앨범 생성자)
     * @return 앨범 생성 응답
     */
    @Transactional
    public AlbumCreationResponse createAlbum(String albumName,
            org.springframework.web.multipart.MultipartFile coverImage, Long userId) {
        // 앨범 이름 중복 체크
        if (albumRepository.findByName(albumName).isPresent()) {
            throw new DuplicateAlbumNameException();
        }

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 커버 이미지 URL 설정
        String finalCoverImageUrl = DEFAULT_COVER_IMAGE_URL;
        if (coverImage != null && !coverImage.isEmpty()) {
            // S3 업로드 서비스 구현 시 여기서 파일 업로드 처리
            // finalCoverImageUrl = s3UploadService.uploadFile(coverImage);
            // 현재는 파일이 있어도 업로드하지 않고 default 이미지 사용
        }

        // 앨범 생성
        Album album = Album.builder()
                .name(albumName)
                .coverImageUrl(finalCoverImageUrl)
                .build();

        Album savedAlbum = albumRepository.save(album);

        // 앨범 멤버 생성 (생성자는 MANAGER 권한)
        AlbumMember albumMember = AlbumMember.builder()
                .user(user)
                .album(savedAlbum)
                .permission(MemberAlbumPermission.MANAGER)
                .build();

        albumMemberRepository.save(albumMember);

        return new AlbumCreationResponse(savedAlbum.getId(), savedAlbum.getName());
    }

    /**
     * 앨범 정보 수정
     * 
     * @param albumId    앨범 ID
     * @param albumName  앨범 이름 (null이면 업데이트 안함)
     * @param coverImage 커버 이미지 파일 (null이면 업데이트 안함)
     * @param userId     사용자 ID
     */
    @Transactional
    public void updateAlbum(Long albumId, String albumName, org.springframework.web.multipart.MultipartFile coverImage,
            Long userId) {
        // 권한 확인 (편집 권한 필요)
        AlbumMember albumMember = getAlbumPermission(albumId, userId);
        requireEditPermission(albumMember.getPermission());

        // 앨범 조회
        Album album = albumRepository.findById(albumId)
                .orElseThrow(AlbumNotFoundException::new);

        // 앨범 이름 업데이트 (제공된 경우)
        if (albumName != null && !albumName.isBlank()) {
            // 다른 앨범과 이름 중복 체크 (자기 자신은 제외)
            albumRepository.findByName(albumName)
                    .ifPresent(existingAlbum -> {
                        if (!existingAlbum.getId().equals(albumId)) {
                            throw new DuplicateAlbumNameException();
                        }
                    });
            album.setName(albumName);
        }

        // 커버 이미지 업데이트 (제공된 경우)
        if (coverImage != null && !coverImage.isEmpty()) {
            // S3 업로드 서비스 구현 시 여기서 파일 업로드 처리
            // String coverImageUrl = s3UploadService.uploadFile(coverImage);
            // album.setCoverImageUrl(coverImageUrl);
            // 현재는 파일이 있어도 업로드하지 않고 기존 이미지 유지
        }

        albumRepository.save(album);
    }

    /**
     * 앨범 나가기
     *
     * @param albumId 앨범 ID
     * @param userId  사용자 ID
     * @return 앨범이 삭제되었는지 여부 (true: 삭제됨, false: 일반 나가기)
     */
    @Transactional
    public boolean leaveAlbum(Long albumId, Long userId) {
        AlbumMember albumMember = getAlbumPermission(albumId, userId);
        List<AlbumMember> albumMembers = albumMemberRepository.findByAlbumIdWithUser(albumId);

        // 1. 나 말고 다른 MANAGER 있나?
        boolean hasOtherManager = albumMembers.stream()
                .filter(member -> !member.getUser().getId().equals(userId))
                .anyMatch(member -> member.getPermission() == MemberAlbumPermission.MANAGER);

        if (hasOtherManager) {
            // 다른 MANAGER가 있으면 나가기 가능
            albumMemberRepository.delete(albumMember);
            return false;
        }

        // 2. 다른 MANAGER 없어 (본인이 유일한 MANAGER)
        // 내가 유일한 멤버야?
        if (albumMembers.size() == 1 && albumMember.getUser().getId().equals(userId)) {
            // 앨범 삭제
            deleteAlbum(albumId);
            return true;
        } else {
            // 다른 멤버가 있으면 예외 (다음 관리자 지정 필요)
            throw new AlbumMustHaveManagerException();
        }
    }

    /**
     * 앨범 삭제
     * 
     * @param albumId 앨범 ID
     */
    @Transactional
    public void deleteAlbum(Long albumId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(AlbumNotFoundException::new);

        // 관련 파일 태그 정보 삭제 (FileTagInfo는 File을 참조)
        // 태그를 먼저 삭제하면 FileTagInfo도 함께 삭제됨 (cascade 또는 외래키 제약조건)
        List<AlbumTag> tags = albumTagRepository.findByAlbum(album);
        for (AlbumTag tag : tags) {
            fileTagInfoRepository.deleteByTag(tag);
        }

        // 관련 파일 삭제
        List<File> files = fileRepository.findByAlbumOrderByCreatedAtDescIdDesc(album,
                PageRequest.of(0, Integer.MAX_VALUE));
        fileRepository.deleteAll(files);

        // 앨범 삭제 (cascade로 태그와 멤버도 함께 삭제됨)
        albumRepository.delete(album);
    }

    /**
     * 앨범 태그 생성
     * 
     * @param albumId 앨범 ID
     * @param tagName 태그 이름
     * @param userId  사용자 ID
     */
    @Transactional
    public void createTag(Long albumId, String tagName, Long userId) {
        AlbumMember albumMember = getAlbumPermissionWithAlbum(albumId, userId);

        requireEditPermission(albumMember.getPermission());

        AlbumTag tag = AlbumTag.builder()
                .album(albumMember.getAlbum())
                .tagName(tagName)
                .build();

        if (albumTagRepository.countByAlbum(albumMember.getAlbum()) >= 10) {
            throw new TagLimitExceededException();
        }

        try {
            albumTagRepository.saveAndFlush(tag);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateTagException();
        }
    }


    public List<Album> getJoinedAlbums(User user){
        return albumMemberRepository.findAlbumsByUserId(user.getId());
    }

    public List<Long> getAlbumIds(User user) {
        return albumMemberRepository.findAlbumIdsByUserId(user.getId());
    }

    /**
     * 앨범 멤버 초대
     *
     * @param albumId 앨범 ID
     * @param userIds 초대할 사용자 ID 목록
     * @param userId  초대 요청자(현재 사용자) ID
     * @return 초대 결과(앨범 ID, 초대된 사용자 목록)
     */
    @Transactional
    public AlbumMemberInviteResult inviteMembers(Long albumId, List<Long> userIds, Long userId) {
        Album album = getAlbum(albumId);
        AlbumMember inviterMember = getAlbumPermission(albumId, userId);

        requireMemberEditPermission(inviterMember.getPermission());

        List<User> users = getInviteUsers(userIds);

        List<InvitedMemberResult> invitedResults = new ArrayList<>();
        for (User invitee : users) {
            AlbumMember albumMember = AlbumMember.builder()
                    .user(invitee)
                    .album(album)
                    .permission(MemberAlbumPermission.VIEWER)
                    .build();

            try {
                albumMemberRepository.save(albumMember);
            } catch (DataIntegrityViolationException e) {
                throw new InvalidAlbumInviteRequestException();
            }

            invitedResults.add(new InvitedMemberResult(
                    invitee.getEmail(),
                    invitee.getId(),
                    invitee.getUsername(),
                    invitee.getProfileImageUrl()));
        }

        return new AlbumMemberInviteResult(album.getId(), invitedResults);
    }

    /**
     * 앨범 권한만 가져오기
     * 
     * @param albumId 앨범 ID
     * @param userId  사용자 ID
     * @return AlbumMember
     */
    private AlbumMember getAlbumPermission(Long albumId, Long userId) {
        return albumMemberRepository.findByAlbumIdAndUserId(albumId, userId)
                .orElseThrow(NoAlbumPermissionException::new);
    }

    /**
     * 멤버 편집 권한 확인
     * Manager 이상의 권한을 가져야 함
     *
     * @implNote 권한이 MANAGER가 아닌 경우 NoAlbumMemberEditPermissionException 예외를 발생시킴
     * @param permission 권한
     */
    private void requireMemberEditPermission(MemberAlbumPermission permission) {
        if (!permission.canManageMembers()) {
            throw new NoAlbumMemberEditPermissionException();
        }
    }

    /**
     * Manager가 다른 Manager를 강퇴하는 것을 방지
     *
     * @implNote 요청자와 대상 멤버가 모두 MANAGER인 경우 CannotKickManagerException 예외를 발생시킴
     * @param requester    요청자(강퇴를 시도하는 멤버)
     * @param targetMember 강퇴 대상 멤버
     */
    private void preventManagerKickingManager(AlbumMember requester, AlbumMember targetMember) {
        MemberAlbumPermission requesterPermission = requester.getPermission();
        MemberAlbumPermission targetPermission = targetMember.getPermission();
        if (requesterPermission == MemberAlbumPermission.MANAGER
                && targetPermission == MemberAlbumPermission.MANAGER) {
            throw new CannotKickManagerException();
        }
    }

    /**
     * 앨범 ID로 앨범을 조회
     *
     * @implNote 앨범을 찾을 수 없는 경우 AlbumNotFoundException 예외를 발생시킴
     *
     * @param albumId 앨범 ID
     * @return Album
     */
    private Album getAlbum(Long albumId) {
        return albumRepository.findById(albumId)
                .orElseThrow(AlbumNotFoundException::new);
    }

    /**
     * 초대 대상 사용자 목록을 조회
     * 
     * @implNote 일부라도 존재하지 않으면 InvalidAlbumInviteRequestException 예외를 발생시킴
     *
     * @param userIds 초대할 사용자 ID 목록
     * @return 사용자 엔티티 목록
     */
    private List<User> getInviteUsers(List<Long> userIds) {
        List<User> users = userRepository.findAllById(userIds);
        if (users.size() != userIds.size()) {
            throw new InvalidAlbumInviteRequestException();
        }
        return users;
    }

    /**
     * 앨범 권한을 가져오고 앨범을 함께 반환 (fetch join)
     * 
     * @param albumId 앨범 ID
     * @param userId  사용자 ID
     * @return AlbumMember
     */
    private AlbumMember getAlbumPermissionWithAlbum(Long albumId, Long userId) {
        return albumMemberRepository.findByAlbumIdAndUserIdWithAlbum(albumId, userId)
                .orElseThrow(NoAlbumPermissionException::new);
    }

    /**
     * 앨범 멤버 권한 변경
     *
     * @param albumId          앨범 ID
     * @param memberId         권한을 변경할 멤버(사용자) ID
     * @param targetPermission 부여할 권한 종류 (manager, editor, viewer)
     * @param userId           요청자(현재 사용자) ID
     */
    @Transactional
    public void updateMemberPermission(Long albumId, Long memberId, MemberAlbumPermission targetPermission,
            Long userId) {
        AlbumMember requester = getAlbumPermission(albumId, userId);
        requireMemberEditPermission(requester.getPermission());

        AlbumMember targetMember = getAlbumMember(albumId, memberId);

        targetMember.changePermission(targetPermission);
    }

    /**
     * 앨범의 특정 멤버 조회
     *
     * @implNote 멤버를 찾을 수 없는 경우 AlbumMemberNotFoundException 예외를 발생시킴
     *
     * @param albumId  앨범 ID
     * @param memberId 멤버(사용자) ID
     * @return AlbumMember
     */
    private AlbumMember getAlbumMember(Long albumId, Long memberId) {
        return albumMemberRepository.findByAlbumIdAndUserId(albumId, memberId)
                .orElseThrow(AlbumMemberNotFoundException::new);
    }

    /**
     * 앨범 멤버 강퇴
     *
     * @param albumId  앨범 ID
     * @param memberId 강퇴시킬 멤버(사용자) ID
     * @param userId   요청자(현재 사용자) ID
     */
    @Transactional
    public void kickMember(Long albumId, Long memberId, Long userId) {
        AlbumMember requester = getAlbumPermission(albumId, userId);
        requireMemberEditPermission(requester.getPermission());

        AlbumMember targetMember = getAlbumMember(albumId, memberId);
        preventManagerKickingManager(requester, targetMember);

        albumMemberRepository.delete(targetMember);
    }

    /**
     * 앨범의 태그를 삭제
     * 
     * @param albumId 앨범 ID
     * @param tagId   태그 ID
     * @param userId  사용자 ID
     */
    @Transactional
    public void deleteTag(Long albumId, Long tagId, Long userId) {

        AlbumMember albumMember = getAlbumPermission(albumId, userId);

        requireEditPermission(albumMember.getPermission());

        AlbumTag tag = getTagByIdAndAlbumId(tagId, albumId);

        fileTagInfoRepository.deleteByTag(tag);
        albumTagRepository.deleteById(tagId);
    }

    /**
     * 앨범 편집 권한 확인
     * Editor 이상의 권한을 가져야 함 (사진/카테고리 관리)
     * 
     * @implNote 권한이 EDITOR 미만(VIEWER 등)인 경우 NoAlbumEditPermissionException 예외를 발생시킴
     * @param permission 권한
     */
    private void requireEditPermission(MemberAlbumPermission permission) {
        if (!permission.canEditAlbum()) {
            throw new NoAlbumEditPermissionException();
        }
    }

    /**
     * 앨범의 태그를 업데이트
     * 
     * @param albumId 앨범 ID
     * @param tagId   태그 ID
     * @param tagName 태그 이름
     * @param userId  사용자 ID
     */
    @Transactional
    public void updateTag(Long albumId, Long tagId, String tagName, Long userId) {

        AlbumMember albumMember = getAlbumPermission(albumId, userId);

        requireEditPermission(albumMember.getPermission());

        AlbumTag tag = getTagByIdAndAlbumId(tagId, albumId);

        tag.setTagName(tagName);

        albumTagRepository.save(tag);
    }

    /**
     * 앨범 상세 정보를 조회
     * 
     * @param albumId 앨범 ID
     * @param userId  사용자 ID
     * @return 앨범 상세 정보
     */
    public AlbumDetailResponse getAlbumDetail(Long albumId, Long userId) {
        // 권한 확인
        getAlbumPermission(albumId, userId);

        // 앨범 조회
        Album album = albumRepository.findById(albumId)
                .orElseThrow(AlbumNotFoundException::new);

        // 파일 개수 조회
        long fileCount = fileRepository.countByAlbum(album);

        return new AlbumDetailResponse(
                album.getName(),
                album.getCoverImageUrl(),
                fileCount);
    }

    /**
     * 앨범의 태그 목록을 조회
     * 
     * @param albumId 앨범 ID
     * @param userId  사용자 ID
     * @return 태그 목록
     */
    public List<AlbumTagResult> getTags(Long albumId, Long userId) {
        AlbumMember albumMember = getAlbumPermissionWithAlbum(albumId, userId);

        List<AlbumTag> tags = albumTagRepository.findByAlbum(albumMember.getAlbum());

        List<AlbumTagResult> result = new ArrayList<>();
        for (AlbumTag tag : tags) {
            result.add(new AlbumTagResult(tag.getId(), tag.getTagName(), tag.getCount()));
        }
        return result;
    }

    /**
     * 앨범의 파일 목록을 조회
     * 
     * @param albumId 앨범 ID
     * @param tagId   태그 ID
     * @param cursor  커서
     * @param size    페이지 크기
     * @param userId  사용자 ID
     * @return 파일 페이지 결과
     */
    public FilePageResult getFiles(Long albumId, Long tagId, String cursor, int size, Long userId) {
        AlbumMember albumMember = getAlbumPermissionWithAlbum(albumId, userId);

        int pageSize = Math.max(size, 1);
        PageRequest pageable = PageRequest.of(0, pageSize + 1);
        FileCursor decodedCursor = parseCursor(cursor);

        if (tagId != null) {
            getTagByIdAndAlbumId(tagId, albumId);
            List<FileTagInfo> fileTagInfos = fetchTagInfosByTag(tagId, decodedCursor, pageable);
            return toPageResult(fileTagInfos, pageSize, info -> FileResult.of(info.getFile()),
                    info -> info.getFile().getCreatedAt(), info -> info.getFile().getId());
        }

        List<File> files = fetchFilesByAlbum(albumMember.getAlbum(), decodedCursor, pageable);
        return toPageResult(files, pageSize, FileResult::of, File::getCreatedAt, File::getId);
    }

    private List<FileTagInfo> fetchTagInfosByTag(Long tagId, FileCursor cursor, PageRequest pageable) {
        if (cursor == null) {
            return fileTagInfoRepository.fetchByTag(tagId, pageable);
        }

        return fileTagInfoRepository.fetchByTagWithCursor(tagId, cursor.getCreatedAt(), cursor.getId(), pageable);
    }

    /**
     * 앨범의 파일 목록을 조회
     * 
     * @param album    앨범
     * @param cursor   커서
     * @param pageable 페이지 요청
     * @return 파일 목록
     */
    private List<File> fetchFilesByAlbum(Album album, FileCursor cursor, PageRequest pageable) {
        if (cursor == null) {
            return fileRepository.findByAlbumOrderByCreatedAtDescIdDesc(album, pageable);
        }
        return fileRepository.findByAlbumWithCursor(album, cursor.getCreatedAt(), cursor.getId(), pageable);
    }

    private AlbumTag getTagByIdAndAlbumId(Long tagId, Long albumId) {
        return albumTagRepository.findByIdAndAlbumId(tagId, albumId)
                .orElseThrow(TagNotFoundException::new);
    }

    /**
     * 파일 목록을 페이지 결과로 변환
     * 
     * 
     * @param items              파일 목록
     * @param pageSize           페이지 크기
     * @param mapper             파일 매퍼
     * @param createdAtExtractor 생성 시간 추출기
     * @param idExtractor        ID 추출기
     * @return 파일 페이지 결과
     */
    private <T> FilePageResult toPageResult(List<T> items,
            int pageSize,
            Function<T, FileResult> mapper,
            Function<T, LocalDateTime> createdAtExtractor,
            Function<T, Long> idExtractor) {
        boolean hasNext = items.size() > pageSize;
        if (hasNext) {
            items = items.subList(0, pageSize);
        }

        List<FileResult> results = items.stream().map(mapper).toList();
        String nextCursor = null;
        if (!items.isEmpty()) {
            T lastItem = items.get(items.size() - 1);
            nextCursor = CursorUtil.encodeCursor(createdAtExtractor.apply(lastItem), idExtractor.apply(lastItem));
        }
        return new FilePageResult(results, nextCursor, hasNext);
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
