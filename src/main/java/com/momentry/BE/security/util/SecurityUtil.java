package com.momentry.BE.security.util;

import com.momentry.BE.security.dto.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
    public static CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증된 사용자가 아닙니다.");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails)) {
            throw new IllegalStateException("인증 정보가 올바르지 않습니다.");
        }

        return (CustomUserDetails) principal;
    }

    public static Long getCurrentUserId() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        return userDetails.getUserId();
    }
}
