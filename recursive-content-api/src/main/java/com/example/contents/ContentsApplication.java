package com.example.contents;

import com.example.contents.model.Content;
import com.example.contents.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ContentsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentsApplication.class, args);
    }

    // Seed a tiny tree for quick testing
//    @Bean
//    CommandLineRunner seed(ContentRepository repo) {
//        return args -> {
//            if (repo.count() == 0) {
//                Content root = Content.builder()
//                        .image("https://example.com/root.jpg")
//                        .textBlock("Root node")
//                        .build();
//
//                Content child1 = Content.builder()
//                        .image("https://example.com/child1.jpg")
//                        .textBlock("Child 1")
//                        .parent(root)
//                        .build();
//
//                Content child2 = Content.builder()
//                        .image("https://example.com/child2.jpg")
//                        .textBlock("Child 2")
//                        .parent(root)
//                        .build();
//
//                Content grandchild = Content.builder()
//                        .image("https://example.com/grandchild.jpg")
//                        .textBlock("Grandchild of child 1")
//                        .parent(child1)
//                        .build();
//
//                root.getChildren().add(child1);
//                root.getChildren().add(child2);
//                child1.getChildren().add(grandchild);
//
//                repo.save(root);
//            }
//        };
//    }
}