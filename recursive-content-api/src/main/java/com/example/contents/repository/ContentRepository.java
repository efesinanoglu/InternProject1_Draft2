package com.example.contents.repository;

import com.example.contents.model.Content;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {
    @EntityGraph(attributePaths = {"children"})
    List<Content> findByParentIsNull();
}