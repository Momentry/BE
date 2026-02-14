package com.momentry.BE.domain.search.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.momentry.BE.domain.album.dto.AlbumCountDto;
import com.momentry.BE.domain.album.dto.AlbumTagDetailResult;
import com.momentry.BE.domain.album.dto.AlbumUrlDto;
import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.album.entity.AlbumTag;
import com.momentry.BE.domain.album.repository.AlbumMemberRepository;
import com.momentry.BE.domain.album.repository.AlbumRepository;
import com.momentry.BE.domain.album.repository.AlbumTagRepository;
import com.momentry.BE.domain.album.util.CoverImageResolver;
import com.momentry.BE.domain.search.dto.AlbumSearchResponse;
import com.momentry.BE.domain.search.dto.SearchAlbumsResponse;
import com.momentry.BE.domain.search.dto.SearchTagsResponse;
import com.momentry.BE.domain.search.dto.SearchUsersResponse;
import com.momentry.BE.domain.user.dto.UserSearchResult;
import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.domain.user.repository.UserRepository;
import com.momentry.BE.global.exception.CursorDecodeFailException;
import com.momentry.BE.global.util.CursorUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int ALBUM_MEMBER_PROFILE_LIMIT = 12; // 앨범 멤버 프로필 최대 12명

    private final AlbumTagRepository albumTagRepository;
    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;
    private final AlbumMemberRepository albumMemberRepository;
    private final CoverImageResolver coverImageResolver;

    /**
     * 태그 이름으로 검색
     * 현재 사용자가 접근 가능한 앨범만 조회함.
     * 
     * @ImplNote 태그 이름으로 검색하여 태그 목록을 반환합니다.
     * 
     * @param tagName 검색할 태그 이름
     * @param cursor  커서 (마지막 태그 ID)
     * @param size    페이지 크기
     * @return 태그 목록(태그 이름, 태그에 해당하는 사진 수, 앨범 아이디, 앨범 이름)
     */
    public SearchTagsResponse searchByTagName(String tagName, Long userId, String cursor, int size) {
        int pageSize = Math.max(1, size);
        Pageable pageable = PageRequest.of(0, pageSize + 1);
        Long cursorId = parseCursor(cursor);

        List<AlbumTag> tags = (cursorId == null)
                ? albumTagRepository.findByTagNameContainingIgnoreCase(tagName, userId, pageable)
                : albumTagRepository.findByTagNameContainingIgnoreCaseWithCursor(tagName, userId, cursorId, pageable);

        boolean hasNext = tags.size() > pageSize;
        if (hasNext) {
            tags = tags.subList(0, pageSize);
        }

        List<AlbumTagDetailResult> result = new ArrayList<>();
        for (AlbumTag tag : tags) {
            result.add(new AlbumTagDetailResult(tag.getId(), tag.getTagName(), tag.getCount(), tag.getAlbum().getId(),
                    tag.getAlbum().getName()));
        }
        String nextCursor = tags.isEmpty() ? null : String.valueOf(tags.get(tags.size() - 1).getId());
        return new SearchTagsResponse(result, nextCursor, hasNext);
    }

    /**
     * 앨범에 추가할 사용자 목록을 검색하여 반환합니다.
     * 
     * @ImplNote 검색어로 username과 email을 대상으로 조회하며,
     *           username 오름차순, 동일 시 email 오름차순으로 정렬합니다.
     * 
     * @param keyword 검색어(username, email)
     * @param cursor  커서 (username, email, id)
     * @param size    페이지 크기
     * @return 검색된 사용자 목록(사용자 아이디, 이름, 이메일, 프로필 이미지 URL)
     */
    public SearchUsersResponse searchUsersByKeyword(String keyword, String cursor, int size) {
        int pageSize = Math.max(1, size);
        Pageable pageable = PageRequest.of(0, pageSize + 1);
        UserCursor decodedCursor = parseUserCursor(cursor);

        List<User> users = (decodedCursor == null)
                ? userRepository.findActiveUsersByKeyword(keyword, pageable)
                : userRepository.findActiveUsersByKeywordWithCursor(
                        keyword,
                        decodedCursor.username(),
                        decodedCursor.email(),
                        decodedCursor.id(),
                        pageable);

        boolean hasNext = users.size() > pageSize;
        if (hasNext) {
            users = users.subList(0, pageSize);
        }

        List<UserSearchResult> results = new ArrayList<>();
        for (User user : users) {
            results.add(new UserSearchResult(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getProfileImageUrl()));
        }
        String nextCursor = users.isEmpty() ? null : encodeUserCursor(users.get(users.size() - 1));
        return new SearchUsersResponse(results, nextCursor, hasNext);
    }

    /**
     * 앨범 이름으로 앨범 검색
     * 
     * 현재 사용자가 접근 가능한 앨범만 조회함.
     * 
     * @param keyword 검색할 앨범 제목 키워드 (null이면 전체 조회)
     * @param cursor  커서 (생성일시, 앨범 ID)
     * @param size    페이지 크기
     * @return 앨범 검색 결과 리스트
     */
    public SearchAlbumsResponse searchAlbums(Long userId, String keyword, String cursor, int size) {
        int pageSize = Math.max(1, size);
        Pageable pageable = PageRequest.of(0, pageSize + 1);
        AlbumCursor albumCursor = parseAlbumCursor(cursor);
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        List<Album> albums;
        if (albumCursor == null) {
            albums = hasKeyword
                    ? albumRepository.findAccessibleAlbumsByKeyword(userId, keyword, pageable)
                    : albumRepository.findAccessibleAlbums(userId, pageable);
        } else {
            albums = hasKeyword
                    ? albumRepository.findAccessibleAlbumsByKeywordWithCursor(
                            userId,
                            keyword,
                            albumCursor.createdAt(),
                            albumCursor.id(),
                            pageable)
                    : albumRepository.findAccessibleAlbumsWithCursor(
                            userId,
                            albumCursor.createdAt(),
                            albumCursor.id(),
                            pageable);
        }

        boolean hasNext = albums.size() > pageSize;
        if (hasNext) {
            albums = albums.subList(0, pageSize);
        }

        if (albums.isEmpty()) {
            return new SearchAlbumsResponse(List.of(), null, false);
        }

        List<Long> albumIds = albums.stream().map(Album::getId).toList();

        Map<Long, Integer> memberCountMap = albumMemberRepository.countMembersByAlbumIds(albumIds)
                .stream().collect(Collectors.toMap(
                        AlbumCountDto::getAlbumId,
                        AlbumCountDto::getCount));

        Map<Long, List<String>> memberProfileMap = albumMemberRepository
                .findMemberProfilesByAlbumIds(albumIds)
                .stream()
                .collect(Collectors.groupingBy(
                        AlbumUrlDto::getAlbumId,
                        Collectors.mapping(AlbumUrlDto::getUrl, Collectors.toList())));

        List<AlbumSearchResponse> results = albums.stream()
                .map(album -> toAlbumSearchResponse(
                        album,
                        memberCountMap.getOrDefault(album.getId(), 0),
                        memberProfileMap.getOrDefault(album.getId(), List.of())))
                .toList();

        String nextCursor = albums.isEmpty() ? null
                : CursorUtil.encodeCursor(
                        albums.get(albums.size() - 1).getCreatedAt(),
                        albums.get(albums.size() - 1).getId());
        return new SearchAlbumsResponse(results, nextCursor, hasNext);
    }

    private AlbumSearchResponse toAlbumSearchResponse(Album album, int memberCount, List<String> profileUrls) {
        List<String> memberProfileUrls = profileUrls.stream()
                .limit(ALBUM_MEMBER_PROFILE_LIMIT)
                .toList();
        return new AlbumSearchResponse(
                album.getId(),
                album.getName(),
                coverImageResolver.resolve(album.getCoverImageUrl()),
                album.getCreatedAt(),
                memberCount,
                memberProfileUrls);
    }

    private Long parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(cursor);
        } catch (NumberFormatException e) {
            throw new CursorDecodeFailException();
        }
    }

    private AlbumCursor parseAlbumCursor(String cursor) {
        String[] parts = CursorUtil.decodeCursorParts(cursor);
        if (parts == null) {
            return null;
        }
        try {
            LocalDateTime createdAt = LocalDateTime.parse(parts[0]);
            Long id = Long.parseLong(parts[1]);
            return new AlbumCursor(createdAt, id);
        } catch (DateTimeParseException | NumberFormatException e) {
            throw new CursorDecodeFailException();
        }
    }

    private UserCursor parseUserCursor(String cursor) {
        String[] parts = CursorUtil.decodeUserCursorParts(cursor);
        if (parts == null) {
            return null;
        }
        try {
            Long id = Long.parseLong(parts[2]);
            return new UserCursor(parts[0], parts[1], id);
        } catch (NumberFormatException e) {
            throw new CursorDecodeFailException();
        }
    }

    private String encodeUserCursor(User user) {
        return CursorUtil.encodeUserCursor(user.getUsername(), user.getEmail(), user.getId());
    }

    private record UserCursor(String username, String email, Long id) {
    }

    private record AlbumCursor(LocalDateTime createdAt, Long id) {
    }
}
