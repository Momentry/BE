package com.momentry.BE.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserUpdateResponse {
    private String userName;
    private String profileUrl;
}
