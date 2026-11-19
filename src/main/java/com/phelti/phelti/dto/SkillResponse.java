package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Skill of the catalog")
public record SkillResponse(
        @Schema(description = "Skill identifier", example = "1")
        Integer skillId,
        @Schema(description = "Skill name", example = "Programacion")
        String name
) {
}
