package com.momentry.BE.domain.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.momentry.BE.domain.search.dto.SearchUsersResponse;
import com.momentry.BE.domain.user.dto.UserSearchResult;
import com.momentry.BE.domain.user.entity.AccountPlan;
import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SearchService searchService;

    @Test
    @DisplayName("사용자 조회 - 키워드로 사용자를 검색하면 검색 결과를 반환한다")
    void searchUsersByKeyword_returnsSearchResults() {
        // given: 키워드로 사용자를 검색하는 상황 설정
        String keyword = "test";
        int size = 10;
        AccountPlan plan = AccountPlan.builder().plan("FREE").build();
        User user1 = User.builder()
                .email("test1@example.com")
                .username("testuser1")
                .profileImageUrl("https://example.com/profile1.jpg")
                .accountPlan(plan)
                .build();
        User user2 = User.builder()
                .email("test2@example.com")
                .username("testuser2")
                .profileImageUrl("https://example.com/profile2.jpg")
                .accountPlan(plan)
                .build();

        when(userRepository.findActiveUsersByKeyword(eq(keyword), any(Pageable.class)))
                .thenReturn(List.of(user1, user2));

        // when: 사용자 검색 서비스 메서드 호출
        SearchUsersResponse response = searchService.searchUsersByKeyword(keyword, null, size);
        List<UserSearchResult> results = response.getUsers();

        // then: 검색 결과가 올바르게 반환되었는지 확인
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getUsername()).isEqualTo("testuser1");
        assertThat(results.get(0).getEmail()).isEqualTo("test1@example.com");
        assertThat(results.get(1).getUsername()).isEqualTo("testuser2");
        assertThat(results.get(1).getEmail()).isEqualTo("test2@example.com");
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getNextCursor()).isNotNull();
        verify(userRepository).findActiveUsersByKeyword(eq(keyword), any(Pageable.class));
    }

    @Test
    @DisplayName("사용자 조회 - 검색 결과가 없으면 빈 리스트를 반환한다")
    void searchUsersByKeyword_noResults_returnsEmptyList() {
        // given: 검색 결과가 없는 키워드로 검색하는 상황 설정
        String keyword = "nonexistent";
        int size = 10;
        when(userRepository.findActiveUsersByKeyword(eq(keyword), any(Pageable.class)))
                .thenReturn(List.of());

        // when: 사용자 검색 서비스 메서드 호출
        SearchUsersResponse response = searchService.searchUsersByKeyword(keyword, null, size);
        List<UserSearchResult> results = response.getUsers();

        // then: 빈 리스트가 반환되었는지 확인
        assertThat(results).isEmpty();
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getNextCursor()).isNull();
    }

    @Test
    @DisplayName("사용자 조회 - 키워드가 null이면 빈 리스트를 반환한다")
    void searchUsersByKeyword_nullKeyword_returnsEmptyList() {
        // given
        int size = 10;
        when(userRepository.findActiveUsersByKeyword(eq(null), any(Pageable.class)))
                .thenReturn(List.of());

        // when: null 키워드로 사용자 검색 서비스 메서드 호출
        SearchUsersResponse response = searchService.searchUsersByKeyword(null, null, size);
        List<UserSearchResult> results = response.getUsers();

        // then: 빈 리스트가 반환되었는지 확인
        assertThat(results).isEmpty();
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getNextCursor()).isNull();
    }

    @Test
    @DisplayName("사용자 조회 - 키워드가 빈 문자열이면 빈 리스트를 반환한다")
    void searchUsersByKeyword_emptyKeyword_returnsEmptyList() {
        // given
        int size = 10;
        String keyword = "   ";
        when(userRepository.findActiveUsersByKeyword(eq(keyword), any(Pageable.class)))
                .thenReturn(List.of());

        // when: 빈 문자열(공백만 포함) 키워드로 사용자 검색 서비스 메서드 호출
        SearchUsersResponse response = searchService.searchUsersByKeyword(keyword, null, size);
        List<UserSearchResult> results = response.getUsers();

        // then: 빈 리스트가 반환되었는지 확인
        assertThat(results).isEmpty();
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getNextCursor()).isNull();
    }
}

