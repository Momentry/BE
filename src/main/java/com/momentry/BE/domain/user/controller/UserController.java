package com.momentry.BE.domain.user.controller;

import com.momentry.BE.domain.user.dto.LoginResponse;
import com.momentry.BE.domain.user.dto.UserUpdateResponse;
import com.momentry.BE.domain.user.service.master.UserMasterService;
import com.momentry.BE.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserMasterService userMasterService;

    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserUpdateResponse>> update(@PathVariable Long userId, @RequestParam(value = "file", required = false) MultipartFile file,
                                                                  @RequestParam(value = "newUsername", required = false) String newUsername){
        UserUpdateResponse userUpdateResponse = userMasterService.updateUser(userId, file, newUsername);

        return ApiResponse.ofSuccess(userUpdateResponse);
    }

    @PatchMapping("/{userId}/alert")
    public ResponseEntity<ApiResponse<LoginResponse.AlertDto>> updateAlert(@PathVariable Long userId, @RequestBody LoginResponse.AlertDto request){
        userMasterService.updateAlertPreference(request, userId);

        return ApiResponse.ofSuccess(request);
    }
}
