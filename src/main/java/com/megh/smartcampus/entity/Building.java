package com.megh.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "buildings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Building extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "code", unique = true, length = 30)
    private String code;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private BuildingType type;

    @Column(name = "floors")
    private Integer floors;

    @Column(name = "coordinate_x")
    private Double coordinateX;

    @Column(name = "coordinate_y")
    private Double coordinateY;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    public enum BuildingType {
        ACADEMIC, ADMINISTRATIVE, LABORATORY, LIBRARY, CAFETERIA,
        HOSTEL, SPORTS, MEDICAL, PARKING, AUDITORIUM, WASHROOM,
        OFFICE, EXAMINATION, OTHER
    }
}
