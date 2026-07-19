package com.megh.smartcampus.dto.request;

import com.megh.smartcampus.entity.GraphNode;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

/** DTO for creating a graph node (a point on the campus map). */
@Data
public class CreateGraphNodeRequest {

    @NotBlank(message = "Node name is required")
    @Size(max = 150, message = "Name must be 150 characters or fewer")
    private String name;

    @NotNull(message = "Node type is required")
    private GraphNode.NodeType nodeType;

    @NotNull(message = "X coordinate is required")
    @DecimalMin(value = "0.0", message = "X must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "X must be between 0 and 100")
    private Double coordinateX;

    @NotNull(message = "Y coordinate is required")
    @DecimalMin(value = "0.0", message = "Y must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Y must be between 0 and 100")
    private Double coordinateY;

    @Min(value = 0, message = "Floor must be 0 or above")
    @Max(value = 50)
    private Integer floor = 0;

    private Long buildingId;  // optional — null means an outdoor node

    @Size(max = 255)
    private String description;
}
