package com.megh.smartcampus.controller;

import com.megh.smartcampus.algorithm.trie.TrieService;
import com.megh.smartcampus.dto.request.CreateClassroomRequest;
import com.megh.smartcampus.entity.*;
import com.megh.smartcampus.exception.AppException;
import com.megh.smartcampus.repository.*;
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
@RequestMapping("/api/v1/classrooms")
@RequiredArgsConstructor
@Tag(name = "Classrooms & Labs")
@SecurityRequirement(name = "bearerAuth")
public class ClassroomController {

    private final ClassroomRepository classroomRepo;
    private final BuildingRepository  buildingRepo;
    private final GraphNodeRepository nodeRepo;
    private final TrieService         trieService;

    // ── Read endpoints ────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Get all active classrooms (optionally filter by availability status)")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @RequestParam(required = false) String status) {

        if (status != null) {
            Classroom.AvailabilityStatus parsed;
            try {
                parsed = Classroom.AvailabilityStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw AppException.badRequest(
                    "Invalid status '" + status + "'. Valid values: "
                    + Arrays.toString(Classroom.AvailabilityStatus.values()));
            }
            return ResponseEntity.ok(
                classroomRepo.findByAvailabilityStatusAndIsActiveTrue(parsed)
                    .stream().map(this::toMap).toList());
        }

        return ResponseEntity.ok(
            classroomRepo.findByIsActiveTrue().stream().map(this::toMap).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get classroom by id")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        Classroom c = classroomRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Classroom not found: " + id));
        return ResponseEntity.ok(toMap(c));
    }

    @GetMapping("/search")
    @Operation(summary = "Search classrooms by room number or name")
    public ResponseEntity<Page<Map<String, Object>>> search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable p = PageRequest.of(page, size, Sort.by("roomNumber"));
        return ResponseEntity.ok(classroomRepo.search(q, p).map(this::toMap));
    }

    // ── Write endpoints (ADMIN only) ──────────────────────────────────

    /**
     * Creates a classroom.
     *
     * Integrity checks performed here (not in the DB layer because we want
     * a clean 400 message, not a cryptic DB constraint violation):
     *   1. The referenced building must exist.
     *   2. The room number must be unique within that building.
     *   3. Capacity is validated positive via @Positive in the DTO.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Create classroom or lab")
    public ResponseEntity<Map<String, Object>> create(
            @Valid @RequestBody CreateClassroomRequest req) {

        // 404 if building doesn't exist
        Building building = buildingRepo.findById(req.getBuildingId())
            .orElseThrow(() -> AppException.notFound(
                "Building not found: " + req.getBuildingId()));

        // Room number must be unique within the same building
        boolean duplicate = classroomRepo.findByIsActiveTrue().stream()
            .anyMatch(c -> c.getBuilding() != null
                && c.getBuilding().getId().equals(building.getId())
                && c.getRoomNumber().equalsIgnoreCase(req.getRoomNumber()));
        if (duplicate) {
            throw AppException.badRequest(
                "Room number '" + req.getRoomNumber()
                + "' already exists in building '" + building.getName() + "'");
        }

        // Resolve optional nearest node — 404 if provided but not found
        GraphNode nearestNode = null;
        if (req.getNearestNodeId() != null) {
            nearestNode = nodeRepo.findById(req.getNearestNodeId())
                .orElseThrow(() -> AppException.notFound(
                    "Graph node not found: " + req.getNearestNodeId()));
        }

        Classroom classroom = Classroom.builder()
            .roomNumber(req.getRoomNumber())
            .name(req.getName())
            .roomType(req.getRoomType())
            .building(building)
            .floor(req.getFloor())
            .capacity(req.getCapacity())
            .nearestNode(nearestNode)
            .availabilityStatus(Classroom.AvailabilityStatus.AVAILABLE)
            .hasProjector(req.isHasProjector())
            .hasAc(req.isHasAc())
            .hasComputers(req.isHasComputers())
            .isActive(true)
            .build();

        Classroom saved = classroomRepo.save(classroom);
        trieService.reload();
        return ResponseEntity.status(HttpStatus.CREATED).body(toMap(saved));
    }

    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Update classroom availability — AVAILABLE / OCCUPIED / MAINTENANCE / CLOSED")
    public ResponseEntity<Map<String, String>> updateAvailability(
            @PathVariable Long id,
            @RequestParam String status) {

        Classroom.AvailabilityStatus parsed;
        try {
            parsed = Classroom.AvailabilityStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw AppException.badRequest(
                "Invalid status '" + status + "'. Valid values: "
                + Arrays.toString(Classroom.AvailabilityStatus.values()));
        }

        Classroom classroom = classroomRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Classroom not found: " + id));
        classroom.setAvailabilityStatus(parsed);
        classroomRepo.save(classroom);
        return ResponseEntity.ok(Map.of("message", "Status updated to " + parsed.name()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Soft-delete classroom")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        Classroom classroom = classroomRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Classroom not found: " + id));
        classroom.setIsActive(false);
        classroomRepo.save(classroom);
        return ResponseEntity.ok(Map.of("message", "Classroom deactivated"));
    }

    // ── Response mapping ──────────────────────────────────────────────

    private Map<String, Object> toMap(Classroom c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",                 c.getId());
        m.put("roomNumber",         c.getRoomNumber());
        m.put("name",               c.getName());
        m.put("roomType",           c.getRoomType().name());
        m.put("buildingId",         c.getBuilding() != null ? c.getBuilding().getId()   : null);
        m.put("buildingName",       c.getBuilding() != null ? c.getBuilding().getName() : null);
        m.put("floor",              c.getFloor());
        m.put("capacity",           c.getCapacity());
        m.put("nearestNodeId",      c.getNearestNode() != null ? c.getNearestNode().getId() : null);
        m.put("availabilityStatus", c.getAvailabilityStatus().name());
        m.put("hasProjector",       c.getHasProjector());
        m.put("hasAc",              c.getHasAc());
        m.put("hasComputers",       c.getHasComputers());
        m.put("isActive",           c.isActive());
        return m;
    }
}
