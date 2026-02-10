package com.momentry.BE.global.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FileCursor {
    private LocalDateTime createdAt;
    private Long id;
}
