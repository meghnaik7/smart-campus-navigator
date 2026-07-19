package com.megh.smartcampus.controller;

import com.megh.smartcampus.algorithm.trie.TrieService;
import com.megh.smartcampus.dto.request.CreateEventRequest;
import com.megh.smartcampus.dto.request.UpdateEventRequest;
import com.megh.smartcampus.entity.*;
import com.megh.smartcampus.exception.AppException;
import com.megh.smartcampus.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Events")
@SecurityRequirement(name = "bearerAuth")
public class EventController {

    private final CampusEventRepository eventRepo;
    private final BuildingRepository    buildingRepo;
    private final GraphNodeRepository   nodeRepo;
    private final TrieService           trieService;

    // ── Read endpoints ────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Get upcoming events (startTime >= now)")
    public ResponseEntity<List<Map<String, Object>>> upcoming() {
        return ResponseEntity.ok(
            eventRepo.findUpcoming(LocalDateTime.now())
                .stream().map(this::toMap).toList());
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming events (alias for /events)")
    public ResponseEntity<List<Map<String, Object>>> upcomingAlias() {
        return upcoming();
    }

    @GetMapping("/all")
    @Operation(summary = "Get all active events regardless of start time")
    public ResponseEntity<List<Map<String, Object>>> all() {
        return ResponseEntity.ok(
            eventRepo.findByIsActiveTrue().stream().map(this::toMap).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by id")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        CampusEvent ev = eventRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Event not found: " + id));
        return ResponseEntity.ok(toMap(ev));
    }

    // ── Write endpoints (ADMIN only) ──────────────────────────────────

    /**
     * Creates a campus event.
     *
     * Business rule: end time must be strictly after start time.
     * This is validated here rather than with a Bean Validation annotation
     * because cross-field validation requires access to both fields.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Create campus event")
    public ResponseEntity<Map<String, Object>> create(
            @Valid @RequestBody CreateEventRequest req) {

        // Cross-field temporal validation
        if (!req.getEndTime().isAfter(req.getStartTime())) {
            throw AppException.badRequest(
                "End time must be after start time. "
                + "Got start=" + req.getStartTime() + ", end=" + req.getEndTime());
        }

        Building building = resolveBuilding(req.getBuildingId());
        GraphNode nearestNode = resolveNode(req.getNearestNodeId());

        CampusEvent event = CampusEvent.builder()
            .title(req.getTitle())
            .description(req.getDescription())
            .startTime(req.getStartTime())
            .endTime(req.getEndTime())
            .venueName(req.getVenueName())
            .organizer(req.getOrganizer())
            .maxParticipants(req.getMaxParticipants())
            .registrationLink(req.getRegistrationLink())
            .eventImageUrl(req.getEventImageUrl())
            .building(building)
            .nearestNode(nearestNode)
            .status(CampusEvent.EventStatus.UPCOMING)
            .isActive(true)
            .build();

        CampusEvent saved = eventRepo.save(event);
        trieService.reload();
        return ResponseEntity.status(HttpStatus.CREATED).body(toMap(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Update event details or status")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventRequest req) {

        CampusEvent ev = eventRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Event not found: " + id));

        if (req.getTitle()       != null) ev.setTitle(req.getTitle());
        if (req.getDescription() != null) ev.setDescription(req.getDescription());
        if (req.getVenueName()   != null) ev.setVenueName(req.getVenueName());
        if (req.getOrganizer()   != null) ev.setOrganizer(req.getOrganizer());
        if (req.getStatus()      != null) ev.setStatus(req.getStatus());

        CampusEvent saved = eventRepo.save(ev);
        trieService.reload();
        return ResponseEntity.ok(toMap(saved));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Cancel event (sets status to CANCELLED)")
    public ResponseEntity<Map<String, String>> cancel(@PathVariable Long id) {
        CampusEvent ev = eventRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Event not found: " + id));
        ev.setStatus(CampusEvent.EventStatus.CANCELLED);
        eventRepo.save(ev);
        return ResponseEntity.ok(Map.of("message", "Event cancelled"));
    }

    // ── Private helpers ───────────────────────────────────────────────

    private Building resolveBuilding(Long id) {
        if (id == null) return null;
        return buildingRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Building not found: " + id));
    }

    private GraphNode resolveNode(Long id) {
        if (id == null) return null;
        return nodeRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Graph node not found: " + id));
    }

    // ── Response mapping ──────────────────────────────────────────────

    private Map<String, Object> toMap(CampusEvent e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",               e.getId());
        m.put("title",            e.getTitle());
        m.put("description",      e.getDescription());
        m.put("startTime",        e.getStartTime().toString());
        m.put("endTime",          e.getEndTime().toString());
        m.put("venueName",        e.getVenueName());
        m.put("organizer",        e.getOrganizer());
        m.put("buildingId",       e.getBuilding()    != null ? e.getBuilding().getId()    : null);
        m.put("buildingName",     e.getBuilding()    != null ? e.getBuilding().getName()  : null);
        m.put("nearestNodeId",    e.getNearestNode() != null ? e.getNearestNode().getId() : null);
        m.put("status",           e.getStatus().name());
        m.put("maxParticipants",  e.getMaxParticipants());
        m.put("registrationLink", e.getRegistrationLink());
        m.put("eventImageUrl",    e.getEventImageUrl());
        return m;
    }
}
