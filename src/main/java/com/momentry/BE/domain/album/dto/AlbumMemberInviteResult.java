package com.momentry.BE.domain.album.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlbumMemberInviteResult {

    @NotNull(message = "앨범 ID는 null일 수 없습니다.")
    private Long albumId;

    @NotNull(message = "초대된 멤버 목록은 null일 수 없습니다.")
    @Size(min = 1, message = "초대된 멤버는 최소 1명 이상이어야 합니다.")
    private List<InvitedMemberResult> invited;
}
