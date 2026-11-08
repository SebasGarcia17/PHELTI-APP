package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
        String message
) {
}
