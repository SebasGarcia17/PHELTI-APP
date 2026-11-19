package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Data required to create a skill")
public record SkillRequest(
        @Schema(description = "Skill name, unique (max 80 characters)", example = "Guitarra")
        @NotBlank
        @Size(max = 80)
        String name
) {
}
