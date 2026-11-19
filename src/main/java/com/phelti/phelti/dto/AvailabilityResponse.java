package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

@Schema(description = "Availability slot of a user")
public record AvailabilityResponse(
        @Schema(description = "Availability identifier", example = "1")
        Integer availabilityId,
        @Schema(description = "Day of the week", example = "SATURDAY")
        String dayOfWeek,
        @Schema(description = "Start of the slot", example = "09:00:00")
        LocalTime startTime,
        @Schema(description = "End of the slot", example = "12:00:00")
        LocalTime endTime
) {
}
