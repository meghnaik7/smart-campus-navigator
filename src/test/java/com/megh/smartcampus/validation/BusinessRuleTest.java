package com.megh.smartcampus.validation;

import com.megh.smartcampus.dto.request.CreateGraphEdgeRequest;
import com.megh.smartcampus.dto.request.CreateBuildingRequest;
import com.megh.smartcampus.entity.Building;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bean Validation tests — verify DTO constraints fire correctly.
 * Uses Hibernate Validator directly; no Spring context needed. Very fast.
 */
@DisplayName("DTO Bean Validation Tests")
class BusinessRuleTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // ── Building DTO ──────────────────────────────────────────────────

    @Test
    @DisplayName("Building: missing name produces a validation error")
    void building_nameRequired() {
        CreateBuildingRequest req = new CreateBuildingRequest();
        req.setType(Building.BuildingType.ACADEMIC);
        // name intentionally blank

        Set<ConstraintViolation<CreateBuildingRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    @DisplayName("Building: missing type produces a validation error")
    void building_typeRequired() {
        CreateBuildingRequest req = new CreateBuildingRequest();
        req.setName("CSE Block");
        // type intentionally null

        Set<ConstraintViolation<CreateBuildingRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("type"));
    }

    @Test
    @DisplayName("Building: floors = 0 rejected (must be at least 1)")
    void building_zeroFloorsRejected() {
        CreateBuildingRequest req = new CreateBuildingRequest();
        req.setName("CSE Block");
        req.setType(Building.BuildingType.ACADEMIC);
        req.setFloors(0);

        Set<ConstraintViolation<CreateBuildingRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("floors"));
    }

    @Test
    @DisplayName("Building: valid request passes all constraints")
    void building_validRequest() {
        CreateBuildingRequest req = new CreateBuildingRequest();
        req.setName("CSE Block");
        req.setType(Building.BuildingType.ACADEMIC);
        req.setFloors(4);
        req.setCoordinateX(25.0);
        req.setCoordinateY(55.0);

        Set<ConstraintViolation<CreateBuildingRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    // ── Graph edge DTO ────────────────────────────────────────────────

    @Test
    @DisplayName("GraphEdge: zero distance rejected (must be positive)")
    void graphEdge_zeroDistanceRejected() {
        CreateGraphEdgeRequest req = new CreateGraphEdgeRequest();
        req.setSourceNodeId(1L);
        req.setTargetNodeId(2L);
        req.setDistanceMeters(0.0);

        Set<ConstraintViolation<CreateGraphEdgeRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("distanceMeters"));
    }

    @Test
    @DisplayName("GraphEdge: negative distance rejected")
    void graphEdge_negativeDistanceRejected() {
        CreateGraphEdgeRequest req = new CreateGraphEdgeRequest();
        req.setSourceNodeId(1L);
        req.setTargetNodeId(2L);
        req.setDistanceMeters(-50.0);

        Set<ConstraintViolation<CreateGraphEdgeRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("distanceMeters"));
    }

    @Test
    @DisplayName("GraphEdge: missing source node ID rejected")
    void graphEdge_missingSourceId() {
        CreateGraphEdgeRequest req = new CreateGraphEdgeRequest();
        req.setTargetNodeId(2L);
        req.setDistanceMeters(100.0);

        Set<ConstraintViolation<CreateGraphEdgeRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("sourceNodeId"));
    }

    @Test
    @DisplayName("GraphEdge: valid request passes all constraints")
    void graphEdge_validRequest() {
        CreateGraphEdgeRequest req = new CreateGraphEdgeRequest();
        req.setSourceNodeId(1L);
        req.setTargetNodeId(2L);
        req.setDistanceMeters(150.0);

        Set<ConstraintViolation<CreateGraphEdgeRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }
}
