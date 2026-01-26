package com.momentry.BE.domain.user.service.master;

import com.momentry.BE.domain.user.dto.LoginRequest;
import com.momentry.BE.domain.user.dto.LoginResponse;
import com.momentry.BE.domain.user.dto.OidcClaims;
import com.momentry.BE.domain.user.dto.RefreshResponse;
import com.momentry.BE.domain.user.entity.AlertPreference;
import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.domain.user.exception.InvalidTokenException;
import com.momentry.BE.domain.user.exception.TokenNotFoundException;
import com.momentry.BE.domain.user.service.sub.AlertPreferenceService;
import com.momentry.BE.domain.user.service.sub.UserService;
import com.momentry.BE.domain.user.validator.IdTokenValidator;
import com.momentry.BE.security.util.CookieUtil;
import com.momentry.BE.security.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthMasterService {
    private final UserService userService;
    private final AlertPreferenceService alertPreferenceService;
    private final IdTokenValidator idTokenValidator;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    @Value("${jwt.refresh-token-expiration}")
    private int refreshTokenExpiration;

    @Transactional
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
        cookieUtil.saveRefreshTokenCookie(response, refreshToken, refreshTokenExpiration);

        return new LoginResponse(user, alertPreference, accessToken);
    }

    public void logout(HttpServletResponse response){
        cookieUtil.deleteRefreshTokenCookie(response);
    }

    public RefreshResponse refresh(String refreshToken, HttpServletResponse response){
        if (refreshToken == null) {
            throw new TokenNotFoundException();
        }

        if (jwtUtil.isTokenInvalid(refreshToken)) {
            throw new InvalidTokenException();
        }

        Long userId = jwtUtil.extractUserId(refreshToken);
        User user = userService.getUser(userId);

        String newRefreshToken = jwtUtil.generateRefreshToken(userId);
        cookieUtil.saveRefreshTokenCookie(response, newRefreshToken, refreshTokenExpiration);

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());

        return new RefreshResponse(newAccessToken);
    }
}
