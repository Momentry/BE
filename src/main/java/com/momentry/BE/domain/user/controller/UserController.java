package com.momentry.BE.domain.user.controller;

import com.momentry.BE.domain.user.dto.LoginRequest;
import com.momentry.BE.domain.user.dto.LoginResponse;
import com.momentry.BE.domain.user.dto.UserUpdateResponse;
import com.momentry.BE.domain.user.service.UserService;
import com.momentry.BE.global.dto.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserUpdateResponse>> update(@PathVariable Long userId, @RequestParam(value = "file", required = false) MultipartFile file,
                                                                  @RequestParam(value = "newUsername", required = false) String newUsername, HttpServletResponse response){
        UserUpdateResponse userUpdateResponse = userService.update(userId, file, newUsername);

        return ApiResponse.ofSuccess(userUpdateResponse);
    }
}
