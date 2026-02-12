package com.momentry.BE.global.event.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadEvent {
    private Long userId;
    private Long albumId;
    private Boolean success;
}
