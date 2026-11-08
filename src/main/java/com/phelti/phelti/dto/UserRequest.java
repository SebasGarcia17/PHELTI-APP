package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Data required to register a user")
public record UserRequest(
        @Schema(description = "Full name (max 100 characters)", example = "Ana Torres")
        @NotBlank
        @Size(max = 100)
        String name,
        @Schema(description = "Unique email (max 150 characters)", example = "ana.torres@example.com")
        @NotBlank
        @Email
        @Size(max = 150)
        String email,
        @Schema(description = "Optional short description of the user", example = "Guitar teacher")
        String description
) {
}
