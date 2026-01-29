package com.momentry.BE.domain.album.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumHeaderDto {
    private Long albumId;
    private String thumbnailUrl;
    private String albumName;
    private Integer memberCount;
    private Integer fileCount;
    @JsonFormat(pattern = "yyyy.MM.dd")
    private LocalDate createdAt;

    @Builder.Default
    private List<String> memberProfiles = List.of();  // null 허용, 최대 4명
}
