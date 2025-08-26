package com.example.contents.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;



@Entity
@Table(name = "contents")
@Getter
@Setter
@NoArgsConstructor
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "file", length = 2048)
    private String file; // Image URL or path

    @Column(name = "text_block", columnDefinition = "text")
    private String textBlock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Content parent;

}