package com.momentry.BE.domain.user.dto;

import java.util.Map;

import com.momentry.BE.domain.album.entity.MemberAlbumPermission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetAlbumPermissionsResponse {
    private Map<Long, MemberAlbumPermission> albumPermissions;
}
