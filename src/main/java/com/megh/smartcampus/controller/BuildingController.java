package com.megh.smartcampus.controller;

import com.megh.smartcampus.algorithm.trie.TrieService;
import com.megh.smartcampus.dto.request.CreateBuildingRequest;
import com.megh.smartcampus.dto.request.UpdateBuildingRequest;
import com.megh.smartcampus.entity.Building;
import com.megh.smartcampus.exception.AppException;
import com.megh.smartcampus.repository.BuildingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/buildings")
@RequiredArgsConstructor
@Tag(name = "Buildings")
@SecurityRequirement(name = "bearerAuth")
public class BuildingController {

    private final BuildingRepository buildingRepo;
    private final TrieService        trieService;

    // ── Read endpoints (any authenticated user) ──────────────────────

    @GetMapping
    @Operation(summary = "Get all active buildings")
    public ResponseEntity<List<Map<String, Object>>> getAll() {
        return ResponseEntity.ok(
            buildingRepo.findByIsActiveTrue().stream().map(this::toMap).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get building by id")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        Building b = buildingRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Building not found: " + id));
        return ResponseEntity.ok(toMap(b));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get buildings by type — e.g. LIBRARY, CAFETERIA, WASHROOM")
    public ResponseEntity<List<Map<String, Object>>> byType(@PathVariable String type) {
        Building.BuildingType bt;
        try {
            bt = Building.BuildingType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Return 400 with the valid options so callers know what to send
            throw AppException.badRequest(
                "Invalid building type '" + type + "'. Valid values: "
                + Arrays.toString(Building.BuildingType.values()));
        }
        return ResponseEntity.ok(
            buildingRepo.findByTypeAndIsActiveTrue(bt).stream().map(this::toMap).toList());
    }

    @GetMapping("/search")
    @Operation(summary = "Search buildings by name or code")
    public ResponseEntity<Page<Map<String, Object>>> search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable p = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(buildingRepo.search(q, p).map(this::toMap));
    }

    // ── Write endpoints (ADMIN only) ──────────────────────────────────

    /**
     * Creates a new building.
     * Uses @Valid + typed DTO — Bean Validation rejects bad input before this method runs.
     * The 400 response with field-level messages is handled by GlobalExceptionHandler.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Create building")
    public ResponseEntity<Map<String, Object>> create(
            @Valid @RequestBody CreateBuildingRequest req) {

        // Unique-code check (data-integrity)
        if (req.getCode() != null && !req.getCode().isBlank()
                && buildingRepo.existsByCode(req.getCode())) {
            throw AppException.badRequest(
                "A building with code '" + req.getCode() + "' already exists");
        }

        Building b = Building.builder()
            .name(req.getName())
            .code(req.getCode())
            .description(req.getDescription())
            .type(req.getType())
            .floors(req.getFloors())
            .coordinateX(req.getCoordinateX())
            .coordinateY(req.getCoordinateY())
            .imageUrl(req.getImageUrl())
            .isActive(true)
            .build();

        Building saved = buildingRepo.save(b);
        trieService.reload();
        return ResponseEntity.status(HttpStatus.CREATED).body(toMap(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Update building")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBuildingRequest req) {

        Building b = buildingRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Building not found: " + id));

        b.setName(req.getName());
        b.setDescription(req.getDescription());
        b.setFloors(req.getFloors());
        b.setCoordinateX(req.getCoordinateX());
        b.setCoordinateY(req.getCoordinateY());
        b.setImageUrl(req.getImageUrl());

        Building saved = buildingRepo.save(b);
        trieService.reload();
        return ResponseEntity.ok(toMap(saved));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Soft-delete building (sets isActive = false)")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        Building b = buildingRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Building not found: " + id));
        b.setIsActive(false);
        buildingRepo.save(b);
        return ResponseEntity.ok(Map.of("message", "Building deactivated"));
    }

    // ── Response mapping ──────────────────────────────────────────────

    private Map<String, Object> toMap(Building b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",          b.getId());
        m.put("name",        b.getName());
        m.put("code",        b.getCode());
        m.put("description", b.getDescription());
        m.put("type",        b.getType().name());
        m.put("floors",      b.getFloors());
        m.put("coordinateX", b.getCoordinateX());
        m.put("coordinateY", b.getCoordinateY());
        m.put("imageUrl",    b.getImageUrl());
        m.put("isActive",    b.getIsActive());
        return m;
    }
}
