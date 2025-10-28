package com.sonarqube.adminapp.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SonarQube Admin API")
                        .version("1.0.0")
                        .description("API for managing SonarQube projects and cleanup operations")
                        .contact(new Contact()
                                .name("Admin")
                                .email("admin@example.com")));
    }
}

