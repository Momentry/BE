package com.momentry.BE.global.event.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AlbumCreateEvent {
    private Long userId;
    private Long albumId;
    private String albumName;
}
