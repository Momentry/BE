package com.momentry.BE.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSearchResult {
    private Long userId;
    private String username;
    private String email;
    private String profileUrl;
}
