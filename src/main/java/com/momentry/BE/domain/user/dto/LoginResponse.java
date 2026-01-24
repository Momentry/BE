package com.momentry.BE.domain.user.dto;

import com.momentry.BE.domain.user.entity.AlertPreference;
import com.momentry.BE.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {

    private Long userId;
    private String userName;
    private String profileUrl;
    private String email;
    private String provider;
    private AlertDto alert;
    private String accessToken;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AlertDto {
        private boolean albumCreated;
        private boolean invited;
        private boolean fileUploaded;

        public AlertDto(AlertPreference alertPreference){
            this.albumCreated = alertPreference.getAlbumCreated();
            this.invited = alertPreference.getInvited();
            this.fileUploaded = alertPreference.getFileUploaded();
        }
    }

    public LoginResponse(User user, AlertPreference alertPreference, String accessToken){
        this.userId = user.getId();
        this.userName = user.getUsername();
        this.profileUrl = user.getProfileImageUrl();
        this.email = user.getEmail();
        this.provider = user.getProvider();
        this.alert = new AlertDto(alertPreference);
        this.accessToken = accessToken;
    }
}
