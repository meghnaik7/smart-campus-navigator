package com.megh.smartcampus.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/** DTO for updating an existing building. Code is not updatable (it is a stable identifier). */
@Data
public class UpdateBuildingRequest {

    @NotBlank(message = "Building name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 500)
    private String description;

    @Min(1) @Max(100)
    private Integer floors;

    private Double coordinateX;
    private Double coordinateY;

    @Size(max = 500)
    private String imageUrl;
}
