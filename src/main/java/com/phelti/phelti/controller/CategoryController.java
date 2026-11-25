package com.phelti.phelti.controller;

import com.phelti.phelti.dto.CategoryResponse;
import com.phelti.phelti.entity.CategoryEntity;
import com.phelti.phelti.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Categories used to classify publications")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "List all categories")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories listed")
    })
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAllCategories() {

        List<CategoryResponse> categories = categoryService.findAllCategories().stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(categories);
    }

    private CategoryResponse toResponse(CategoryEntity category) {

        return new CategoryResponse(category.getCategoryId(), category.getName());
    }
}
