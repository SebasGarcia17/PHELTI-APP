package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Publication category")
public record CategoryResponse(
        @Schema(description = "Category identifier", example = "1")
        Integer categoryId,
        @Schema(description = "Category name", example = "Tecnologia")
        String name
) {
}
