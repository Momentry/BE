package com.momentry.BE.security.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {
    @Value("${cookie.name.refresh}")
    public String REFRESH_TOKEN_NAME;

    // maxAge = ms 단위
    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);  // HTTPS 배포 시 true로 변경
        cookie.setPath("/");
        cookie.setMaxAge(maxAge/1000);
        return cookie;
    }

    public void saveRefreshTokenCookie(HttpServletResponse response,
                                      String refreshToken,
                                      int refreshTokenExpiration) {
        Cookie refreshTokenCookie = createCookie(REFRESH_TOKEN_NAME, refreshToken, refreshTokenExpiration);
        response.addCookie(refreshTokenCookie);
    }

    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie refreshTokenCookie = createCookie(REFRESH_TOKEN_NAME, null, 0);
        response.addCookie(refreshTokenCookie);
    }
}
