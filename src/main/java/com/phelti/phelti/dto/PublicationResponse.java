package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Time-exchange publication (offer or request for support)")
public record PublicationResponse(
        @Schema(description = "Publication identifier", example = "1")
        Integer publicationId,
        @Schema(description = "Owner of the publication", example = "1")
        Integer userId,
        @Schema(description = "Category of the publication", example = "2")
        Integer categoryId,
        @Schema(description = "Type of publication", allowableValues = {"OFFERS_SUPPORT", "NEEDS_SUPPORT"}, example = "OFFERS_SUPPORT")
        String publicationType,
        @Schema(description = "Short title", example = "Guitar lessons for beginners")
        String title,
        @Schema(description = "Detailed description", example = "One hour of guitar basics")
        String description,
        @Schema(description = "How the activity takes place", allowableValues = {"VIRTUAL", "PRESENTIAL"}, example = "VIRTUAL")
        String interactionMode,
        @Schema(description = "Phelts offered or requested", example = "20.00")
        BigDecimal pheltAmount,
        @Schema(description = "Whether the amount can be negotiated", example = "true")
        boolean negotiable,
        @Schema(description = "Current status", allowableValues = {"PUBLISHED", "IN_PROGRESS", "COMPLETED", "CANCELLED"}, example = "PUBLISHED")
        String publicationStatus
) {
}
