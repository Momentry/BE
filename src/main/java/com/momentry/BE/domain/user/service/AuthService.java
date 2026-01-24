package com.momentry.BE.domain.user.service;

import com.momentry.BE.domain.user.dto.LoginRequest;
import com.momentry.BE.domain.user.dto.LoginResponse;
import com.momentry.BE.domain.user.dto.OidcClaims;
import com.momentry.BE.domain.user.entity.AlertPreference;
import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.domain.user.validator.IdTokenValidator;
import com.momentry.BE.security.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final AlertPreferenceService alertPreferenceService;
    private final IdTokenValidator idTokenValidator;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-token-expiration}")
    private int refreshTokenExpiration;

    public LoginResponse login(LoginRequest request, HttpServletResponse response){
        // 1. id_token 서명 검증 + claims 추출
        OidcClaims claims = idTokenValidator.validateToken(request.getProvider(), request.getIdToken());

        // 2. 사용자 조회 or 회원 가입
        User user = userService.findOrCreateUser(claims, request.getProvider());

        // 3. AlertPreference 조회 or 생성
        AlertPreference alertPreference = alertPreferenceService.getOrCreateAlertPreference(user);

        // 4. JWT 토큰 발급 (백엔드 JWT)
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 5. refreshToken은 쿠키에 저장하기
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false); // HTTPS 사용 시 true
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(refreshTokenExpiration);
        response.addCookie(refreshTokenCookie);

        return new LoginResponse(user, alertPreference, accessToken);
    }

    
}
