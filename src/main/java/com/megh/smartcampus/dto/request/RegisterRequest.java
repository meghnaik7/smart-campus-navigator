package com.megh.smartcampus.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    // Only firstName and email are truly required for an account to work.
    // lastName, studentId, department, yearOfStudy are all optional.

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be 2-50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must be 50 characters or fewer")
    private String lastName;       // optional

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    private String password;

    private String phone;          // optional
    private String studentId;      // optional
    private String department;     // optional

    @Min(value = 1, message = "Year of study must be 1 or more")
    @Max(value = 6, message = "Year of study must be 6 or fewer")
    private Integer yearOfStudy;   // optional — Integer so null is accepted
}
