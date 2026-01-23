package com.momentry.BE.domain.album.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.momentry.BE.domain.album.dto.AlbumMemberInviteResult;
import com.momentry.BE.domain.album.dto.InvitedMemberResult;
import com.momentry.BE.domain.album.dto.AlbumTagResult;
import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.album.entity.AlbumMember;
import com.momentry.BE.domain.album.entity.AlbumTag;
import com.momentry.BE.domain.album.entity.MemberAlbumPermission;
import com.momentry.BE.domain.album.exception.CannotKickManagerException;
import com.momentry.BE.domain.album.exception.DuplicateTagException;
import com.momentry.BE.domain.album.exception.AlbumNotFoundException;
import com.momentry.BE.domain.album.exception.InvalidAlbumInviteRequestException;
import com.momentry.BE.domain.album.exception.NoAlbumEditPermissionException;
import com.momentry.BE.domain.album.exception.NoAlbumMemberEditPermissionException;
import com.momentry.BE.domain.album.exception.NoAlbumPermissionException;
import com.momentry.BE.domain.album.exception.TagNotFoundException;
import com.momentry.BE.domain.album.exception.AlbumMemberNotFoundException;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlbumService {
    
    private final AlbumTagRepository albumTagRepository;

    private final AlbumMemberRepository albumMemberRepository;
    private final AlbumRepository albumRepository;
    private final FileTagInfoRepository fileTagInfoRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    /**
     * 앨범 태그 생성
     * 
     * @param albumId 앨범 ID
     * @param tagName 태그 이름
     * @param userId 사용자 ID
     */
    @Transactional
    public void createTag(Long albumId, String tagName, Long userId) {
        AlbumMember albumMember = getAlbumPermissionWithAlbum(albumId, userId);

        requireEditPermission(albumMember.getPermission());

        AlbumTag tag = AlbumTag.builder()
                .album(albumMember.getAlbum())
                .tagName(tagName)
                .build();

        try {
            albumTagRepository.saveAndFlush(tag);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateTagException();
        }
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
                invitee.getProfileImageUrl()
            ));
        }

        return new AlbumMemberInviteResult(album.getId(), invitedResults);
    }

    /**
     * 앨범 권한만 가져오기
     * @param albumId 앨범 ID
     * @param userId 사용자 ID
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
     * @param requester 요청자(강퇴를 시도하는 멤버)
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
     * @param albumId 앨범 ID
     * @param userId 사용자 ID
     * @return AlbumMember
     */
    private AlbumMember getAlbumPermissionWithAlbum(Long albumId, Long userId) {
        return albumMemberRepository.findByAlbumIdAndUserIdWithAlbum(albumId, userId)
                .orElseThrow(NoAlbumPermissionException::new);
    }

    /**
     * 앨범 멤버 권한 변경
     *
     * @param albumId   앨범 ID
     * @param memberId  권한을 변경할 멤버(사용자) ID
     * @param permission 부여할 권한 종류 (manager, editor, viewer)
     * @param userId    요청자(현재 사용자) ID
     */
    @Transactional
    public void updateMemberPermission(Long albumId, Long memberId, String permission, Long userId) {
        AlbumMember requester = getAlbumPermission(albumId, userId);
        requireMemberEditPermission(requester.getPermission());

        AlbumMember targetMember = getAlbumMember(albumId, memberId);
        MemberAlbumPermission targetPermission = MemberAlbumPermission.valueOf(permission.toUpperCase());

        targetMember.changePermission(targetPermission);
    }

    /**
     * 앨범의 특정 멤버 조회
     *
     * @implNote 멤버를 찾을 수 없는 경우 AlbumMemberNotFoundException 예외를 발생시킴
     *
     * @param albumId 앨범 ID
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
     * @param tagId 태그 ID
     * @param userId 사용자 ID
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
     * @param tagId 태그 ID
     * @param tagName 태그 이름
     * @param userId 사용자 ID
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
     * 앨범의 태그 목록을 조회
     * 
     * @param albumId 앨범 ID
     * @param userId 사용자 ID
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
     * @param tagId 태그 ID
     * @param cursor 커서
     * @param size 페이지 크기
     * @param userId 사용자 ID
     * @return 파일 페이지 결과
     */
    public FilePageResult getFiles(Long albumId, Long tagId, String cursor, int size, Long userId) {
        AlbumMember albumMember = getAlbumPermissionWithAlbum(albumId, userId);

        int pageSize = Math.max(size, 1);
        PageRequest pageable = PageRequest.of(0, pageSize + 1);
        FileCursor decodedCursor = decodeCursor(cursor);

        if (tagId != null) {
            getTagByIdAndAlbumId(tagId, albumId);
            List<FileTagInfo> fileTagInfos = fetchTagInfosByTag(tagId, decodedCursor, pageable);
            return toPageResult(fileTagInfos, pageSize, info -> FileResult.of(info.getFile()), info -> info.getFile().getCreatedAt(), info -> info.getFile().getId());
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
     * @param album 앨범
     * @param cursor 커서
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
     * @param items 파일 목록
     * @param pageSize 페이지 크기
     * @param mapper 파일 매퍼
     * @param createdAtExtractor 생성 시간 추출기
     * @param idExtractor ID 추출기
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
            nextCursor = encodeCursor(createdAtExtractor.apply(lastItem), idExtractor.apply(lastItem));
        }
        return new FilePageResult(results, nextCursor, hasNext);
    }

    /**
     * cursor를 디코딩하여 Cursor 객체로 반환
     * 
     * cursor 문자열 형식: createdAt|id
     * base64 인코딩된 문자열이며 디코딩 후 createdAt|id 형식의 문자열로 변환되어야 함
     * 
     * @implNote cursor 문자열이 올바르지 않으면 CursorDecodeFailException 예외를 발생시킴
     * 
     * @param cursor cursor 문자열
     * @return Cursor
     */
    private FileCursor decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cursor);
            String payload = new String(decoded, StandardCharsets.UTF_8);
            String[] parts = payload.split("\\|", 2);
            if (parts.length != 2) {
                throw new CursorDecodeFailException();
            }
            LocalDateTime createdAt = LocalDateTime.parse(parts[0]);
            Long id = Long.parseLong(parts[1]);
            return new FileCursor(createdAt, id);
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new CursorDecodeFailException();
        }
    }

    /**
     * Cursor 객체를 base64 인코딩하여 문자열로 반환
     * 
     * @param createdAt 생성 시간
     * @param id ID
     * @return 인코딩된 문자열
     */
    private String encodeCursor(LocalDateTime createdAt, Long id) {
        String payload = createdAt + "|" + id;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }
}
