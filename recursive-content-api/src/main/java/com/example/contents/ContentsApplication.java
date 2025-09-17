package com.example.contents;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ContentsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentsApplication.class, args);
    }


}