package com.voyagia.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "Voyagia Backend");
        response.put("version", "1.0.0.");
        return response;
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        Map<String, String> info = new HashMap<>();
        info.put("application", "Voyagia E-Commerce Backend");
        info.put("description", "Spring Boot REST API for E-Commerce Platform");
        info.put("tech-stack", "Spring Boot 3.5 + Java 17");
        return info;
    }
}
