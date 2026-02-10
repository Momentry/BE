package com.momentry.BE.domain.user.controller;

import com.momentry.BE.domain.user.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.momentry.BE.domain.user.exception.MismatchUserException;
import com.momentry.BE.domain.user.service.master.UserMasterService;
import com.momentry.BE.global.dto.ApiResponse;
import com.momentry.BE.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserMasterService userMasterService;

    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserUpdateResponse>> update(
            @PathVariable Long userId,
            @RequestBody() UpdateUserInfoRequest request) {
        validateSelf(userId);
        UserUpdateResponse userUpdateResponse = userMasterService.updateUser(userId, request);

        return ApiResponse.ofSuccess(userUpdateResponse);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> signOut(@PathVariable Long userId) {
        validateSelf(userId);
        userMasterService.signOut(userId);

        return ApiResponse.ofSuccess(HttpStatus.NO_CONTENT, null);
    }

    @PutMapping("/{userId}/alert")
    public ResponseEntity<ApiResponse<LoginResponse.AlertDto>> updateAlert(
            @PathVariable Long userId,
            @RequestBody LoginResponse.AlertDto request) {
        validateSelf(userId);
        userMasterService.updateAlertPreference(request, userId);

        return ApiResponse.ofSuccess(request);
    }

    @PostMapping("/{userId}/cloudfront-cookie/refresh")
    public ResponseEntity<ApiResponse<Void>> refreshCloudFrontCookie(@PathVariable Long userId,
            jakarta.servlet.http.HttpServletResponse response) {
        validateSelf(userId);
        userMasterService.refreshCloudFrontCookie(userId, response);

        return ApiResponse.ofSuccess();
    }


    @GetMapping("/{userId}/albums")
    public ResponseEntity<ApiResponse<GetCurrentUserAlbumListResponse>> getCurrentUserAlbumList(
            @PathVariable Long userId) {
        validateSelf(userId);
        GetCurrentUserAlbumListResponse response = userMasterService.getCurrentUserAlbums(userId);

        return ApiResponse.ofSuccess(response);
    }

    @GetMapping("/{userId}/like-files")
    public ResponseEntity<ApiResponse<GetCurrentUserLikedFileListResponse>> getCurrentUserLikedFileList(
            @PathVariable Long userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "20") int size) {
        validateSelf(userId);
        GetCurrentUserLikedFileListResponse response = userMasterService.getCurrentUserLikedFile(userId, cursor, size);

        return ApiResponse.ofSuccess(response);
    }

    @GetMapping("/{userId}/files")
    public ResponseEntity<ApiResponse<GetCurrentUserFileListResponse>> getCurrentUserFileList(@PathVariable Long userId,
            @RequestParam(required = false) String cursor) {
        validateSelf(userId);
        GetCurrentUserFileListResponse response = userMasterService.getCurrentUserFileList(userId, cursor);

        return ApiResponse.ofSuccess(response);
    }

    private void validateSelf(Long userId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!userId.equals(currentUserId)) {
            throw new MismatchUserException();
        }
    }
}
