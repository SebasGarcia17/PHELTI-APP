package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Data required to create a interest")
public record InterestRequest(
        @Schema(description = "Interest name, unique (max 80 characters)", example = "Fotografia")
        @NotBlank
        @Size(max = 80)
        String name
) {
}
