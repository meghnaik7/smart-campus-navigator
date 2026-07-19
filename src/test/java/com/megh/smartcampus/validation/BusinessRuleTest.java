package com.megh.smartcampus.validation;

import com.megh.smartcampus.dto.request.CreateEventRequest;
import com.megh.smartcampus.dto.request.CreateClassroomRequest;
import com.megh.smartcampus.dto.request.CreateGraphEdgeRequest;
import com.megh.smartcampus.entity.Classroom;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bean Validation tests — verify DTO constraints work correctly.
 *
 * Uses the Hibernate Validator directly, so no Spring context is needed.
 * Very fast tests that catch configuration mistakes before deployment.
 *
 * Interview talking point:
 *   "I test validation at the DTO layer. If a constraint annotation is
 *    mis-configured, this test catches it immediately without spinning up
 *    an HTTP server."
 */
@DisplayName("DTO Bean Validation — Business Rule Tests")
class BusinessRuleTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // ── Classroom DTO ─────────────────────────────────────────────────

    @Test
    @DisplayName("Classroom: missing room number produces a validation error")
    void classroom_roomNumberRequired() {
        CreateClassroomRequest req = new CreateClassroomRequest();
        req.setBuildingId(1L);
        req.setRoomType(Classroom.RoomType.LECTURE_HALL);
        // roomNumber intentionally left blank

        Set<ConstraintViolation<CreateClassroomRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("roomNumber"));
    }

    @Test
    @DisplayName("Classroom: negative capacity produces a validation error")
    void classroom_negativeCapacityRejected() {
        CreateClassroomRequest req = new CreateClassroomRequest();
        req.setRoomNumber("CSE-101");
        req.setBuildingId(1L);
        req.setRoomType(Classroom.RoomType.LECTURE_HALL);
        req.setCapacity(-50);   // negative — violates @Positive

        Set<ConstraintViolation<CreateClassroomRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("capacity"));
    }

    @Test
    @DisplayName("Classroom: zero capacity also rejected (must be strictly positive)")
    void classroom_zeroCapacityRejected() {
        CreateClassroomRequest req = new CreateClassroomRequest();
        req.setRoomNumber("CSE-102");
        req.setBuildingId(1L);
        req.setRoomType(Classroom.RoomType.LECTURE_HALL);
        req.setCapacity(0);   // zero — violates @Positive

        Set<ConstraintViolation<CreateClassroomRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("capacity"));
    }

    @Test
    @DisplayName("Classroom: valid request passes all constraints")
    void classroom_validRequestPassesValidation() {
        CreateClassroomRequest req = new CreateClassroomRequest();
        req.setRoomNumber("CSE-101");
        req.setBuildingId(1L);
        req.setRoomType(Classroom.RoomType.LECTURE_HALL);
        req.setCapacity(60);
        req.setFloor(1);

        Set<ConstraintViolation<CreateClassroomRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    // ── Event DTO ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Event: missing title produces a validation error")
    void event_titleRequired() {
        CreateEventRequest req = new CreateEventRequest();
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setEndTime(LocalDateTime.now().plusDays(2));
        // title intentionally blank

        Set<ConstraintViolation<CreateEventRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("title"));
    }

    @Test
    @DisplayName("Event: missing start time produces a validation error")
    void event_startTimeRequired() {
        CreateEventRequest req = new CreateEventRequest();
        req.setTitle("Tech Fest 2025");
        req.setEndTime(LocalDateTime.now().plusDays(2));
        // startTime intentionally null

        Set<ConstraintViolation<CreateEventRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("startTime"));
    }

    @Test
    @DisplayName("Event: negative maxParticipants produces a validation error")
    void event_negativeMaxParticipantsRejected() {
        CreateEventRequest req = new CreateEventRequest();
        req.setTitle("Tech Fest");
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setEndTime(LocalDateTime.now().plusDays(2));
        req.setMaxParticipants(-1);   // violates @Positive

        Set<ConstraintViolation<CreateEventRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("maxParticipants"));
    }

    // ── Graph edge DTO ────────────────────────────────────────────────

    @Test
    @DisplayName("GraphEdge: zero distance produces a validation error")
    void graphEdge_zeroDistanceRejected() {
        CreateGraphEdgeRequest req = new CreateGraphEdgeRequest();
        req.setSourceNodeId(1L);
        req.setTargetNodeId(2L);
        req.setDistanceMeters(0.0);   // violates @Positive

        Set<ConstraintViolation<CreateGraphEdgeRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("distanceMeters"));
    }

    @Test
    @DisplayName("GraphEdge: negative distance produces a validation error")
    void graphEdge_negativeDistanceRejected() {
        CreateGraphEdgeRequest req = new CreateGraphEdgeRequest();
        req.setSourceNodeId(1L);
        req.setTargetNodeId(2L);
        req.setDistanceMeters(-100.0);  // negative metres — makes no sense

        Set<ConstraintViolation<CreateGraphEdgeRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("distanceMeters"));
    }

    @Test
    @DisplayName("GraphEdge: valid request passes all constraints")
    void graphEdge_validRequestPassesValidation() {
        CreateGraphEdgeRequest req = new CreateGraphEdgeRequest();
        req.setSourceNodeId(1L);
        req.setTargetNodeId(2L);
        req.setDistanceMeters(150.0);

        Set<ConstraintViolation<CreateGraphEdgeRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }
}
