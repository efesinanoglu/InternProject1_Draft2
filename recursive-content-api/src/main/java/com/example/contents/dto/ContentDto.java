package com.example.contents.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentDto {
    @Schema(example = "1")
    private Long id;

    @Schema(example = "https://example.com/img.jpg")
    private String image;

    @Schema(example = "Some text block")
    private String textBlock;

    @Schema(example = "null", description = "Optional parent id for tree structure")
    private Long parentId;
}