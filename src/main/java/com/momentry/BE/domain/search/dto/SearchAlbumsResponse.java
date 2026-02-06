package com.momentry.BE.domain.search.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchAlbumsResponse {
    private List<AlbumSearchResponse> albums;
    private String nextCursor;
    private boolean hasNext;
}
