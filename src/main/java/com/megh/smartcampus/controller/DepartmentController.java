package com.megh.smartcampus.controller;

import com.megh.smartcampus.dto.request.CreateDepartmentRequest;
import com.megh.smartcampus.dto.request.UpdateDepartmentRequest;
import com.megh.smartcampus.entity.Department;
import com.megh.smartcampus.exception.AppException;
import com.megh.smartcampus.repository.DepartmentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@Tag(name = "Departments")
@SecurityRequirement(name = "bearerAuth")
public class DepartmentController {

    private final DepartmentRepository deptRepo;

    // ── Read ──────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Get all active departments")
    public ResponseEntity<List<Map<String, Object>>> getAll() {
        return ResponseEntity.ok(
            deptRepo.findByIsActiveTrue().stream().map(this::toMap).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get department by id")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        Department d = deptRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Department not found: " + id));
        return ResponseEntity.ok(toMap(d));
    }

    // ── Write (ADMIN only) ────────────────────────────────────────────

    /**
     * Creates a department.
     * Both name and code must be unique — the DB has unique constraints,
     * but we validate first to return a clear 400 message instead of a
     * DataIntegrityViolationException from the DB.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Create department")
    public ResponseEntity<Map<String, Object>> create(
            @Valid @RequestBody CreateDepartmentRequest req) {

        if (deptRepo.existsByName(req.getName())) {
            throw AppException.badRequest(
                "A department named '" + req.getName() + "' already exists");
        }
        if (req.getCode() != null && !req.getCode().isBlank()
                && deptRepo.existsByCode(req.getCode())) {
            throw AppException.badRequest(
                "Department code '" + req.getCode() + "' is already in use");
        }

        Department dept = Department.builder()
            .name(req.getName())
            .code(req.getCode())
            .description(req.getDescription())
            .headOfDepartment(req.getHeadOfDepartment())
            .phone(req.getPhone())
            .email(req.getEmail())
            .isActive(true)
            .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(toMap(deptRepo.save(dept)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Update department (name and code cannot change)")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDepartmentRequest req) {

        Department d = deptRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Department not found: " + id));

        if (req.getHeadOfDepartment() != null) d.setHeadOfDepartment(req.getHeadOfDepartment());
        if (req.getPhone()            != null) d.setPhone(req.getPhone());
        if (req.getEmail()            != null) d.setEmail(req.getEmail());
        if (req.getDescription()      != null) d.setDescription(req.getDescription());

        return ResponseEntity.ok(toMap(deptRepo.save(d)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Soft-delete department")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        Department d = deptRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Department not found: " + id));
        d.setIsActive(false);
        deptRepo.save(d);
        return ResponseEntity.ok(Map.of("message", "Department deactivated"));
    }

    // ── Response mapping ──────────────────────────────────────────────

    private Map<String, Object> toMap(Department d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",               d.getId());
        m.put("name",             d.getName());
        m.put("code",             d.getCode());
        m.put("description",      d.getDescription());
        m.put("headOfDepartment", d.getHeadOfDepartment());
        m.put("phone",            d.getPhone());
        m.put("email",            d.getEmail());
        m.put("isActive",         d.isActive());
        return m;
    }
}
