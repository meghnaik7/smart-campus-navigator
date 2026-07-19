package com.megh.smartcampus.controller;

import com.megh.smartcampus.algorithm.graph.GraphService;
import com.megh.smartcampus.algorithm.graph.RouteResult;
import com.megh.smartcampus.dto.request.CreateGraphEdgeRequest;
import com.megh.smartcampus.dto.request.CreateGraphNodeRequest;
import com.megh.smartcampus.dto.request.UpdateGraphNodeRequest;
import com.megh.smartcampus.entity.*;
import com.megh.smartcampus.exception.AppException;
import com.megh.smartcampus.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Navigation & Graph")
@SecurityRequirement(name = "bearerAuth")
public class GraphController {

    private static final Logger log = LoggerFactory.getLogger(GraphController.class);

    private final GraphService           graphService;
    private final GraphNodeRepository    nodeRepo;
    private final GraphEdgeRepository    edgeRepo;
    private final BuildingRepository     buildingRepo;
    private final FacultyRepository      facultyRepo;
    private final ClassroomRepository    classroomRepo;
    private final CampusEventRepository  eventRepo;
    private final SearchHistoryRepository searchHistoryRepo;
    private final RouteHistoryRepository  routeHistoryRepo;
    private final UserRepository          userRepo;

    // ── Graph nodes ───────────────────────────────────────────────────

    @GetMapping("/nodes")
    @Operation(summary = "Get all active graph nodes (used to render the campus map)")
    public ResponseEntity<List<Map<String, Object>>> getNodes() {
        return ResponseEntity.ok(
            nodeRepo.findAllActive().stream().map(this::nodeMap).toList());
    }

    /**
     * Creates a new campus map node.
     * Uses @Valid + typed DTO — coordinates, type, and name are validated
     * before this method body executes.
     */
    @PostMapping("/nodes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Create graph node")
    public ResponseEntity<Map<String, Object>> createNode(
            @Valid @RequestBody CreateGraphNodeRequest req) {

        // Resolve optional building — 404 if ID given but not found
        Building building = null;
        if (req.getBuildingId() != null) {
            building = buildingRepo.findById(req.getBuildingId())
                .orElseThrow(() -> AppException.notFound(
                    "Building not found: " + req.getBuildingId()));
        }

        GraphNode node = GraphNode.builder()
            .name(req.getName())
            .nodeType(req.getNodeType())
            .coordinateX(req.getCoordinateX())
            .coordinateY(req.getCoordinateY())
            .floor(req.getFloor() != null ? req.getFloor() : 0)
            .building(building)
            .description(req.getDescription())
            .isActive(true)
            .isAccessible(true)
            .build();

        GraphNode saved = nodeRepo.save(node);
        graphService.reload();
        return ResponseEntity.status(HttpStatus.CREATED).body(nodeMap(saved));
    }

    @PutMapping("/nodes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Update graph node position or name")
    public ResponseEntity<Map<String, Object>> updateNode(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGraphNodeRequest req) {

        GraphNode node = nodeRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Node not found: " + id));

        if (req.getName()        != null) node.setName(req.getName());
        if (req.getCoordinateX() != null) node.setCoordinateX(req.getCoordinateX());
        if (req.getCoordinateY() != null) node.setCoordinateY(req.getCoordinateY());
        if (req.getDescription() != null) node.setDescription(req.getDescription());

        graphService.reload();
        return ResponseEntity.ok(nodeMap(nodeRepo.save(node)));
    }

    @DeleteMapping("/nodes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Deactivate graph node")
    public ResponseEntity<Map<String, String>> deleteNode(@PathVariable Long id) {
        GraphNode node = nodeRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Node not found: " + id));
        node.setIsActive(false);
        nodeRepo.save(node);
        graphService.reload();
        return ResponseEntity.ok(Map.of("message", "Node deactivated"));
    }

    // ── Graph edges ───────────────────────────────────────────────────

    @GetMapping("/edges")
    @Operation(summary = "Get all active graph edges")
    public ResponseEntity<List<Map<String, Object>>> getEdges() {
        return ResponseEntity.ok(
            edgeRepo.findAllActiveWithNodes().stream().map(e ->
                Map.<String, Object>of(
                    "id",             e.getId(),
                    "sourceNodeId",   e.getSourceNode().getId(),
                    "sourceName",     e.getSourceNode().getName(),
                    "targetNodeId",   e.getTargetNode().getId(),
                    "targetName",     e.getTargetNode().getName(),
                    "distanceMeters", e.getDistanceMeters(),
                    "bidirectional",  e.isBidirectional()
                )
            ).toList()
        );
    }

    /**
     * Creates an edge (walkable path) between two nodes.
     *
     * Integrity checks:
     *   1. Both nodes must exist (404 otherwise).
     *   2. Source and target must be different nodes.
     *   3. No duplicate active edge for the same source-target pair.
     *   4. Distance must be positive (enforced by @Positive in DTO).
     */
    @PostMapping("/edges")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Add a walkable path between two nodes")
    public ResponseEntity<Map<String, Object>> createEdge(
            @Valid @RequestBody CreateGraphEdgeRequest req) {

        if (req.getSourceNodeId().equals(req.getTargetNodeId())) {
            throw AppException.badRequest(
                "Source and target nodes cannot be the same");
        }

        GraphNode source = nodeRepo.findById(req.getSourceNodeId())
            .orElseThrow(() -> AppException.notFound(
                "Source node not found: " + req.getSourceNodeId()));
        GraphNode target = nodeRepo.findById(req.getTargetNodeId())
            .orElseThrow(() -> AppException.notFound(
                "Target node not found: " + req.getTargetNodeId()));

        // Prevent duplicate active edges between the same pair
        boolean edgeExists = edgeRepo.findAllActiveWithNodes().stream()
            .anyMatch(e ->
                e.getSourceNode().getId().equals(req.getSourceNodeId())
                && e.getTargetNode().getId().equals(req.getTargetNodeId()));
        if (edgeExists) {
            throw AppException.badRequest(
                "An active edge already exists from node "
                + req.getSourceNodeId() + " to node " + req.getTargetNodeId());
        }

        GraphEdge edge = GraphEdge.builder()
            .sourceNode(source)
            .targetNode(target)
            .distanceMeters(req.getDistanceMeters())
            .pathType(req.getPathType() != null ? req.getPathType() : GraphEdge.PathType.WALKWAY)
            .isBidirectional(req.isBidirectional())
            .isActive(true)
            .build();

        GraphEdge saved = edgeRepo.save(edge);
        graphService.reload();

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "id",             saved.getId(),
            "sourceNodeId",   source.getId(),
            "sourceName",     source.getName(),
            "targetNodeId",   target.getId(),
            "targetName",     target.getName(),
            "distanceMeters", saved.getDistanceMeters(),
            "bidirectional",  saved.isBidirectional()
        ));
    }

    @DeleteMapping("/edges/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Deactivate an edge (removes path from routing)")
    public ResponseEntity<Map<String, String>> deleteEdge(@PathVariable Long id) {
        GraphEdge edge = edgeRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Edge not found: " + id));
        edge.setIsActive(false);
        edgeRepo.save(edge);
        graphService.reload();
        return ResponseEntity.ok(Map.of("message", "Edge deactivated"));
    }

    // ── Navigation / routing ──────────────────────────────────────────

    @GetMapping("/navigate/route")
    @Operation(summary = "Find shortest route between two nodes using Dijkstra")
    public ResponseEntity<Map<String, Object>> route(
            @RequestParam Long from,
            @RequestParam Long to,
            @AuthenticationPrincipal UserDetails ud) {

        RouteResult result = graphService.dijkstra(from, to);
        saveRouteHistory(ud, result, "Node #" + from, "Node #" + to);
        return ResponseEntity.ok(routeMap(result));
    }

    @GetMapping("/navigate/faculty/{id}")
    @Operation(summary = "Navigate to a faculty member's cabin")
    public ResponseEntity<Map<String, Object>> toFaculty(
            @PathVariable Long id,
            @RequestParam Long from,
            @AuthenticationPrincipal UserDetails ud) {

        Faculty faculty = facultyRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Faculty not found: " + id));
        if (faculty.getNearestNode() == null) {
            throw AppException.badRequest(
                "Faculty '" + faculty.getName() + "' has no campus location set yet");
        }

        RouteResult result = graphService.dijkstra(from, faculty.getNearestNode().getId());
        saveRouteHistory(ud, result, "Node #" + from, faculty.getName());
        return ResponseEntity.ok(routeMap(result));
    }

    @GetMapping("/navigate/classroom/{id}")
    @Operation(summary = "Navigate to a classroom or lab")
    public ResponseEntity<Map<String, Object>> toClassroom(
            @PathVariable Long id,
            @RequestParam Long from,
            @AuthenticationPrincipal UserDetails ud) {

        Classroom classroom = classroomRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Classroom not found: " + id));
        if (classroom.getNearestNode() == null) {
            throw AppException.badRequest(
                "Classroom '" + classroom.getRoomNumber() + "' has no campus location set yet");
        }

        RouteResult result = graphService.dijkstra(from, classroom.getNearestNode().getId());
        saveRouteHistory(ud, result, "Node #" + from, classroom.getRoomNumber());
        return ResponseEntity.ok(routeMap(result));
    }

    @GetMapping("/navigate/event/{id}")
    @Operation(summary = "Navigate to an event venue")
    public ResponseEntity<Map<String, Object>> toEvent(
            @PathVariable Long id,
            @RequestParam Long from) {

        CampusEvent event = eventRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Event not found: " + id));
        if (event.getNearestNode() == null) {
            throw AppException.badRequest(
                "Event '" + event.getTitle() + "' has no venue location set yet");
        }

        return ResponseEntity.ok(routeMap(
            graphService.dijkstra(from, event.getNearestNode().getId())));
    }

    @GetMapping("/navigate/nearest")
    @Operation(summary = "Find the nearest facility by building type (e.g. WASHROOM, CAFETERIA, MEDICAL)")
    public ResponseEntity<Map<String, Object>> nearest(
            @RequestParam Long from,
            @RequestParam String type) {

        Building.BuildingType bt;
        try {
            bt = Building.BuildingType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw AppException.badRequest(
                "Invalid type '" + type + "'. Valid values: "
                + Arrays.toString(Building.BuildingType.values()));
        }

        List<Long> candidateNodeIds = buildingRepo.findByTypeAndIsActiveTrue(bt).stream()
            .flatMap(b -> nodeRepo.findByBuildingIdAndIsActiveTrue(b.getId()).stream())
            .map(GraphNode::getId)
            .collect(Collectors.toList());

        if (candidateNodeIds.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "found",   false,
                "message", "No active " + type + " found on campus"));
        }

        return ResponseEntity.ok(routeMap(graphService.nearest(from, candidateNodeIds)));
    }

    @PostMapping("/graph/reload")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Rebuild the in-memory graph from the database")
    public ResponseEntity<Map<String, String>> reloadGraph() {
        graphService.reload();
        return ResponseEntity.ok(Map.of(
            "message", "Graph reloaded successfully",
            "stats",   graphService.stats()));
    }

    // ── Private helpers ───────────────────────────────────────────────

    private Map<String, Object> nodeMap(GraphNode n) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",           n.getId());
        m.put("name",         n.getName());
        m.put("nodeType",     n.getNodeType().name());
        m.put("coordinateX",  n.getCoordinateX());
        m.put("coordinateY",  n.getCoordinateY());
        m.put("floor",        n.getFloor());
        m.put("buildingId",   n.getBuilding() != null ? n.getBuilding().getId()   : null);
        m.put("buildingName", n.getBuilding() != null ? n.getBuilding().getName() : null);
        m.put("description",  n.getDescription());
        return m;
    }

    private Map<String, Object> routeMap(RouteResult r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("found",               r.isFound());
        m.put("message",             r.getMessage());
        m.put("totalDistanceMeters", r.getTotalMeters());
        m.put("distanceDisplay",
            r.getTotalMeters() < 1000
                ? String.format("%.0f m",  r.getTotalMeters())
                : String.format("%.1f km", r.getTotalMeters() / 1000));
        m.put("estimatedMinutes", r.getEstimatedMinutes());
        m.put("timeDisplay",
            r.getEstimatedMinutes() + " min" + (r.getEstimatedMinutes() != 1 ? "s" : ""));
        m.put("nodeCount", r.getPath().size());
        m.put("path", r.getPath().stream().map(p ->
            Map.<String, Object>of(
                "id",    p.getId(),
                "name",  p.getName(),
                "x",     p.getX(),
                "y",     p.getY(),
                "floor", p.getFloor()
            )).toList());
        return m;
    }

    /**
     * Persists route history for analytics.
     * Logged at WARN level if it fails — never silently ignored,
     * but also never allowed to crash the main navigation response.
     */
    private void saveRouteHistory(UserDetails ud, RouteResult result,
                                  String sourceName, String destName) {
        if (ud == null || !result.isFound()) return;
        try {
            userRepo.findByEmail(ud.getUsername()).ifPresent(user ->
                routeHistoryRepo.save(RouteHistory.builder()
                    .user(user)
                    .sourceName(sourceName)
                    .destinationName(destName)
                    .totalDistanceMeters(result.getTotalMeters())
                    .estimatedTimeMinutes(result.getEstimatedMinutes())
                    .build()));
        } catch (Exception ex) {
            // History logging must not fail the navigation response,
            // but we want to know if something is wrong.
            log.warn("Failed to save route history for user '{}': {}",
                ud.getUsername(), ex.getMessage());
        }
    }
}
