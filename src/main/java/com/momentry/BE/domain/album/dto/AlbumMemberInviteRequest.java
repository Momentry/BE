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
    @NotEmpty
    private List<Long> userIds;
}
