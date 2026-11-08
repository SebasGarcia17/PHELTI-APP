package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Proposal made by a user on a publication")
public record ProposalResponse(
        @Schema(description = "Proposal identifier", example = "1")
        Integer proposalId,
        @Schema(description = "Publication the proposal is made on", example = "1")
        Integer publicationId,
        @Schema(description = "User who made the proposal", example = "2")
        Integer proposingUserId,
        @Schema(description = "Optional message", example = "I can help on Saturday")
        String message,
        @Schema(description = "Current status", allowableValues = {"PENDING", "ACCEPTED", "REJECTED", "CANCELLED"}, example = "PENDING")
        String proposalStatus,
        @Schema(description = "Creation moment")
        Instant createdAt
) {
}
