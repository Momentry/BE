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

import com.momentry.BE.domain.album.dto.AlbumCreationResponse;
import com.momentry.BE.domain.album.dto.AlbumDetailResponse;
import com.momentry.BE.domain.album.dto.AlbumMemberResult;
import com.momentry.BE.domain.album.dto.AlbumTagResult;
import com.momentry.BE.domain.album.dto.AlbumTagSimpleResult;
import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.album.entity.AlbumMember;
import com.momentry.BE.domain.album.entity.AlbumPermission;
import com.momentry.BE.domain.album.entity.AlbumTag;
import com.momentry.BE.domain.album.exception.AlbumNotFoundException;
import com.momentry.BE.domain.album.exception.DuplicateAlbumNameException;
import com.momentry.BE.domain.album.exception.DuplicateTagException;
import com.momentry.BE.domain.album.exception.NoAlbumEditPermissionException;
import com.momentry.BE.domain.album.exception.NoAlbumPermissionException;
import com.momentry.BE.domain.album.exception.TagNotFoundException;
import com.momentry.BE.domain.album.repository.AlbumMemberRepository;
import com.momentry.BE.domain.album.repository.AlbumPermissionRepository;
import com.momentry.BE.domain.album.repository.AlbumRepository;
import com.momentry.BE.domain.album.repository.AlbumTagRepository;
import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.domain.user.repository.UserRepository;
import com.momentry.BE.domain.file.dto.FilePageResult;
import com.momentry.BE.domain.file.dto.FileResult;
import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.file.entity.FileTagInfo;
import com.momentry.BE.domain.file.repository.FileRepository;
import com.momentry.BE.domain.file.repository.FileTagInfoRepository;
import com.momentry.BE.global.dto.FileCursor;
import com.momentry.BE.global.exception.CursorDecodeFailException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final AlbumTagRepository albumTagRepository;
    private final AlbumMemberRepository albumMemberRepository;
    private final AlbumPermissionRepository albumPermissionRepository;
    private final FileTagInfoRepository fileTagInfoRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    // 나중에 s3에서 가져오도록 변경 필요
    private static final String DEFAULT_COVER_IMAGE_URL = "https://images.unsplash.com/photo-1511497584788-876760111969?w=800";

    /**
     * 앨범 생성
     * 
     * @param albumName     앨범 이름
     * @param coverImageUrl 커버 이미지 URL (null이면 default 이미지 사용)
     * @param userId        사용자 ID (앨범 생성자)
     * @return 앨범 생성 응답
     */
    @Transactional
    public AlbumCreationResponse createAlbum(String albumName, String coverImageUrl, Long userId) {
        // 앨범 이름 중복 체크
        if (albumRepository.findByName(albumName).isPresent()) {
            throw new DuplicateAlbumNameException();
        }

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 커버 이미지 URL 설정 (없으면 default 이미지 사용)
        String finalCoverImageUrl = (coverImageUrl != null && !coverImageUrl.isBlank())
                ? coverImageUrl
                : DEFAULT_COVER_IMAGE_URL;

        // 앨범 생성
        Album album = Album.builder()
                .name(albumName)
                .coverImageUrl(finalCoverImageUrl)
                .build();

        Album savedAlbum = albumRepository.save(album);

        // 추후 MemberAlbumPermission enum 적용 예정
        // MANAGER 권한 조회 (ID 1 = MANAGER)
        AlbumPermission managerPermission = albumPermissionRepository.getReferenceById(1L);

        // 앨범 멤버 생성 (생성자는 MANAGER 권한)
        AlbumMember albumMember = AlbumMember.builder()
                .user(user)
                .album(savedAlbum)
                .permission(managerPermission)
                .build();

        albumMemberRepository.save(albumMember);

        return new AlbumCreationResponse(savedAlbum.getId(), savedAlbum.getName());
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

        requireEditPermission(albumMember.getPermission().getPermission());

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
     * 앨범의 태그를 삭제
     * 
     * @param albumId 앨범 ID
     * @param tagId   태그 ID
     * @param userId  사용자 ID
     */
    @Transactional
    public void deleteTag(Long albumId, Long tagId, Long userId) {

        AlbumMember albumMember = getAlbumPermission(albumId, userId);

        requireEditPermission(albumMember.getPermission().getPermission());

        AlbumTag tag = getTagByIdAndAlbumId(tagId, albumId);

        fileTagInfoRepository.deleteByTag(tag);
        albumTagRepository.deleteById(tagId);
    }

    /**
     * 앨범 편집 권한 확인
     * Viewer 이상의 권한을 가져야 함
     * 
     * @implNote 권한이 VIEWER인 경우 NoAlbumEditPermissionException 예외를 발생시킴
     * @param permission 권한
     */
    private void requireEditPermission(String permission) {
        if (permission.equals("VIEWER")) {
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

        requireEditPermission(albumMember.getPermission().getPermission());

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

        // 멤버 조회
        List<AlbumMember> members = albumMemberRepository.findByAlbumIdWithUser(albumId);
        List<AlbumMemberResult> memberResults = members.stream()
                .map(member -> new AlbumMemberResult(
                        member.getUser().getId(),
                        member.getUser().getUsername(),
                        member.getUser().getEmail(),
                        member.getUser().getProfileImageUrl()))
                .toList();

        // 태그 조회
        List<AlbumTag> tags = albumTagRepository.findByAlbum(album);
        List<AlbumTagSimpleResult> tagResults = tags.stream()
                .map(tag -> new AlbumTagSimpleResult(tag.getId(), tag.getTagName()))
                .toList();

        // 파일 개수 조회
        long fileCount = fileRepository.countByAlbum(album);

        return new AlbumDetailResponse(
                album.getName(),
                album.getCoverImageUrl(),
                fileCount,
                memberResults,
                tagResults);
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
        FileCursor decodedCursor = decodeCursor(cursor);

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
     * @param id        ID
     * @return 인코딩된 문자열
     */
    private String encodeCursor(LocalDateTime createdAt, Long id) {
        String payload = createdAt + "|" + id;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }
}
