package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Rating of an activity")
public record RatingResponse(
        @Schema(description = "Rating identifier", example = "1")
        Integer ratingId,
        @Schema(description = "Rated activity", example = "1")
        Integer activityId,
        @Schema(description = "User who gave the rating", example = "1")
        Integer raterId,
        @Schema(description = "User who received the rating", example = "2")
        Integer ratedId,
        @Schema(description = "Score from 1 to 5", example = "5")
        Integer score,
        @Schema(description = "Optional comment")
        String comment,
        @Schema(description = "Moment of the rating")
        LocalDateTime createdAt
) {
}
