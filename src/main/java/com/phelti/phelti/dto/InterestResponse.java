package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Interest of the catalog")
public record InterestResponse(
        @Schema(description = "Interest identifier", example = "1")
        Integer interestId,
        @Schema(description = "Interest name", example = "Cocina")
        String name
) {
}
