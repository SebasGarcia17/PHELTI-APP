package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Data required to create or replace a publication (offer or request for support)")
public record PublicationRequest(
        @Schema(description = "Owner of the publication (existing user)", example = "1")
        @NotNull
        Integer userId,
        @Schema(description = "Category of the publication (existing category)", example = "2")
        @NotNull
        Integer categoryId,
        @Schema(description = "Type of publication", allowableValues = {"OFFERS_SUPPORT", "NEEDS_SUPPORT"}, example = "OFFERS_SUPPORT")
        @NotBlank
        String publicationType,
        @Schema(description = "Short title, required (max 150 characters)", example = "Guitar lessons for beginners")
        @NotBlank
        @Size(max = 150)
        String title,
        @Schema(description = "Optional detailed description", example = "One hour of guitar basics")
        String description,
        @Schema(description = "How the activity takes place", allowableValues = {"VIRTUAL", "PRESENTIAL"}, example = "VIRTUAL")
        @NotBlank
        String interactionMode,
        @Schema(description = "Phelts offered or requested, greater than zero", example = "20.00")
        @NotNull
        @Positive
        BigDecimal pheltAmount,
        @Schema(description = "Whether the amount can be negotiated", example = "true")
        boolean negotiable,
        @Schema(description = "Status; defaults to PUBLISHED when omitted", allowableValues = {"PUBLISHED", "IN_PROGRESS", "COMPLETED", "CANCELLED"}, example = "PUBLISHED")
        String publicationStatus
) {
}
