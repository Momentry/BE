package com.momentry.BE.domain.user.service.master;

import com.momentry.BE.domain.user.dto.LoginResponse;
import com.momentry.BE.domain.user.dto.UserUpdateResponse;
import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.domain.user.service.sub.AlertPreferenceService;
import com.momentry.BE.domain.user.service.sub.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserMasterService {
    private final UserService userService;
    private final AlertPreferenceService alertPreferenceService;

    @Transactional
    public UserUpdateResponse updateUser(Long userId, MultipartFile file, String newUsername){
        return userService.update(userId, file, newUsername);
    }

    @Transactional
    public void updateAlertPreference(LoginResponse.AlertDto request, Long userId){
        User user = userService.getCurrentUser(userId);
        alertPreferenceService.updateAlertPreference(request, user);
    }
}
