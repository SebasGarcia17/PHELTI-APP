package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Data required to make a proposal on a publication")
public record ProposalRequest(
        @Schema(description = "Publication the proposal is made on (must be PUBLISHED)", example = "1")
        @NotNull
        Integer publicationId,
        @Schema(description = "User who makes the proposal (not the publication owner)", example = "2")
        @NotNull
        Integer proposingUserId,
        @Schema(description = "Optional message, max 500 characters", example = "I can help on Saturday")
        @Size(max = 500)
        String message,
        @Schema(description = "Optional amount of phelts proposed, only allowed when the publication is negotiable", example = "15.00")
        @Positive
        BigDecimal proposedAmount
) {
}
