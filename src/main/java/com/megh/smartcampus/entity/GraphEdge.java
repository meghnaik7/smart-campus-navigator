package com.megh.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "graph_edges",
    indexes = {
        @Index(name = "idx_edge_source", columnList = "source_node_id"),
        @Index(name = "idx_edge_target", columnList = "target_node_id")
    })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GraphEdge extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_node_id", nullable = false)
    private GraphNode sourceNode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_node_id", nullable = false)
    private GraphNode targetNode;

    @Column(name = "distance_meters", nullable = false)
    private Double distanceMeters;

    @Enumerated(EnumType.STRING)
    @Column(name = "path_type", length = 20)
    @Builder.Default
    private PathType pathType = PathType.WALKWAY;

    @Column(name = "is_bidirectional")
    @Builder.Default
    private Boolean isBidirectional = true;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    public boolean isActive() { return Boolean.TRUE.equals(isActive); }
    public boolean isBidirectional() { return Boolean.TRUE.equals(isBidirectional); }

    public enum PathType { WALKWAY, ROAD, STAIRCASE, ELEVATOR, CORRIDOR, SHORTCUT }
}
