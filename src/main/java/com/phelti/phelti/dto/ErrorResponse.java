package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard error body returned by every failed request")
public record ErrorResponse(
        @Schema(description = "Moment the error was produced", example = "2026-11-07T14:30:00Z")
        String timestamp,
        @Schema(description = "HTTP status code", example = "404")
        int status,
        @Schema(description = "HTTP reason phrase", example = "Not Found")
        String error,
        @Schema(description = "Human readable explanation", example = "Publication not found with id: 99")
        String message
) {
}
