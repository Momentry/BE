package com.momentry.BE.domain.file.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagThumbnailRowDto {
    private Long tagId;
    private String thumbUrl;
}
