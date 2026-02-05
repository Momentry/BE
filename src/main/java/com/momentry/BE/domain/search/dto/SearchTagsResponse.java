package com.momentry.BE.domain.search.dto;

import java.util.List;

import com.momentry.BE.domain.album.dto.AlbumTagDetailResult;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchTagsResponse {
    private List<AlbumTagDetailResult> tags;
    private String nextCursor;
    private boolean hasNext;
}
