package com.phelti.phelti.controller;

import com.phelti.phelti.dto.AvailabilityRequest;
import com.phelti.phelti.dto.AvailabilityResponse;
import com.phelti.phelti.entity.AvailabilityEntity;
import com.phelti.phelti.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Availability", description = "Days and hours in which a user can take part in activities")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @Operation(
            summary = "Add an availability slot to a user",
            description = "The slot cannot overlap another slot of the same user on the same day"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Availability registered"),
            @ApiResponse(responseCode = "400", description = "Invalid day or hours"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "The slot overlaps an existing one")
    })
    @PostMapping("/users/{userId}/availability")
    public ResponseEntity<AvailabilityResponse> addAvailability(
            @Parameter(description = "User identifier", example = "1")
            @PathVariable Integer userId,
            @Valid @RequestBody AvailabilityRequest request) {

        AvailabilityEntity availability = availabilityService.addAvailability(
                userId,
                request.dayOfWeek(),
                request.startTime(),
                request.endTime()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(availability));
    }

    @Operation(summary = "List the availability of a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Availability listed"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/users/{userId}/availability")
    public ResponseEntity<List<AvailabilityResponse>> findAvailabilityByUserId(
            @Parameter(description = "User identifier", example = "1")
            @PathVariable Integer userId) {

        List<AvailabilityResponse> slots = availabilityService.findAvailabilityByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(slots);
    }

    @Operation(summary = "Delete an availability slot")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Availability deleted"),
            @ApiResponse(responseCode = "404", description = "Availability not found")
    })
    @DeleteMapping("/availability/{id}")
    public ResponseEntity<Void> deleteAvailability(
            @Parameter(description = "Availability identifier", example = "1")
            @PathVariable("id") Integer availabilityId) {

        availabilityService.deleteAvailability(availabilityId);

        return ResponseEntity.noContent().build();
    }

    private AvailabilityResponse toResponse(AvailabilityEntity availability) {

        return new AvailabilityResponse(
                availability.getAvailabilityId(),
                availability.getDayOfWeek(),
                availability.getStartTime(),
                availability.getEndTime()
        );
    }
}
