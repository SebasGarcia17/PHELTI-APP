package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Registered user")
public record UserResponse(
        @Schema(description = "User identifier", example = "1")
        Integer userId,
        @Schema(description = "Full name", example = "Ana Torres")
        String name,
        @Schema(description = "Unique email", example = "ana.torres@example.com")
        String email,
        @Schema(description = "Optional description", example = "Guitar teacher")
        String description,
        @Schema(description = "Average rating received", example = "0.00")
        BigDecimal averageRating
) {
}
