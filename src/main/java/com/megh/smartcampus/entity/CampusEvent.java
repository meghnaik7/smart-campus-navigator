package com.megh.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "campus_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CampusEvent extends BaseEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id")
    private Building building;

    @Column(name = "venue_name", length = 200)
    private String venueName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nearest_node_id")
    private GraphNode nearestNode;

    @Column(name = "organizer", length = 100)
    private String organizer;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "registration_link", length = 500)
    private String registrationLink;

    @Column(name = "event_image_url", length = 500)
    private String eventImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private EventStatus status = EventStatus.UPCOMING;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    public boolean isActive() { return Boolean.TRUE.equals(isActive); }

    public enum EventStatus { UPCOMING, ONGOING, COMPLETED, CANCELLED }
}
