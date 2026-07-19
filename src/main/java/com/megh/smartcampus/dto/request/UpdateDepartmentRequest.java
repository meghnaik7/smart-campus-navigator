package com.megh.smartcampus.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/** DTO for updating a department. Name and code cannot change after creation. */
@Data
public class UpdateDepartmentRequest {

    @Size(max = 100)
    private String headOfDepartment;

    @Pattern(regexp = "^[0-9+\\-\\s]{7,15}$",
             message = "Phone must be 7-15 digits")
    private String phone;

    @Email(message = "Email must be a valid email address")
    @Size(max = 150)
    private String email;

    @Size(max = 500)
    private String description;
}
