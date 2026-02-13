package com.momentry.BE.global.event.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AlbumInviteEvent {
    private List<Long> invitedUserIds;
    private Long albumId;
    private String albumName;
}
