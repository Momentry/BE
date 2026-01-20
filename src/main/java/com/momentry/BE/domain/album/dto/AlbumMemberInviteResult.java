package com.momentry.BE.domain.album.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlbumMemberInviteResult {

    private Long albumId;
    private List<InvitedMemberResult> invited;
}
