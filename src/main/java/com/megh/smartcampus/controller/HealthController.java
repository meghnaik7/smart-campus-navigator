package com.megh.smartcampus.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Health")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status",      "UP",
            "application", "Smart Campus Navigator",
            "version",     "1.0.0",
            "timestamp",   LocalDateTime.now().toString(),
            "swagger",     "http://localhost:8080/swagger-ui.html"
        ));
    }

    @GetMapping("/ping")
    @Operation(summary = "Ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
