package com.megh.smartcampus.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/** DTO for updating an existing faculty member. */
@Data
public class UpdateFacultyRequest {

    @Size(max = 100)
    private String designation;

    @Email(message = "Email must be a valid email address")
    @Size(max = 150)
    private String email;

    @Pattern(regexp = "^[0-9+\\-\\s]{7,15}$",
             message = "Phone must be 7-15 digits")
    private String phone;

    @Size(max = 255)
    private String specialization;

    @Size(max = 20)
    private String cabinNumber;

    @Min(0) @Max(50)
    private Integer floor;

    @Size(max = 500)
    private String photoUrl;

    private Boolean isAvailable;
    private Long nearestNodeId;
}
