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

    private final GraphService        graphService;
    private final GraphNodeRepository nodeRepo;
    private final GraphEdgeRepository edgeRepo;
    private final BuildingRepository  buildingRepo;

    // ── Nodes ─────────────────────────────────────────────────────────

    @GetMapping("/nodes")
    @Operation(summary = "Get all active campus nodes")
    public ResponseEntity<List<Map<String, Object>>> getNodes() {
        return ResponseEntity.ok(
            nodeRepo.findAllActive().stream().map(this::nodeMap).toList());
    }

    @PostMapping("/nodes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Add a new campus node")
    public ResponseEntity<Map<String, Object>> createNode(
            @Valid @RequestBody CreateGraphNodeRequest req) {

        Building building = null;
        if (req.getBuildingId() != null) {
            building = buildingRepo.findById(req.getBuildingId())
                .orElseThrow(() -> AppException.notFound("Building not found: " + req.getBuildingId()));
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
            .build();

        GraphNode saved = nodeRepo.save(node);
        graphService.reload();
        return ResponseEntity.status(HttpStatus.CREATED).body(nodeMap(saved));
    }

    @PutMapping("/nodes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Update a node")
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
    @Operation(summary = "[ADMIN] Deactivate a node (removes it from routing)")
    public ResponseEntity<Map<String, String>> deleteNode(@PathVariable Long id) {
        GraphNode node = nodeRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Node not found: " + id));
        node.setIsActive(false);
        nodeRepo.save(node);
        graphService.reload();
        return ResponseEntity.ok(Map.of("message", "Node deactivated"));
    }

    // ── Edges ─────────────────────────────────────────────────────────

    @GetMapping("/edges")
    @Operation(summary = "Get all active edges")
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
                )).toList()
        );
    }

    @PostMapping("/edges")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Add an edge (walkable path between two nodes)")
    public ResponseEntity<Map<String, Object>> createEdge(
            @Valid @RequestBody CreateGraphEdgeRequest req) {

        if (req.getSourceNodeId().equals(req.getTargetNodeId()))
            throw AppException.badRequest("Source and target nodes must be different");

        GraphNode source = nodeRepo.findById(req.getSourceNodeId())
            .orElseThrow(() -> AppException.notFound("Source node not found: " + req.getSourceNodeId()));
        GraphNode target = nodeRepo.findById(req.getTargetNodeId())
            .orElseThrow(() -> AppException.notFound("Target node not found: " + req.getTargetNodeId()));

        // Prevent duplicate active edge between the same pair
        boolean exists = edgeRepo.findAllActiveWithNodes().stream().anyMatch(e ->
            e.getSourceNode().getId().equals(req.getSourceNodeId()) &&
            e.getTargetNode().getId().equals(req.getTargetNodeId()));
        if (exists)
            throw AppException.badRequest("An active edge already exists from node "
                + req.getSourceNodeId() + " to node " + req.getTargetNodeId());

        GraphEdge edge = GraphEdge.builder()
            .sourceNode(source).targetNode(target)
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

    // ── Routing ───────────────────────────────────────────────────────

    @GetMapping("/navigate/route")
    @Operation(summary = "Find shortest path between two nodes (Dijkstra)",
               description = "Returns total distance, walking time, and ordered list of nodes to walk through.")
    public ResponseEntity<Map<String, Object>> route(
            @RequestParam Long from,
            @RequestParam Long to) {
        RouteResult result = graphService.dijkstra(from, to);
        return ResponseEntity.ok(routeMap(result));
    }

    @GetMapping("/navigate/nearest")
    @Operation(summary = "Find nearest facility by building type (e.g. WASHROOM, CAFETERIA, MEDICAL)")
    public ResponseEntity<Map<String, Object>> nearest(
            @RequestParam Long from,
            @RequestParam String type) {

        Building.BuildingType bt;
        try { bt = Building.BuildingType.valueOf(type.toUpperCase()); }
        catch (IllegalArgumentException e) {
            throw AppException.badRequest("Invalid type '" + type + "'. Valid values: "
                + Arrays.toString(Building.BuildingType.values()));
        }

        List<Long> candidates = buildingRepo.findByTypeAndIsActiveTrue(bt).stream()
            .flatMap(b -> nodeRepo.findByBuildingIdAndIsActiveTrue(b.getId()).stream())
            .map(GraphNode::getId)
            .collect(Collectors.toList());

        if (candidates.isEmpty())
            return ResponseEntity.ok(Map.of("found", false, "message", "No " + type + " found on campus"));

        return ResponseEntity.ok(routeMap(graphService.nearest(from, candidates)));
    }

    @PostMapping("/graph/reload")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Rebuild the in-memory graph from the database")
    public ResponseEntity<Map<String, String>> reload() {
        graphService.reload();
        return ResponseEntity.ok(Map.of("message", "Graph reloaded", "stats", graphService.stats()));
    }

    // ── Helpers ───────────────────────────────────────────────────────

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
        m.put("distanceDisplay",     r.getTotalMeters() < 1000
            ? String.format("%.0f m",  r.getTotalMeters())
            : String.format("%.1f km", r.getTotalMeters() / 1000));
        m.put("estimatedMinutes",    r.getEstimatedMinutes());
        m.put("timeDisplay",         r.getEstimatedMinutes() + " min"
            + (r.getEstimatedMinutes() != 1 ? "s" : ""));
        m.put("nodeCount",           r.getPath().size());
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
}
