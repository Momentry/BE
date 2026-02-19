package com.momentry.BE.security.filter;

import com.momentry.BE.security.dto.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Profile("local")   // local 프로파일일때만 빈 생성
@Slf4j
public class TestAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String testUserId = request.getHeader("Test-User-Id");

        // 헤더에 Test-User-Id가 있으면 강제 로그인 처리
        if (testUserId != null && !testUserId.isEmpty()) {
            try {
                Long userId = Long.parseLong(testUserId);
                CustomUserDetails userDetails = new CustomUserDetails(userId);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info("테스트 모드 인증 성공 - userId: {}", userId);

                // 인증에 성공했으므로 다음 필터(JwtAuthenticationFilter)는 통과함
                // JwtAuthenticationFilter 내부의 SecurityContextHolder.getContext().getAuthentication() == null 조건 때문
            } catch (NumberFormatException e) {
                log.error("잘못된 테스트 유저 ID 형식입니다: {}", testUserId);
            }
        }

        filterChain.doFilter(request, response);
    }
}
