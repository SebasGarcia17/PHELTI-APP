package com.phelti.phelti.controller;

import com.phelti.phelti.dto.CategoryRequest;
import com.phelti.phelti.dto.CategoryResponse;
import com.phelti.phelti.entity.CategoryEntity;
import com.phelti.phelti.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @Operation(summary = "Create a category")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created"),
            @ApiResponse(responseCode = "400", description = "Invalid category data"),
            @ApiResponse(responseCode = "409", description = "Category already exists")
    })
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {

        CategoryEntity category = categoryService.createCategory(request.name());

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(category));
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
