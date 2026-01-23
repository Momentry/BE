package com.momentry.BE.domain.search.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.momentry.BE.domain.album.dto.AlbumTagDetailResult;
import com.momentry.BE.domain.album.entity.AlbumTag;
import com.momentry.BE.domain.album.repository.AlbumTagRepository;
import com.momentry.BE.domain.user.dto.UserSearchResult;
import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchService {
    
    private final AlbumTagRepository albumTagRepository;
    private final UserRepository userRepository;

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
            result.add(new AlbumTagDetailResult(tag.getId(), tag.getTagName(), tag.getCount(), tag.getAlbum().getId(), tag.getAlbum().getName()));
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
                user.getProfileImageUrl()
            ));
        }
        return results;
    }
}
