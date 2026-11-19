package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Data required to create a publication category")
public record CategoryRequest(
        @Schema(description = "Category name, unique (max 80 characters)", example = "Musica")
        @NotBlank
        @Size(max = 80)
        String name
) {
}
