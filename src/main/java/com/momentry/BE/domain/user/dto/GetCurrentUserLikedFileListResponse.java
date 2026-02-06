package com.momentry.BE.domain.user.dto;


import com.momentry.BE.domain.file.dto.LikedFileDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetCurrentUserLikedFileListResponse {
    private List<LikedFileDto> likedFiles;
    private String nextCursor;
}
