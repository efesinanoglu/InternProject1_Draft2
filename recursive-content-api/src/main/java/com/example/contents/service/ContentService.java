package com.example.contents.service;

import com.example.contents.dto.ContentDto;
import com.example.contents.dto.ContentTreeDto;
import com.example.contents.exception.NotFoundException;
import com.example.contents.model.Content;
import com.example.contents.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentService {

    private final ContentRepository repo;

    public ContentDto getById(Long id) {
        Content c = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Content %d not found".formatted(id)));
        return toDto(c);
    }


    public ContentTreeDto getTree(Long id) {
        Content root = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Content %d not found".formatted(id)));
        return toTreeDto(root);
    }


    @Transactional
    public ContentDto createOrUpdate(ContentDto dto) {
        Content entity = null;
        if (dto.getId() != null) {
            entity = repo.findById(dto.getId())
                    .orElseThrow(() -> new NotFoundException("Content %d not found".formatted(dto.getId())));
        } else {
            entity = new Content();
        }

        entity.setFile(dto.getFile());
        entity.setTextBlock(dto.getTextBlock());

        if (dto.getParentId() != null) {
            Content parent = repo.findById(dto.getParentId())
                    .orElseThrow(() -> new NotFoundException("Parent %d not found".formatted(dto.getParentId())));

        }

        Content saved = repo.save(entity);
        return toDto(saved);
    }

    private ContentDto toDto(Content c) {
        return ContentDto.builder()
                .id(c.getId())
                .File(c.getFile())
                .textBlock(c.getTextBlock())
                .build();
    }

    private ContentTreeDto toTreeDto(Content c) {
        return ContentTreeDto.builder()
                .id(c.getId())
                .File(c.getFile())
                .textBlock(c.getTextBlock())
                .build();
    }
}