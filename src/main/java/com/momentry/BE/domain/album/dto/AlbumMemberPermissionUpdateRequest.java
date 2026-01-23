package com.momentry.BE.domain.album.dto;

import com.momentry.BE.domain.album.entity.MemberAlbumPermission;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;

@Getter
public class AlbumMemberPermissionUpdateRequest {

    @NotNull(message = "권한은 필수 값입니다.")
    private MemberAlbumPermission permission;
}
