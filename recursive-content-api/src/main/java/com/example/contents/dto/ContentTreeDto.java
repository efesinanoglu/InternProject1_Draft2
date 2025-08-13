package com.example.contents.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentTreeDto {
    @Schema(example = "1")
    private Long id;

    @Schema(example = "https://example.com/img.jpg")
    private String image;

    @Schema(example = "Some text block")
    private String textBlock;

    private List<ContentTreeDto> children;
}