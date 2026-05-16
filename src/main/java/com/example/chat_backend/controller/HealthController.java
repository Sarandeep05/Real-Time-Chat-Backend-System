package com.example.chat_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Health check and welcome endpoint.
 * Provides a public root URL response and health status for monitoring.
 */
@RestController
public class HealthController {

    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
                "service", "Real-Time Chat Backend API",
                "status", "running",
                "docs", "/swagger-ui/index.html",
                "timestamp", Instant.now().toString()
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
