package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

@Schema(description = "Time slot in which a user can take part in activities")
public record AvailabilityRequest(
        @Schema(description = "Day of the week in English", example = "SATURDAY")
        @NotBlank
        String dayOfWeek,
        @Schema(description = "Start of the slot", example = "09:00:00")
        @NotNull
        LocalTime startTime,
        @Schema(description = "End of the slot, after the start", example = "12:00:00")
        @NotNull
        LocalTime endTime
) {
}
