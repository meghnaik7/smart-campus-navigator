package com.megh.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "classrooms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Classroom extends BaseEntity {

    @Column(name = "room_number", nullable = false, length = 20)
    private String roomNumber;

    @Column(name = "name", length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 30)
    private RoomType roomType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "capacity")
    private Integer capacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nearest_node_id")
    private GraphNode nearestNode;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", length = 20)
    @Builder.Default
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.AVAILABLE;

    @Column(name = "has_projector")
    @Builder.Default
    private Boolean hasProjector = false;

    @Column(name = "has_ac")
    @Builder.Default
    private Boolean hasAc = false;

    @Column(name = "has_computers")
    @Builder.Default
    private Boolean hasComputers = false;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    public boolean isActive() { return Boolean.TRUE.equals(isActive); }

    public enum RoomType {
        LECTURE_HALL, SEMINAR_HALL, TUTORIAL_ROOM, COMPUTER_LAB,
        PHYSICS_LAB, CHEMISTRY_LAB, BIOLOGY_LAB, MECHANICAL_WORKSHOP,
        ELECTRONICS_LAB, EXAMINATION_HALL, CONFERENCE_ROOM
    }

    public enum AvailabilityStatus { AVAILABLE, OCCUPIED, MAINTENANCE, CLOSED }
}
