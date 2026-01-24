package com.momentry.BE.domain.search.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlbumSearchResponse {
    
    private List<AlbumSearchResult> albums;
}
