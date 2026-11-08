package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Check-in code that confirms the activity took place")
public record CompleteActivityRequest(
        @Schema(description = "Code generated when the activity was scheduled", example = "3f1c2a54-9d7e-4b1a-8a3e-0c2b6d1f7e90")
        @NotBlank
        String checkInCode
) {
}
