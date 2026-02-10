package com.momentry.BE.domain.album.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlbumMemberInviteRequest {

    @NotEmpty(message = "초대할 사용자 ID 목록은 비어 있을 수 없습니다.")
    private List<Long> userIds;
}
