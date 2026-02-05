package com.momentry.BE.domain.search.dto;

import java.util.List;

import com.momentry.BE.domain.user.dto.UserSearchResult;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchUsersResponse {
    private List<UserSearchResult> users;
    private String nextCursor;
    private boolean hasNext;
}
