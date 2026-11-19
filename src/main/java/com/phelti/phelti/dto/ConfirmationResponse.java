package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Confirmation of an activity made by one user")
public record ConfirmationResponse(
        @Schema(description = "Confirmation identifier", example = "1")
        Integer confirmationId,
        @Schema(description = "Confirmed activity", example = "1")
        Integer activityId,
        @Schema(description = "User who confirmed", example = "1")
        Integer userId,
        @Schema(description = "Moment of the confirmation")
        LocalDateTime confirmedAt
) {
}
