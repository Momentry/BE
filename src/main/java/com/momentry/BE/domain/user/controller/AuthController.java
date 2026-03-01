package com.momentry.BE.domain.user.controller;

import com.momentry.BE.domain.user.dto.LoginRequest;
import com.momentry.BE.domain.user.dto.LoginResponse;
import com.momentry.BE.domain.user.dto.RefreshResponse;
import com.momentry.BE.domain.user.service.master.AuthMasterService;
import com.momentry.BE.global.dto.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthMasterService authMasterService;

    @PostMapping("/social")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request, HttpServletResponse response){
        LoginResponse responseData = authMasterService.login(request, response);
        
        return ApiResponse.ofSuccess(responseData);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response){
        authMasterService.logout(response);

        return ApiResponse.ofSuccess();
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponse>> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response){
        RefreshResponse refreshResponse = authMasterService.refresh(refreshToken, response);

        return ApiResponse.ofSuccess(refreshResponse);
    }

    // 심사용 테스트 로그인 API
    @PostMapping("/test-login")
    public ResponseEntity<ApiResponse<LoginResponse>> testLogin(@RequestBody LoginRequest request, HttpServletResponse response){
        LoginResponse responseData = authMasterService.testLogin(request, response);

        return ApiResponse.ofSuccess(responseData);
    }
}
