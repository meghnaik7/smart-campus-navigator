package com.megh.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users",
    uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = {"email"}),
    indexes = @Index(name = "idx_users_email", columnList = "email"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    @Builder.Default
    private Role role = Role.ROLE_STUDENT;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "student_id", unique = true, length = 50)
    private String studentId;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "year_of_study")
    private Integer yearOfStudy;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Transient
    public String getFullName() { return firstName + " " + lastName; }

    public boolean isActive() { return Boolean.TRUE.equals(isActive); }
}
