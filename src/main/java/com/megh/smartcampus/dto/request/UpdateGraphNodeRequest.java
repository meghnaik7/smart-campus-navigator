package com.megh.smartcampus.dto.request;

import jakarta.validation.constraints.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

/** DTO for updating an existing graph node. */
@Data
public class UpdateGraphNodeRequest {

    @Size(max = 150)
    private String name;

    @DecimalMin("0.0") @DecimalMax("100.0")
    private Double coordinateX;

    @DecimalMin("0.0") @DecimalMax("100.0")
    private Double coordinateY;

    @Size(max = 255)
    private String description;
}
