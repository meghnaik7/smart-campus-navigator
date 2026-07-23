package com.megh.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "graph_nodes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GraphNode extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false, length = 30)
    private NodeType nodeType;

    @Column(name = "coordinate_x", nullable = false)
    private Double coordinateX;

    @Column(name = "coordinate_y", nullable = false)
    private Double coordinateY;

    @Column(name = "floor")
    @Builder.Default
    private Integer floor = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id")
    private Building building;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    public boolean isActive() { return Boolean.TRUE.equals(isActive); }

    public enum NodeType {
        BUILDING_ENTRANCE, BUILDING_EXIT, INTERSECTION,
        STAIRCASE, ELEVATOR, CORRIDOR, OUTDOOR_PATH,
        PARKING_LOT, GATE, LANDMARK
    }
}
