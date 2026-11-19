package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Rating given by one user of a completed activity to the other")
public record RatingRequest(
        @Schema(description = "Completed activity being rated", example = "1")
        @NotNull
        Integer activityId,
        @Schema(description = "User who rates (requester or participant of the activity)", example = "1")
        @NotNull
        Integer raterId,
        @Schema(description = "Score from 1 to 5", example = "5")
        @NotNull
        @Min(1)
        @Max(5)
        Integer score,
        @Schema(description = "Optional comment, max 500 characters", example = "Very patient and clear")
        @Size(max = 500)
        String comment
) {
}
