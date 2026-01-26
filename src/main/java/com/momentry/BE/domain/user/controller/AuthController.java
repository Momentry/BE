package com.momentry.BE.domain.user.controller;

import com.momentry.BE.domain.user.dto.LoginRequest;
import com.momentry.BE.domain.user.dto.LoginResponse;
import com.momentry.BE.domain.user.dto.RefreshResponse;
import com.momentry.BE.domain.user.service.AuthService;
import com.momentry.BE.global.dto.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/social")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request, HttpServletResponse response){
        LoginResponse responseData = authService.login(request, response);

        return ApiResponse.ofSuccess(responseData);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response){
        authService.logout(response);

        return ApiResponse.ofSuccess();
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponse>> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response){
        RefreshResponse refreshResponse = authService.refresh(refreshToken, response);

        return ApiResponse.ofSuccess(refreshResponse);
    }
}
