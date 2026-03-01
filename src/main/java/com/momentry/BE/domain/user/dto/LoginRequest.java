package com.momentry.BE.domain.user.dto;

import com.momentry.BE.domain.user.enums.Provider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    private String idToken;
    private String fcmToken;
    private Provider provider;
}
