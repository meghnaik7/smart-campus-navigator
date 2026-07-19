package com.megh.smartcampus.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/** DTO for creating a faculty member. */
@Data
public class CreateFacultyRequest {

    @NotBlank(message = "Faculty name is required")
    @Size(max = 100, message = "Name must be 100 characters or fewer")
    private String name;

    @Size(max = 100)
    private String designation;

    @Email(message = "Email must be a valid email address")
    @Size(max = 150)
    private String email;

    @Pattern(regexp = "^[0-9+\\-\\s]{7,15}$",
             message = "Phone must be 7-15 digits (may include +, -, spaces)")
    private String phone;

    @Size(max = 255)
    private String specialization;

    @Size(max = 20)
    private String cabinNumber;

    @Min(0) @Max(50)
    private Integer floor;

    @Size(max = 500)
    private String photoUrl;

    private Long departmentId;   // optional FK — 404 thrown if provided but not found
    private Long buildingId;     // optional FK
    private Long nearestNodeId;  // optional FK — links to graph node for routing
}
