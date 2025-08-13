package com.example.contents.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Recursive Content API",
                version = "v1",
                description = "Endpoints to fetch content nodes and their recursive children.")
)
public class OpenApiConfig { }