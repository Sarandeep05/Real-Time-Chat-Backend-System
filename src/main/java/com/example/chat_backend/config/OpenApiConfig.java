package com.example.chat_backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 / Swagger UI configuration.
 *
 * Provides interactive API documentation at /swagger-ui/index.html
 * with a built-in JWT authorization button for testing secured endpoints.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Real-Time Chat Backend API",
                version = "1.0.0",
                description = "Production-ready real-time chat backend supporting one-to-one messaging, "
                        + "group chats, message persistence, read receipts, and WebSocket communication. "
                        + "Built with Spring Boot 3, MongoDB, and STOMP over WebSocket.",
                contact = @Contact(
                        name = "Sarandeep",
                        url = "https://github.com/Sarandeep05/Real-Time-Chat-Backend-System"
                )
        ),
        security = @SecurityRequirement(name = "Bearer Authentication")
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER,
        description = "Enter your JWT token obtained from /auth/login"
)
public class OpenApiConfig {
}
