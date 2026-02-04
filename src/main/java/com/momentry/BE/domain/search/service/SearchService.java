package com.momentry.BE.domain.search.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.momentry.BE.domain.album.dto.AlbumCountDto;
import com.momentry.BE.domain.album.dto.AlbumUrlDto;
import com.momentry.BE.domain.album.dto.AlbumTagDetailResult;
import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.album.entity.AlbumTag;
import com.momentry.BE.domain.album.repository.AlbumMemberRepository;
import com.momentry.BE.domain.album.repository.AlbumTagRepository;
import com.momentry.BE.domain.user.dto.UserSearchResult;
import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.domain.user.repository.UserRepository;
import com.momentry.BE.domain.album.repository.AlbumRepository;
import com.momentry.BE.domain.search.dto.AlbumSearchResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int ALBUM_MEMBER_PROFILE_LIMIT = 12; // 앨범 멤버 프로필 최대 12명

    private final AlbumTagRepository albumTagRepository;
    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;
    private final AlbumMemberRepository albumMemberRepository;

    /**
     * 태그 이름으로 검색
     * 현재 사용자가 접근 가능한 앨범인지 검사하지 않음.
     * 추가로 페이지네이션 적용 여부 검토 필요.
     * 
     * @ImplNote 태그 이름으로 검색하여 태그 목록을 반환합니다.
     * 
     * @param tagName 검색할 태그 이름
     * @return 태그 목록(태그 이름, 태그에 해당하는 사진 수, 앨범 아이디, 앨범 이름)
     */
    public List<AlbumTagDetailResult> searchByTagName(String tagName) {
        List<AlbumTag> tags = albumTagRepository.findByTagNameContainingIgnoreCase(tagName);

        List<AlbumTagDetailResult> result = new ArrayList<>();
        for (AlbumTag tag : tags) {
            result.add(new AlbumTagDetailResult(tag.getId(), tag.getTagName(), tag.getCount(), tag.getAlbum().getId(),
                    tag.getAlbum().getName()));
        }
        return result;
    }

    /**
     * 앨범에 추가할 사용자 목록을 검색하여 반환합니다.
     * 추가로 검색결과 개수 제한 검토 필요
     * 
     * @ImplNote 검색어로 username과 email을 대상으로 조회하며,
     *           username 오름차순, 동일 시 email 오름차순으로 정렬합니다.
     * 
     * @param keyword 검색어(username, email)
     * @return 검색된 사용자 목록(사용자 아이디, 이름, 이메일, 프로필 이미지 URL)
     */
    public List<UserSearchResult> searchUsersByKeyword(String keyword) {
        List<User> users = userRepository.findActiveUsersByKeyword(keyword);
        List<UserSearchResult> results = new ArrayList<>();
        for (User user : users) {
            results.add(new UserSearchResult(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getProfileImageUrl()));
        }
        return results;
    }

    /**
     * 앨범 이름으로 앨범 검색
     * 
     * 현재 사용자가 접근 가능한 앨범인지 검사하지 않음.
     * 추가로 페이지네이션 적용 여부 검토 필요.
     * 
     * @param keyword 검색할 앨범 제목 키워드 (null이면 전체 조회)
     * @return 앨범 검색 결과 리스트
     */
    public List<AlbumSearchResponse> searchAlbums(String keyword) {
        List<Album> albums;

        if (keyword == null || keyword.isBlank()) {
            albums = albumRepository.findAll();
        } else {
            albums = albumRepository.findByNameContainingIgnoreCase(keyword);
        }

        if (albums.isEmpty()) {
            return List.of();
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

        return albums.stream()
                .map(album -> toAlbumSearchResponse(
                        album,
                        memberCountMap.getOrDefault(album.getId(), 0),
                        memberProfileMap.getOrDefault(album.getId(), List.of())))
                .toList();
    }

    private AlbumSearchResponse toAlbumSearchResponse(Album album, int memberCount, List<String> profileUrls) {
        List<String> memberProfileUrls = profileUrls.stream()
                .limit(ALBUM_MEMBER_PROFILE_LIMIT)
                .toList();
        return new AlbumSearchResponse(
                album.getId(),
                album.getName(),
                album.getCoverImageUrl(),
                album.getCreatedAt(),
                memberCount,
                memberProfileUrls);
    }
}
