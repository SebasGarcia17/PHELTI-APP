package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Data to update the profile of a user")
public record ProfileUpdateRequest(
        @Schema(description = "Full name (max 100 characters)", example = "Ana Torres")
        @NotBlank
        @Size(max = 100)
        String name,
        @Schema(description = "Short description of the user", example = "Guitar teacher")
        @Size(max = 1500)
        String description,
        @Schema(description = "City or area of the user (max 150 characters)", example = "Medellin")
        @Size(max = 150)
        String location,
        @Schema(description = "Preferred interaction mode, empty means no preference", allowableValues = {"VIRTUAL", "PRESENTIAL"}, example = "VIRTUAL")
        String preferredInteractionMode
) {
}
