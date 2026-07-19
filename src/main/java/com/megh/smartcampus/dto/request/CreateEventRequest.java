package com.megh.smartcampus.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

/** DTO for creating a campus event. */
@Data
public class CreateEventRequest {

    @NotBlank(message = "Event title is required")
    @Size(max = 200, message = "Title must be 200 characters or fewer")
    private String title;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "Start time is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    @Size(max = 200)
    private String venueName;

    @Size(max = 100)
    private String organizer;

    @Positive(message = "Max participants must be a positive number")
    private Integer maxParticipants;

    @Size(max = 500)
    private String registrationLink;

    @Size(max = 500)
    private String eventImageUrl;

    private Long buildingId;
    private Long nearestNodeId;
}
