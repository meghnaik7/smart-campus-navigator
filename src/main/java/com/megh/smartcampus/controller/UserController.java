package com.megh.smartcampus.controller;

import com.megh.smartcampus.dto.response.UserResponse;
import com.megh.smartcampus.entity.User;
import com.megh.smartcampus.exception.AppException;
import com.megh.smartcampus.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    @GetMapping("/profile")
    @Operation(summary = "Get my profile")
    public ResponseEntity<UserResponse> profile(@AuthenticationPrincipal UserDetails ud) {
        User u = userRepo.findByEmail(ud.getUsername())
            .orElseThrow(() -> AppException.notFound("User not found"));
        return ResponseEntity.ok(toDto(u));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update my profile")
    public ResponseEntity<UserResponse> update(@AuthenticationPrincipal UserDetails ud,
                                               @RequestBody Map<String, Object> body) {
        User u = userRepo.findByEmail(ud.getUsername())
            .orElseThrow(() -> AppException.notFound("User not found"));
        if (body.containsKey("firstName"))   u.setFirstName((String) body.get("firstName"));
        if (body.containsKey("lastName"))    u.setLastName((String) body.get("lastName"));
        if (body.containsKey("phone"))       u.setPhone((String) body.get("phone"));
        if (body.containsKey("department"))  u.setDepartment((String) body.get("department"));
        if (body.containsKey("yearOfStudy") && body.get("yearOfStudy") != null)
            u.setYearOfStudy((Integer) body.get("yearOfStudy"));
        return ResponseEntity.ok(toDto(userRepo.save(u)));
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody Map<String, String> body) {
        User u = userRepo.findByEmail(ud.getUsername())
            .orElseThrow(() -> AppException.notFound("User not found"));
        if (!encoder.matches(body.get("currentPassword"), u.getPassword()))
            throw AppException.badRequest("Current password is incorrect");
        String np = body.get("newPassword");
        if (np == null || np.length() < 8)
            throw AppException.badRequest("New password must be at least 8 characters");
        u.setPassword(encoder.encode(np));
        userRepo.save(u);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    // Admin endpoints
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] List all users")
    public ResponseEntity<Page<UserResponse>> allUsers(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) {
        Pageable p = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(userRepo.findAll(p).map(this::toDto));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Search users")
    public ResponseEntity<Page<UserResponse>> search(
            @RequestParam String q,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) {
        Pageable p = PageRequest.of(page, size);
        return ResponseEntity.ok(userRepo.searchUsers(q, p).map(this::toDto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Get user by id")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        User u = userRepo.findById(id).orElseThrow(() -> AppException.notFound("User not found: " + id));
        return ResponseEntity.ok(toDto(u));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Toggle user active status")
    public ResponseEntity<Map<String, Object>> toggleStatus(
            @PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        User u = userRepo.findById(id).orElseThrow(() -> AppException.notFound("User not found: " + id));
        u.setIsActive(body.get("isActive"));
        userRepo.save(u);
        return ResponseEntity.ok(Map.of("id", id, "isActive", u.getIsActive()));
    }

    private UserResponse toDto(User u) {
        return UserResponse.builder()
            .id(u.getId()).firstName(u.getFirstName()).lastName(u.getLastName())
            .fullName(u.getFullName()).email(u.getEmail()).role(u.getRole())
            .phone(u.getPhone()).studentId(u.getStudentId()).department(u.getDepartment())
            .yearOfStudy(u.getYearOfStudy()).isActive(u.getIsActive())
            .createdAt(u.getCreatedAt()).build();
    }
}
