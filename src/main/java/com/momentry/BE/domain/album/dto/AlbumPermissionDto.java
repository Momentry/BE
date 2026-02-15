package com.momentry.BE.domain.album.dto;

import com.momentry.BE.domain.album.entity.MemberAlbumPermission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlbumPermissionDto {
    private Long albumId;
    private MemberAlbumPermission permission;

}
