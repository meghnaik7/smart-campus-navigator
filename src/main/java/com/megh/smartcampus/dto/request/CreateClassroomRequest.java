package com.megh.smartcampus.dto.request;

import com.megh.smartcampus.entity.Classroom;
import jakarta.validation.constraints.*;
import lombok.Data;

/** DTO for creating a classroom or lab. */
@Data
public class CreateClassroomRequest {

    @NotBlank(message = "Room number is required")
    @Size(max = 20, message = "Room number must be 20 characters or fewer")
    private String roomNumber;

    @Size(max = 100)
    private String name;

    @NotNull(message = "Room type is required")
    private Classroom.RoomType roomType;

    @NotNull(message = "Building ID is required")
    private Long buildingId;

    private Integer floor;

    @Positive(message = "Capacity must be a positive number")
    private Integer capacity;

    private Long nearestNodeId;  // optional — links room to graph for navigation

    private boolean hasProjector;
    private boolean hasAc;
    private boolean hasComputers;
}
