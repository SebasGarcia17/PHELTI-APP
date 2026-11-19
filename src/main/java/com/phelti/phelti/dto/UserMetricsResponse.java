package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Compliance and reputation metrics of a user")
public record UserMetricsResponse(
        @Schema(description = "User identifier", example = "1")
        Integer userId,
        @Schema(description = "Activities where the user takes part", example = "5")
        long totalActivities,
        @Schema(description = "Activities still scheduled", example = "1")
        long scheduledActivities,
        @Schema(description = "Completed activities", example = "3")
        long completedActivities,
        @Schema(description = "Cancelled activities", example = "1")
        long cancelledActivities,
        @Schema(description = "Percentage of finished activities that were completed", example = "75.00")
        BigDecimal completionRate,
        @Schema(description = "Average rating received", example = "4.50")
        BigDecimal averageRating,
        @Schema(description = "Number of ratings received", example = "3")
        long ratingsReceived,
        @Schema(description = "Phelts earned giving support in completed activities", example = "40.00")
        BigDecimal pheltsEarned,
        @Schema(description = "Phelts spent receiving support in completed activities", example = "20.00")
        BigDecimal pheltsSpent
) {
}
