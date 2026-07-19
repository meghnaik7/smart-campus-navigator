package com.megh.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "faculty")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Faculty extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "designation", length = 100)
    private String designation;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "specialization", length = 255)
    private String specialization;

    @Column(name = "cabin_number", length = 20)
    private String cabinNumber;

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id")
    private Building building;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nearest_node_id")
    private GraphNode nearestNode;

    @Column(name = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    public boolean isActive() { return Boolean.TRUE.equals(isActive); }
    public boolean isAvailable() { return Boolean.TRUE.equals(isAvailable); }
}
