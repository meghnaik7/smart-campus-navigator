package com.megh.smartcampus.dto.request;

import com.megh.smartcampus.entity.Building;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO for creating a new building.
 * Bean Validation annotations mean the controller never receives
 * invalid data — Spring rejects it before our code even runs,
 * and GlobalExceptionHandler returns a proper 400 with field details.
 */
@Data
public class CreateBuildingRequest {

    @NotBlank(message = "Building name is required")
    @Size(max = 150, message = "Name must be 150 characters or fewer")
    private String name;

    @Size(max = 30, message = "Code must be 30 characters or fewer")
    private String code;          // optional, but must be unique if provided

    @Size(max = 500)
    private String description;

    @NotNull(message = "Building type is required")
    private Building.BuildingType type;

    @Min(value = 1, message = "Floors must be at least 1")
    @Max(value = 100, message = "Floors must be 100 or fewer")
    private Integer floors;

    private Double coordinateX;   // 0–100 map percentage
    private Double coordinateY;

    @Size(max = 500)
    private String imageUrl;
}
