package com.momentry.BE.domain.search.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.momentry.BE.domain.album.dto.AlbumMemberProfileResult;
import com.momentry.BE.domain.album.dto.AlbumTagDetailResult;
import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.album.entity.AlbumMember;
import com.momentry.BE.domain.album.entity.AlbumTag;
import com.momentry.BE.domain.album.repository.AlbumMemberRepository;
import com.momentry.BE.domain.album.repository.AlbumTagRepository;
import com.momentry.BE.domain.album.repository.AlbumRepository;
import com.momentry.BE.domain.search.dto.AlbumSearchResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final AlbumTagRepository albumTagRepository;
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
     * 앨범 이름으로 앨범 검색
     * 
     * @param keyword 검색할 앨범 제목 키워드 (null이면 전체 조회)
     * @return 앨범 검색 결과 리스트
     */
    public List<AlbumSearchResponse> searchAlbums(String keyword) {
        List<Album> albums;

        if (keyword == null || keyword.isBlank()) {
            // 키워드가 없으면 전체 앨범 조회
            albums = albumRepository.findAll();
        } else {
            // 키워드로 검색
            albums = albumRepository.findByNameContainingIgnoreCase(keyword);
        }

        return albums.stream()
                .map(album -> {
                    // 멤버 조회 (username 가나다순, 최대 12명)
                    List<AlbumMember> members = albumMemberRepository.findByAlbumIdWithUser(album.getId());
                    int memberCount = members.size();

                    // 멤버 프로필 리스트 (username 가나다순, 최대 12명)
                    List<AlbumMemberProfileResult> memberProfiles = members.stream()
                            .sorted((m1, m2) -> m1.getUser().getUsername()
                                    .compareToIgnoreCase(m2.getUser().getUsername())) // username 가나다순
                            .limit(12)
                            .map(member -> new AlbumMemberProfileResult(
                                    member.getUser().getId(),
                                    member.getUser().getProfileImageUrl()))
                            .toList();

                    return new AlbumSearchResponse(
                            album.getId(),
                            album.getName(),
                            album.getCoverImageUrl(),
                            album.getCreatedAt(),
                            memberCount,
                            memberProfiles);
                })
                .toList();
    }
}
