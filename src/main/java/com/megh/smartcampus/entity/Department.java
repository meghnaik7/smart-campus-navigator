package com.megh.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Department extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "code", unique = true, length = 10)
    private String code;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "head_of_department", length = 100)
    private String headOfDepartment;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    public boolean isActive() { return Boolean.TRUE.equals(isActive); }
}
