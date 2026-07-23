package com.megh.smartcampus.dto.response;

import com.megh.smartcampus.entity.Role;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private Role role;
    private String phone;
    private String studentId;
    private String department;
    private Integer yearOfStudy;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
