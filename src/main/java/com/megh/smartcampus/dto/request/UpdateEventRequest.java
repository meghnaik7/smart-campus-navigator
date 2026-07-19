package com.megh.smartcampus.dto.request;

import com.megh.smartcampus.entity.CampusEvent;
import jakarta.validation.constraints.*;
import lombok.Data;

/** DTO for updating an existing event's editable fields. */
@Data
public class UpdateEventRequest {

    @Size(max = 200)
    private String title;

    @Size(max = 1000)
    private String description;

    @Size(max = 200)
    private String venueName;

    @Size(max = 100)
    private String organizer;

    /** Status can be updated by admin — e.g. mark as ONGOING or COMPLETED. */
    private CampusEvent.EventStatus status;
}
