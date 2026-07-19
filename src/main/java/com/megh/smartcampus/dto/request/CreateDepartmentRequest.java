package com.megh.smartcampus.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/** DTO for creating a department. */
@Data
public class CreateDepartmentRequest {

    @NotBlank(message = "Department name is required")
    @Size(max = 100, message = "Name must be 100 characters or fewer")
    private String name;

    @Size(max = 10, message = "Code must be 10 characters or fewer")
    private String code;

    @Size(max = 500)
    private String description;

    @Size(max = 100)
    private String headOfDepartment;

    @Pattern(regexp = "^[0-9+\\-\\s]{7,15}$",
             message = "Phone must be 7-15 digits")
    private String phone;

    @Email(message = "Email must be a valid email address")
    @Size(max = 150)
    private String email;
}
