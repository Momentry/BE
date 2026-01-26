package com.momentry.BE.domain.user.dto;

import com.momentry.BE.domain.album.dto.AlbumHeaderDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetCurrentUserAlbumListResponse {
    private List<AlbumHeaderDto> albums;
}
