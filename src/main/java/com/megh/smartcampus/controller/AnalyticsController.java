package com.megh.smartcampus.controller;

import com.megh.smartcampus.analytics.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Analytics")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @Operation(summary = "Admin dashboard stats")
    public ResponseEntity<Map<String, Object>> dashboard() {
        return ResponseEntity.ok(analyticsService.dashboard());
    }

    @GetMapping("/top-searches")
    @Operation(summary = "Top searched queries")
    public ResponseEntity<List<Map<String, Object>>> topSearches(
            @RequestParam(defaultValue="10") int limit) {
        return ResponseEntity.ok(analyticsService.topSearches(limit));
    }

    @GetMapping("/top-destinations")
    @Operation(summary = "Most navigated destinations")
    public ResponseEntity<List<Map<String, Object>>> topDest(
            @RequestParam(defaultValue="10") int limit) {
        return ResponseEntity.ok(analyticsService.topDestinations(limit));
    }
}
