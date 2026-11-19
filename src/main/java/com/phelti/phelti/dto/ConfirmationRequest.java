package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Confirmation that an activity took place, made by one of its two users")
public record ConfirmationRequest(
        @Schema(description = "User who confirms (requester or participant of the activity)", example = "1")
        @NotNull
        Integer userId,
        @Schema(description = "Check-in code of the activity")
        @NotBlank
        String checkInCode
) {
}
