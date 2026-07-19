package com.megh.smartcampus.dto.request;

import com.megh.smartcampus.entity.GraphEdge;
import jakarta.validation.constraints.*;
import lombok.Data;

/** DTO for adding a walkable path (edge) between two graph nodes. */
@Data
public class CreateGraphEdgeRequest {

    @NotNull(message = "Source node ID is required")
    private Long sourceNodeId;

    @NotNull(message = "Target node ID is required")
    private Long targetNodeId;

    /**
     * Walking distance in metres.
     * Must be strictly positive — zero or negative makes no physical sense
     * and would corrupt Dijkstra's ordering.
     */
    @NotNull(message = "Distance is required")
    @Positive(message = "Distance must be a positive number (metres)")
    private Double distanceMeters;

    private GraphEdge.PathType pathType = GraphEdge.PathType.WALKWAY;

    /** True means both A->B and B->A are walkable. */
    private boolean bidirectional = true;
}
