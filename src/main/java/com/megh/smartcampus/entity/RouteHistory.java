package com.megh.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "route_history",
    indexes = @Index(name = "idx_route_user", columnList = "user_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RouteHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "source_name", length = 150)
    private String sourceName;

    @Column(name = "destination_name", length = 150)
    private String destinationName;

    @Column(name = "total_distance_meters")
    private Double totalDistanceMeters;

    @Column(name = "estimated_time_minutes")
    private Integer estimatedTimeMinutes;

    @Column(name = "path_nodes", columnDefinition = "TEXT")
    private String pathNodes;
}
