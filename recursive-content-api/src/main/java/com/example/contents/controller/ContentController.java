package com.example.contents.controller;

import com.example.contents.dto.ContentDto;
import com.example.contents.dto.ContentTreeDto;
import com.example.contents.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
@Validated
@Tag(name = "Contents")
public class ContentController {

    private final ContentService service;

    @Operation(summary = "Get a single content by id")
    @GetMapping("/{id}")
    public ResponseEntity<ContentDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Get a content node recursively with all children")
    @GetMapping("/{id}/recursive")
    public ResponseEntity<ContentTreeDto> getRecursive(@PathVariable Long id) {
        return ResponseEntity.ok(service.getTree(id));
    }

    @Operation(summary = "Create or update a content node")
    @PostMapping
    public ResponseEntity<ContentDto> createOrUpdate(@RequestBody @Valid UpsertRequest req) {
        ContentDto dto = ContentDto.builder()
                .id(req.id)
                .image(req.image)
                .textBlock(req.textBlock)
                .parentId(req.parentId)
                .build();
        return ResponseEntity.ok(service.createOrUpdate(dto));
    }

    @Data
    public static class UpsertRequest {
        public Long id; // optional for update
        @NotBlank
        public String image;
        @NotBlank
        public String textBlock;
        public Long parentId; // optional
    }
}