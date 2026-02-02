package com.momentry.BE.domain.file.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FileTagRequestDto {
    private List<Long> fileIds; // 태그를 추가할 파일 아이디 배열
    private List<Long> tagIds;  // 추가할 태그 배열
}
