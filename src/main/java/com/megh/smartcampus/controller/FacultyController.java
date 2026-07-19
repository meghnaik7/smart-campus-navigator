package com.megh.smartcampus.controller;

import com.megh.smartcampus.algorithm.trie.TrieService;
import com.megh.smartcampus.dto.request.CreateFacultyRequest;
import com.megh.smartcampus.dto.request.UpdateFacultyRequest;
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
@RequestMapping("/api/v1/faculty")
@RequiredArgsConstructor
@Tag(name = "Faculty")
@SecurityRequirement(name = "bearerAuth")
public class FacultyController {

    private final FacultyRepository    facultyRepo;
    private final BuildingRepository   buildingRepo;
    private final DepartmentRepository deptRepo;
    private final GraphNodeRepository  nodeRepo;
    private final TrieService          trieService;

    // ── Read endpoints ────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Get all active faculty members")
    public ResponseEntity<List<Map<String, Object>>> getAll() {
        return ResponseEntity.ok(
            facultyRepo.findByIsActiveTrue().stream().map(this::toMap).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get faculty member by id")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        Faculty f = facultyRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Faculty not found: " + id));
        return ResponseEntity.ok(toMap(f));
    }

    @GetMapping("/search")
    @Operation(summary = "Search faculty by name, designation, or specialization")
    public ResponseEntity<Page<Map<String, Object>>> search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable p = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(facultyRepo.search(q, p).map(this::toMap));
    }

    @GetMapping("/department/{deptId}")
    @Operation(summary = "Get all faculty in a specific department")
    public ResponseEntity<List<Map<String, Object>>> byDepartment(@PathVariable Long deptId) {
        // Verify the department exists so we return 404 instead of an empty list
        if (!deptRepo.existsById(deptId)) {
            throw AppException.notFound("Department not found: " + deptId);
        }
        return ResponseEntity.ok(
            facultyRepo.findByDepartmentIdAndIsActiveTrue(deptId)
                .stream().map(this::toMap).toList());
    }

    // ── Write endpoints (ADMIN only) ──────────────────────────────────

    /**
     * Creates a faculty member.
     *
     * Referenced IDs (buildingId, departmentId, nearestNodeId) resolve to
     * a clean 404 if they are provided but don't exist in the database.
     * Silently assigning null when an ID is not found hides configuration
     * mistakes — a hard 404 is more honest.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Create faculty member")
    public ResponseEntity<Map<String, Object>> create(
            @Valid @RequestBody CreateFacultyRequest req) {

        Building building = resolveBuilding(req.getBuildingId());
        Department department = resolveDepartment(req.getDepartmentId());
        GraphNode nearestNode = resolveNode(req.getNearestNodeId());

        Faculty faculty = Faculty.builder()
            .name(req.getName())
            .designation(req.getDesignation())
            .email(req.getEmail())
            .phone(req.getPhone())
            .specialization(req.getSpecialization())
            .cabinNumber(req.getCabinNumber())
            .floor(req.getFloor())
            .photoUrl(req.getPhotoUrl())
            .building(building)
            .department(department)
            .nearestNode(nearestNode)
            .isAvailable(true)
            .isActive(true)
            .build();

        Faculty saved = facultyRepo.save(faculty);
        trieService.reload();
        return ResponseEntity.status(HttpStatus.CREATED).body(toMap(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Update faculty member")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFacultyRequest req) {

        Faculty f = facultyRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Faculty not found: " + id));

        if (req.getDesignation()   != null) f.setDesignation(req.getDesignation());
        if (req.getEmail()         != null) f.setEmail(req.getEmail());
        if (req.getPhone()         != null) f.setPhone(req.getPhone());
        if (req.getSpecialization()!= null) f.setSpecialization(req.getSpecialization());
        if (req.getCabinNumber()   != null) f.setCabinNumber(req.getCabinNumber());
        if (req.getFloor()         != null) f.setFloor(req.getFloor());
        if (req.getPhotoUrl()      != null) f.setPhotoUrl(req.getPhotoUrl());
        if (req.getIsAvailable()   != null) f.setIsAvailable(req.getIsAvailable());
        if (req.getNearestNodeId() != null) {
            f.setNearestNode(resolveNode(req.getNearestNodeId()));
        }

        Faculty saved = facultyRepo.save(f);
        trieService.reload();
        return ResponseEntity.ok(toMap(saved));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Deactivate faculty member")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        Faculty f = facultyRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Faculty not found: " + id));
        f.setIsActive(false);
        facultyRepo.save(f);
        return ResponseEntity.ok(Map.of("message", "Faculty deactivated"));
    }

    // ── Private helpers ───────────────────────────────────────────────

    /** Resolves a building by ID. Returns null if id is null; throws 404 if not found. */
    private Building resolveBuilding(Long id) {
        if (id == null) return null;
        return buildingRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Building not found: " + id));
    }

    private Department resolveDepartment(Long id) {
        if (id == null) return null;
        return deptRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Department not found: " + id));
    }

    private GraphNode resolveNode(Long id) {
        if (id == null) return null;
        return nodeRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Graph node not found: " + id));
    }

    // ── Response mapping ──────────────────────────────────────────────

    private Map<String, Object> toMap(Faculty f) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",             f.getId());
        m.put("name",           f.getName());
        m.put("designation",    f.getDesignation());
        m.put("email",          f.getEmail());
        m.put("phone",          f.getPhone());
        m.put("specialization", f.getSpecialization());
        m.put("cabinNumber",    f.getCabinNumber());
        m.put("floor",          f.getFloor());
        m.put("photoUrl",       f.getPhotoUrl());
        m.put("departmentId",   f.getDepartment() != null ? f.getDepartment().getId()   : null);
        m.put("departmentName", f.getDepartment() != null ? f.getDepartment().getName() : null);
        m.put("buildingId",     f.getBuilding()   != null ? f.getBuilding().getId()     : null);
        m.put("buildingName",   f.getBuilding()   != null ? f.getBuilding().getName()   : null);
        m.put("nearestNodeId",  f.getNearestNode()!= null ? f.getNearestNode().getId()  : null);
        m.put("isAvailable",    f.isAvailable());
        m.put("isActive",       f.isActive());
        return m;
    }
}
