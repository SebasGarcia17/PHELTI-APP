package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Publication compatible with a user, with its compatibility score")
public record PublicationMatchResponse(
        @Schema(description = "Compatible publication")
        PublicationResponse publication,
        @Schema(description = "Compatibility score, higher is better", example = "6")
        int score,
        @Schema(description = "Why the publication is compatible")
        List<String> reasons
) {
}
