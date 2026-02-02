package com.momentry.BE.domain.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.momentry.BE.domain.user.dto.GetCurrentUserAlbumListResponse;
import com.momentry.BE.domain.user.dto.GetCurrentUserFileListResponse;
import com.momentry.BE.domain.user.dto.GetCurrentUserLikedFileListResponse;
import com.momentry.BE.domain.user.dto.LoginResponse;
import com.momentry.BE.domain.user.dto.UserUpdateResponse;
import com.momentry.BE.domain.user.exception.MismatchUserException;
import com.momentry.BE.domain.user.service.master.UserMasterService;
import com.momentry.BE.global.dto.ApiResponse;
import com.momentry.BE.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserMasterService userMasterService;

    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserUpdateResponse>> update(
            @PathVariable Long userId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "newUsername", required = false) String newUsername) {
        validateSelf(userId);
        UserUpdateResponse userUpdateResponse = userMasterService.updateUser(userId, file, newUsername);

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

    // TODO : 이것도 페이지네이션 필요함???
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
            @RequestParam String cursor,
            @RequestParam(defaultValue = "20") int size) {
        validateSelf(userId);
        GetCurrentUserLikedFileListResponse response = userMasterService.getCurrentUserLikedFile(userId, cursor, size);

        return ApiResponse.ofSuccess(response);
    }

    @GetMapping("/{userId}/files")
    public ResponseEntity<ApiResponse<GetCurrentUserFileListResponse>> getCurrentUserFileList(@PathVariable Long userId,
            @RequestParam String cursor) {
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
