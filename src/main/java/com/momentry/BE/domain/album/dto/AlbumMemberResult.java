package com.momentry.BE.domain.album.dto;

import com.momentry.BE.domain.album.entity.AlbumMember;
import com.momentry.BE.domain.album.entity.MemberAlbumPermission;
import com.momentry.BE.domain.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlbumMemberResult {

    private String email;
    private Long userId;
    private String username;
    private String profileUrl;
    private MemberAlbumPermission permission;

    public static AlbumMemberResult of(AlbumMember member) {
        User user = member.getUser();
        return new AlbumMemberResult(
                user.getEmail(),
                user.getId(),
                user.getUsername(),
                user.getProfileImageUrl(),
                member.getPermission());
    }
}
