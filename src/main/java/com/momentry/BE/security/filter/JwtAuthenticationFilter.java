package com.momentry.BE.security.filter;

import com.momentry.BE.security.dto.CustomUserDetails;
import com.momentry.BE.security.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // 1. Authorization 헤더에서 JWT 추출
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("JWT 토큰 없음 - URI: {}, Method: {}, Authorization Header: {}",
                    requestURI, method, authHeader != null ? "present" : "null");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (jwtUtil.isTokenInvalid(token)) {
                log.warn("유효하지 않거나 만료된 JWT 토큰 - URI: {}, Method: {}, RemoteAddr: {}",
                        requestURI, method, request.getRemoteAddr());
                filterChain.doFilter(request, response);
                return;
            }

            Claims claims = jwtUtil.extractAllClaims(token);
            Long userId = claims.get("userId", Long.class);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                CustomUserDetails userDetails = new CustomUserDetails(userId);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info("사용자 인증 성공 - userId: {}, URI: {}, Method: {}",
                        userId, requestURI, method);
            } else {
                log.debug("이미 인증된 사용자 - URI: {}, Method: {}", requestURI, method);
            }
        } catch (Exception e) {
            log.error("JWT 인증 처리 중 오류 발생 - URI: {}, Method: {}, error: {}",
                    requestURI, method, e.getMessage(), e);
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}